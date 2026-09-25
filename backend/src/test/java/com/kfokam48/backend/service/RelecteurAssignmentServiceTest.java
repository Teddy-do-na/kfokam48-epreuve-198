package com.kfokam48.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.kfokam48.backend.entity.CoursSession;
import com.kfokam48.backend.entity.Etudiant;
import com.kfokam48.backend.entity.Exercice;
import com.kfokam48.backend.entity.Presence;
import com.kfokam48.backend.entity.Promotion;
import com.kfokam48.backend.entity.Relecture;
import com.kfokam48.backend.repository.RelectureRepository;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class RelecteurAssignmentServiceTest {
    @Mock private RelectureRepository relectureRepository;

    @Test
    void choisitUnPresentAutreQueLauteur() {
        RelecteurAssignmentService service = new RelecteurAssignmentService(relectureRepository, new java.security.SecureRandom());
        Promotion promotion = new Promotion("Promotion A");
        Etudiant auteur = new Etudiant("Auteur", promotion);
        Etudiant pair = new Etudiant("Pair", promotion);
        ReflectionTestUtils.setField(auteur, "id", 1L);
        ReflectionTestUtils.setField(pair, "id", 2L);
        CoursSession session = new CoursSession("Cours", promotion, "ABC234", Instant.now(), Instant.now().plusSeconds(60));
        session.getPresences().add(new Presence(session, auteur, "ETUDIANT"));
        session.getPresences().add(new Presence(session, pair, "ETUDIANT"));
        Exercice exercice = new Exercice(session, auteur, "https://example.org/work", Instant.now());
        when(relectureRepository.existsByExerciceId(null)).thenReturn(false);
        when(relectureRepository.save(any(Relecture.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.assigner(exercice);

        var captor = org.mockito.ArgumentCaptor.forClass(Relecture.class);
        verify(relectureRepository).save(captor.capture());
        assertEquals(pair, captor.getValue().getRelecteur());
    }

    @Test
    void resteSansAssignationLorsqueSeulAuteurEstPresent() {
        RelecteurAssignmentService service = new RelecteurAssignmentService(relectureRepository);
        Promotion promotion = new Promotion("Promotion A");
        Etudiant auteur = new Etudiant("Auteur", promotion);
        ReflectionTestUtils.setField(auteur, "id", 1L);
        CoursSession session = new CoursSession("Cours", promotion, "ABC234", Instant.now(), Instant.now().plusSeconds(60));
        session.getPresences().add(new Presence(session, auteur, "ETUDIANT"));
        Exercice exercice = new Exercice(session, auteur, "https://example.org/work", Instant.now());
        when(relectureRepository.existsByExerciceId(null)).thenReturn(false);

        service.assigner(exercice);

        verify(relectureRepository, never()).save(any());
    }
}
