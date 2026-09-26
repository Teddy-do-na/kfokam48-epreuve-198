package com.kfokam48.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.kfokam48.backend.dto.DeposerExerciceRequest;
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
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class ExerciceServiceTest {
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
    void accepteUnDepotsApresExpirationSiLaSessionNestPasCloturee() {
        Promotion promotion = new Promotion("Promotion A");
        ReflectionTestUtils.setField(promotion, "id", 1L);
        CoursSession session = new CoursSession("Cours", promotion, "ABC234",
                Instant.parse("2026-01-01T10:00:00Z"), Instant.parse("2026-01-01T10:15:00Z"));
        Etudiant etudiant = new Etudiant("Étudiant", promotion);
        ReflectionTestUtils.setField(etudiant, "id", 3L);
        when(sessionRepository.findByIdForUpdate(2L)).thenReturn(Optional.of(session));
        when(etudiantRepository.findById(3L)).thenReturn(Optional.of(etudiant));
        when(exerciceRepository.existsBySessionIdAndEtudiantId(2L, 3L)).thenReturn(false);
        when(exerciceRepository.save(any(Exercice.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = service.deposer(new DeposerExerciceRequest(2L, 3L, "https://example.org/work"));

        assertEquals("EN_ATTENTE_RELECTURE", response.statut());
    }

    @Test
    void refuseUnLienHttp() {
        Promotion promotion = new Promotion("Promotion A");
        ReflectionTestUtils.setField(promotion, "id", 1L);
        CoursSession session = sessionNonCloturee(promotion);
        Etudiant etudiant = new Etudiant("Étudiant", promotion);
        ReflectionTestUtils.setField(etudiant, "id", 3L);
        when(sessionRepository.findByIdForUpdate(2L)).thenReturn(Optional.of(session));
        when(etudiantRepository.findById(3L)).thenReturn(Optional.of(etudiant));

        ApiException exception = assertThrows(ApiException.class,
                () -> service.deposer(new DeposerExerciceRequest(2L, 3L, "http://example.org/work")));

        assertEquals("LIEN_INVALIDE", exception.getCode());
    }

    @Test
    void refuseUnSecondDepot() {
        Promotion promotion = new Promotion("Promotion A");
        ReflectionTestUtils.setField(promotion, "id", 1L);
        CoursSession session = sessionNonCloturee(promotion);
        Etudiant etudiant = new Etudiant("Étudiant", promotion);
        ReflectionTestUtils.setField(etudiant, "id", 3L);
        when(sessionRepository.findByIdForUpdate(2L)).thenReturn(Optional.of(session));
        when(etudiantRepository.findById(3L)).thenReturn(Optional.of(etudiant));
        when(exerciceRepository.existsBySessionIdAndEtudiantId(2L, 3L)).thenReturn(true);

        ApiException exception = assertThrows(ApiException.class,
                () -> service.deposer(new DeposerExerciceRequest(2L, 3L, "https://example.org/work")));

        assertEquals(409, exception.getStatus().value());
    }

    private CoursSession sessionNonCloturee() {
        return sessionNonCloturee(new Promotion("Promotion A"));
    }

    private CoursSession sessionNonCloturee(Promotion promotion) {
        return new CoursSession("Cours", promotion, "ABC234", Instant.parse("2026-01-01T10:00:00Z"),
                Instant.parse("2026-01-01T10:15:00Z"));
    }
}
