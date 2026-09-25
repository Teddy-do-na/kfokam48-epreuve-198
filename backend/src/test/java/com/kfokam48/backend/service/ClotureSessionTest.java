package com.kfokam48.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import com.kfokam48.backend.exception.ApiException;
import com.kfokam48.backend.repository.CoursSessionRepository;
import com.kfokam48.backend.repository.PromotionRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ClotureSessionTest {
    @Mock private CoursSessionRepository sessionRepository;
    @Mock private PromotionRepository promotionRepository;

    @Test
    void renvoie404SiLaSessionNestPasTrouvee() {
        when(sessionRepository.findById(404L)).thenReturn(Optional.empty());
        SessionService service = new SessionService(sessionRepository, promotionRepository,
                Clock.fixed(Instant.parse("2026-01-01T12:00:00Z"), ZoneOffset.UTC));

        ApiException exception = assertThrows(ApiException.class, () -> service.cloturer(404L));

        assertEquals(404, exception.getStatus().value());
        assertEquals("SESSION_INTROUVABLE", exception.getCode());
    }
}
