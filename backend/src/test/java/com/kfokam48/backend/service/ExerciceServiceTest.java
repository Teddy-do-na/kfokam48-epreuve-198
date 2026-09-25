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
class ExerciceServiceTest {
    @Mock private CoursSessionRepository sessionRepository;
    @Mock private EtudiantRepository etudiantRepository;
    @Mock private ExerciceRepository exerciceRepository;
    private ExerciceService service;

    @BeforeEach
    void setUp() {
        service = new ExerciceService(sessionRepository, etudiantRepository, exerciceRepository,
                Clock.fixed(Instant.parse("2026-01-01T10:30:00Z"), ZoneOffset.UTC));
    }

    @Test
    void accepteUnDepotsApresExpirationSiLaSessionNestPasCloturee() {
        Promotion promotion = new Promotion("Promotion A");
        CoursSession session = new CoursSession("Cours", promotion, "ABC234",
                Instant.parse("2026-01-01T10:00:00Z"), Instant.parse("2026-01-01T10:15:00Z"));
        Etudiant etudiant = new Etudiant("Étudiant", promotion);
        when(sessionRepository.findById(2L)).thenReturn(Optional.of(session));
        when(etudiantRepository.findById(3L)).thenReturn(Optional.of(etudiant));
        when(exerciceRepository.existsBySessionIdAndEtudiantId(2L, 3L)).thenReturn(false);
        when(exerciceRepository.save(any(Exercice.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = service.deposer(new DeposerExerciceRequest(2L, 3L, "https://example.org/work"));

        assertEquals("EN_ATTENTE_RELECTURE", response.statut());
    }

    @Test
    void refuseUnLienHttp() {
        when(sessionRepository.findById(2L)).thenReturn(Optional.of(sessionNonCloturee()));
        when(etudiantRepository.findById(3L)).thenReturn(Optional.of(new Etudiant("Étudiant", new Promotion("Promotion A"))));

        ApiException exception = assertThrows(ApiException.class,
                () -> service.deposer(new DeposerExerciceRequest(2L, 3L, "http://example.org/work")));

        assertEquals("LIEN_INVALIDE", exception.getCode());
    }

    @Test
    void refuseUnSecondDepot() {
        Promotion promotion = new Promotion("Promotion A");
        when(sessionRepository.findById(2L)).thenReturn(Optional.of(sessionNonCloturee(promotion)));
        when(etudiantRepository.findById(3L)).thenReturn(Optional.of(new Etudiant("Étudiant", promotion)));
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
