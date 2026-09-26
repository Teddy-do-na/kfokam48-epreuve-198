package com.kfokam48.backend.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.kfokam48.backend.dto.DeposerExerciceRequest;
import com.kfokam48.backend.dto.OuvrirSessionRequest;
import com.kfokam48.backend.dto.SessionResponse;
import com.kfokam48.backend.entity.CoursSession;
import com.kfokam48.backend.entity.Promotion;
import com.kfokam48.backend.exception.ApiException;
import com.kfokam48.backend.repository.CoursSessionRepository;
import com.kfokam48.backend.repository.PresenceRepository;
import com.kfokam48.backend.repository.PromotionRepository;
import com.kfokam48.backend.repository.TentativeCodeRepository;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

/**
 * Reproduction des défauts de l'enveloppe étape 3.
 *
 * <p><strong>ISSUE 18</strong> — rapport client : « deux étudiants tapent le code presque en même
 * temps et il n'y en a qu'un seul qui apparaît ». L'assignation du relecteur échoue en base
 * ({@code Relecture} sans horodatages → 23502), la transaction de présence est annulée et la
 * présence disparaît du tableau.</p>
 *
 * <p><strong>ISSUE 19</strong> — {@code TentativeCode} ne renseigne jamais {@code session} alors
 * que la colonne est NOT NULL : chaque code inconnu ou expiré plante (23502) au lieu de renvoyer
 * 400 / 410, et le compteur de blocage après 5 échecs (EF5/RG3) ne se remplit jamais.</p>
 *
 * <p><strong>ISSUE 20</strong> — le contrôle « déjà présent » puis l'insertion ne sont pas
 * atomiques : deux soumissions simultanées du même étudiant peuvent produire un 500 au lieu
 * d'un 409 {@code DEJA_PRESENT}.</p>
 *
 * <p>Tests rouges avant le correctif, verts après (V1/V2 inchangées, migration V3 ajoutée).
 * PostgreSQL réel : conteneur testcontainers si Docker est disponible, sinon la base locale
 * {@code kfokam48_test} (variables {@code DB_HOST}/{@code DB_PORT}/{@code DB_USER}/
 * {@code DB_PASSWORD}, mêmes valeurs que l'application). La classe est désactivée si ni l'un ni
 * l'autre n'est accessible.</p>
 */
@SpringBootTest
@EnabledIf("datasourceDisponible")
class PresenceAssignationIntegrationTest {

    private static final String BASE_TEST = "kfokam48_test";

    private static final PostgreSQLContainer<?> POSTGRES;
    private static final String LOCAL_URL;
    private static final String LOCAL_USER;
    private static final String LOCAL_PASSWORD;
    private static final boolean DOCKER;

    static {
        PostgreSQLContainer<?> conteneur = null;
        try {
            conteneur = new PostgreSQLContainer<>("postgres:16-alpine");
            conteneur.start();
        } catch (Throwable t) {
            // Docker absent : on tente ci-dessous le Postgres local
            conteneur = null;
        }
        POSTGRES = conteneur;
        DOCKER = conteneur != null;

        String url = null;
        String user = env("DB_USER", "postgres");
        String password = env("DB_PASSWORD", "Teddy");
        if (!DOCKER) {
            String host = env("DB_HOST", "localhost");
            String port = env("DB_PORT", "5432");
            try (Connection admin = DriverManager.getConnection(
                    "jdbc:postgresql://" + host + ":" + port + "/postgres", user, password)) {
                try (var statement = admin.createStatement();
                     ResultSet existante = statement.executeQuery(
                             "SELECT 1 FROM pg_database WHERE datname = '" + BASE_TEST + "'")) {
                    if (!existante.next()) {
                        try (var creation = admin.createStatement()) {
                            creation.execute("CREATE DATABASE " + BASE_TEST);
                        }
                    }
                }
                url = "jdbc:postgresql://" + host + ":" + port + "/" + BASE_TEST;
            } catch (Throwable t) {
                // Ni Docker ni Postgres local : la classe de test est désactivée par @EnabledIf
                url = null;
            }
        }
        LOCAL_URL = url;
        LOCAL_USER = user;
        LOCAL_PASSWORD = password;
    }

    private static String env(String cle, String defaut) {
        String valeur = System.getenv(cle);
        return valeur == null || valeur.isBlank() ? defaut : valeur;
    }

    static boolean datasourceDisponible() {
        return DOCKER || LOCAL_URL != null;
    }

    @DynamicPropertySource
    static void datasource(DynamicPropertyRegistry registry) {
        if (DOCKER) {
            registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
            registry.add("spring.datasource.username", POSTGRES::getUsername);
            registry.add("spring.datasource.password", POSTGRES::getPassword);
        } else {
            registry.add("spring.datasource.url", () -> LOCAL_URL);
            registry.add("spring.datasource.username", () -> LOCAL_USER);
            registry.add("spring.datasource.password", () -> LOCAL_PASSWORD);
        }
    }

    @Autowired
    private SessionService sessionService;
    @Autowired
    private PresenceService presenceService;
    @Autowired
    private ExerciceService exerciceService;
    @Autowired
    private PresenceRepository presenceRepository;
    @Autowired
    private TentativeCodeRepository tentativeCodeRepository;
    @Autowired
    private CoursSessionRepository sessionRepository;
    @Autowired
    private PromotionRepository promotionRepository;

    @BeforeEach
    void repartirDeZero() {
        // Les tentatives de code d'un essai précédent ne doivent pas bloquer le suivant (EF5)
        tentativeCodeRepository.deleteAllInBatch();
    }

    // ------------------------------------------------------------------ ISSUE 18

    @Test
    void presenceSaisiePendantQuUnExerciceAttendSonRelecteur_neDoitPasEtrePerdue() {
        SessionResponse session = sessionService.ouvrir(new OuvrirSessionRequest("Session integration", 1L));
        presenceService.marquer(session.code(), 1L);
        exerciceService.deposer(new DeposerExerciceRequest(session.id(), 1L,
                "https://github.com/kfokam48/exo-integration"));

        // Scénario du rapport client : un autre étudiant saisit le bon code
        assertDoesNotThrow(() -> presenceService.marquer(session.code(), 2L),
                "La presence de l'etudiant 2 est perdue : la transaction est annulee par l'echec d'assignation");
        assertTrue(presenceRepository.existsBySessionIdAndEtudiantId(session.id(), 2L),
                "La presence de l'etudiant 2 doit figurer dans le tableau du formateur");
    }

    @Test
    void deuxPresencesSaisiesEnMemeTemps_sontToutesDeuxEnregistrees() throws Exception {
        SessionResponse session = sessionService.ouvrir(new OuvrirSessionRequest("Session simultanee", 1L));
        presenceService.marquer(session.code(), 1L);
        exerciceService.deposer(new DeposerExerciceRequest(session.id(), 1L,
                "https://github.com/kfokam48/exo-simultane"));

        Soumissions simultanees = marquerEnMemeTemps(session.code(), 2L, 3L);

        assertEquals(2, simultanees.succes(), "Les deux presences doivent etre enregistrees");
        assertTrue(simultanees.echecs().isEmpty(), () -> "Soumissions simultanees echouees : " + simultanees.echecs());
        assertTrue(presenceRepository.existsBySessionIdAndEtudiantId(session.id(), 2L),
                "L'etudiant 2 doit apparaitre dans le tableau");
        assertTrue(presenceRepository.existsBySessionIdAndEtudiantId(session.id(), 3L),
                "L'etudiant 3 doit apparaitre dans le tableau");
    }

    // ------------------------------------------------------------------ ISSUE 19

    @Test
    void codeInconnuRenvoie400EtCompteDansLesEchecsPourLeBlocage() {
        ApiException inconnu = assertThrows(ApiException.class,
                () -> presenceService.marquer("INCONNU9", 3L),
                "Un code inconnu doit renvoyer 400 CODE_INCONNU, pas une erreur interne");
        assertEquals(400, inconnu.getStatus().value());
        assertEquals("CODE_INCONNU", inconnu.getCode());
        assertEquals(1, tentativeCodeRepository.count(),
                "L'echec doit etre comptabilise pour le blocage apres 5 essais (EF5, RG3)");

        for (int tentative = 1; tentative < 5; tentative++) {
            final String codeFaux = "INCONNU" + tentative;
            assertThrows(ApiException.class, () -> presenceService.marquer(codeFaux, 3L));
        }

        SessionResponse session = sessionService.ouvrir(new OuvrirSessionRequest("Blocage", 1L));
        ApiException bloque = assertThrows(ApiException.class,
                () -> presenceService.marquer(session.code(), 3L),
                "La 6e tentative doit etre refusee meme avec le bon code");
        assertEquals("ETUDIANT_BLOQUE", bloque.getCode());
    }

    @Test
    void codeExpireRenvoie410() {
        Instant maintenant = Instant.now();
        Promotion promotion = promotionRepository.findById(1L).orElseThrow();
        // Code unique : les essais successifs du même test ne doivent pas heurter UNIQUE (code)
        String code = "EXP" + (System.nanoTime() % 1_000_000_000L);
        sessionRepository.save(new CoursSession("Session expiree", promotion, code,
                maintenant.minusSeconds(1200), maintenant.minusSeconds(300)));

        ApiException expire = assertThrows(ApiException.class,
                () -> presenceService.marquer(code, 3L),
                "Un code expire doit renvoyer 410 CODE_EXPIRE, pas une erreur interne");
        assertEquals(410, expire.getStatus().value());
        assertEquals("CODE_EXPIRE", expire.getCode());
    }

    // ------------------------------------------------------------------ ISSUE 20

    @Test
    void doublePresenceSimultaneeRenvoieExactementUn201EtDes409() throws Exception {
        SessionResponse session = sessionService.ouvrir(new OuvrirSessionRequest("Double presence", 1L));

        Soumissions simultanees = marquerEnMemeTemps(session.code(), 2L, 2L, 2L, 2L);

        assertTrue(presenceRepository.existsBySessionIdAndEtudiantId(session.id(), 2L),
                "La premiere presence doit etre enregistree");
        assertEquals(1, simultanees.succes(), "Exactement une soumission doit recevoir 201");
        assertEquals(3, simultanees.echecs().size(),
                () -> "Les 3 autres soumissions doivent recevoir 409 : " + simultanees.echecs());
        for (Throwable echec : simultanees.echecs()) {
            assertTrue(echec instanceof ApiException,
                    () -> "Aucune soumission ne doit planter : " + echec);
            assertEquals("DEJA_PRESENT", ((ApiException) echec).getCode(),
                    () -> "Les autres soumissions doivent recevoir 409 DEJA_PRESENT : " + echec);
        }
    }

    private record Soumissions(int succes, List<Throwable> echecs) {
    }

    private Soumissions marquerEnMemeTemps(String code, Long... etudiants) throws Exception {
        CountDownLatch debut = new CountDownLatch(1);
        List<Throwable> echecs = new ArrayList<>();
        int[] succes = {0};
        ExecutorService pool = Executors.newFixedThreadPool(etudiants.length);
        for (Long etudiantId : etudiants) {
            final Long id = etudiantId;
            pool.execute(() -> {
                try {
                    debut.await();
                    presenceService.marquer(code, id);
                    synchronized (echecs) {
                        succes[0]++;
                    }
                } catch (Throwable t) {
                    synchronized (echecs) {
                        echecs.add(t);
                    }
                }
            });
        }
        debut.countDown();
        pool.shutdown();
        assertTrue(pool.awaitTermination(30, TimeUnit.SECONDS), "Les soumissions doivent aboutir");
        return new Soumissions(succes[0], echecs);
    }
}
