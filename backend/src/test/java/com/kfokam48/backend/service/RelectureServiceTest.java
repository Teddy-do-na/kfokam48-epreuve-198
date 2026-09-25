package com.kfokam48.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import com.kfokam48.backend.dto.SoumettreRelectureRequest;
import com.kfokam48.backend.entity.CoursSession;
import com.kfokam48.backend.entity.Etudiant;
import com.kfokam48.backend.entity.Exercice;
import com.kfokam48.backend.entity.Promotion;
import com.kfokam48.backend.entity.Relecture;
import com.kfokam48.backend.exception.ApiException;
import com.kfokam48.backend.repository.EtudiantRepository;
import com.kfokam48.backend.repository.RelectureRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RelectureServiceTest {
    @Mock private RelectureRepository relectureRepository;
    @Mock private EtudiantRepository etudiantRepository;
    private RelectureService service;

    @BeforeEach
    void setUp() {
        service = new RelectureService(relectureRepository, etudiantRepository,
                Clock.fixed(Instant.parse("2026-01-01T11:00:00Z"), ZoneOffset.UTC));
    }

    @Test
    void interditLaRelectureDeSonPropreExercice() {
        var fixture = fixture();
        when(relectureRepository.findById(7L)).thenReturn(Optional.of(fixture.relecture));
        when(etudiantRepository.findById(3L)).thenReturn(Optional.of(fixture.author));

        ApiException exception = assertThrows(ApiException.class,
                () -> service.soumettre(7L, new SoumettreRelectureRequest(3L, 15, "Avis")));

        assertEquals(403, exception.getStatus().value());
        assertEquals("AUTO_RELECTURE_INTERDITE", exception.getCode());
    }

    @Test
    void enregistreLaNoteDuRelecteurAssigne() {
        var fixture = fixture();
        when(relectureRepository.findById(7L)).thenReturn(Optional.of(fixture.relecture));
        when(etudiantRepository.findById(4L)).thenReturn(Optional.of(fixture.reviewer));

        service.soumettre(7L, new SoumettreRelectureRequest(4L, 18, "  Bon travail  "));

        assertEquals(Short.valueOf((short) 18), fixture.relecture.getNote());
        assertEquals("Bon travail", fixture.relecture.getCommentaire());
        assertEquals("RENDUE", fixture.relecture.getStatut());
    }

    private Fixture fixture() {
        Promotion promotion = new Promotion("Promotion A");
        Etudiant author = new Etudiant("Auteur", promotion);
        Etudiant reviewer = new Etudiant("Relecteur", promotion);
        CoursSession session = new CoursSession("Cours", promotion, "ABC234", Instant.now(), Instant.now().plusSeconds(600));
        Exercice exercise = new Exercice(session, author, "https://example.org/work", Instant.now());
        Relecture review = new Relecture(exercise, reviewer);
        return new Fixture(author, reviewer, review);
    }

    private record Fixture(Etudiant author, Etudiant reviewer, Relecture relecture) {
    }
}
