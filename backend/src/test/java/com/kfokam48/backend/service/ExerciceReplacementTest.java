package com.kfokam48.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import com.kfokam48.backend.entity.CoursSession;
import com.kfokam48.backend.entity.Etudiant;
import com.kfokam48.backend.entity.Exercice;
import com.kfokam48.backend.entity.Promotion;
import com.kfokam48.backend.exception.ApiException;
import com.kfokam48.backend.repository.CoursSessionRepository;
import com.kfokam48.backend.repository.EtudiantRepository;
import com.kfokam48.backend.repository.ExerciceRepository;
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
class ExerciceReplacementTest {
    @Mock private CoursSessionRepository sessionRepository;
    @Mock private EtudiantRepository etudiantRepository;
    @Mock private ExerciceRepository exerciceRepository;
    @Mock private RelectureRepository relectureRepository;
    @Mock private RelecteurAssignmentService assignmentService;
    private ExerciceService service;

    @BeforeEach
    void setUp() {
        service = new ExerciceService(sessionRepository, etudiantRepository, exerciceRepository, relectureRepository, assignmentService,
                Clock.fixed(Instant.parse("2026-01-01T10:30:00Z"), ZoneOffset.UTC));
    }

    @Test
    void refuseLeRemplacementApresUneRelectureRendue() {
        Promotion promotion = new Promotion("Promotion A");
        CoursSession session = new CoursSession("Cours", promotion, "ABC234",
                Instant.parse("2026-01-01T10:00:00Z"), Instant.parse("2026-01-01T10:15:00Z"));
        Exercice exercice = new Exercice(session, new Etudiant("Étudiant", promotion), "https://example.org/old",
                Instant.parse("2026-01-01T10:10:00Z"));
        when(exerciceRepository.findByIdAndEtudiantId(4L, 3L)).thenReturn(Optional.of(exercice));
        when(relectureRepository.existsByExerciceIdAndStatut(4L, "RENDUE")).thenReturn(true);

        ApiException exception = assertThrows(ApiException.class,
                () -> service.remplacer(4L, 3L, "https://example.org/new"));

        assertEquals("RELECTURE_DEJA_RENDUE", exception.getCode());
    }
}
