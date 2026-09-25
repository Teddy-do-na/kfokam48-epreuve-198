package com.kfokam48.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import com.kfokam48.backend.entity.CoursSession;
import com.kfokam48.backend.entity.Etudiant;
import com.kfokam48.backend.entity.Exercice;
import com.kfokam48.backend.entity.Promotion;
import com.kfokam48.backend.entity.Relecture;
import com.kfokam48.backend.repository.EtudiantRepository;
import com.kfokam48.backend.repository.RelectureRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RelecturePrivacyTest {
    @Mock private RelectureRepository relectureRepository;
    @Mock private EtudiantRepository etudiantRepository;

    @Test
    void retourneLaNoteEtLeCommentaireSansIdentiteDuRelecteur() {
        Promotion promotion = new Promotion("Promotion A");
        Etudiant author = new Etudiant("Auteur", promotion);
        Etudiant reviewer = new Etudiant("Nom secret", promotion);
        CoursSession session = new CoursSession("Cours", promotion, "ABC234", Instant.now(), Instant.now().plusSeconds(600));
        Exercice exercise = new Exercice(session, author, "https://example.org", Instant.now());
        Relecture review = new Relecture(exercise, reviewer);
        review.soumettre((short) 17, "Bon travail", Instant.now());
        when(relectureRepository.findByExerciceEtudiantId(3L)).thenReturn(List.of(review));
        RelectureService service = new RelectureService(relectureRepository, etudiantRepository,
                Clock.fixed(Instant.now(), ZoneOffset.UTC));

        var result = service.consulterParEtudiant(3L).get(0);

        assertEquals(17, result.note());
        assertEquals("Bon travail", result.commentaire());
        assertEquals("RENDUE", result.statut());
    }
}
