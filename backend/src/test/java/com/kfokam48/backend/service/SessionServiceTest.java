package com.kfokam48.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.kfokam48.backend.dto.OuvrirSessionRequest;
import com.kfokam48.backend.entity.CoursSession;
import com.kfokam48.backend.entity.Promotion;
import com.kfokam48.backend.exception.ApiException;
import com.kfokam48.backend.repository.CoursSessionRepository;
import com.kfokam48.backend.repository.PromotionRepository;
import java.security.SecureRandom;
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
class SessionServiceTest {
    @Mock
    private CoursSessionRepository sessionRepository;
    @Mock
    private PromotionRepository promotionRepository;
    private SessionService service;

    @BeforeEach
    void setUp() {
        service = new SessionService(sessionRepository, promotionRepository,
                Clock.fixed(Instant.parse("2026-01-01T10:00:00Z"), ZoneOffset.UTC), new SecureRandom());
    }

    @Test
    void ouvreUneSessionAvecUneExpirationAQuinzeMinutes() {
        Promotion promotion = new Promotion("Promotion A");
        when(promotionRepository.findById(1L)).thenReturn(Optional.of(promotion));
        when(sessionRepository.existsByCode(any())).thenReturn(false);
        when(sessionRepository.save(any(CoursSession.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = service.ouvrir(new OuvrirSessionRequest("Cours Java", 1L));

        assertEquals("2026-01-01T10:00:00Z", response.ouvertureAt().toString());
        assertEquals("2026-01-01T10:15:00Z", response.expirationAt().toString());
        assertEquals(6, response.code().length());
    }

    @Test
    void clotureUneSessionEtRetourneSonHorodatage() {
        CoursSession session = new CoursSession("Cours Java", new Promotion("Promotion A"), "ABC234",
                Instant.parse("2026-01-01T10:00:00Z"), Instant.parse("2026-01-01T10:15:00Z"));
        ReflectionTestUtils.setField(session, "id", 7L);
        when(sessionRepository.findById(7L)).thenReturn(Optional.of(session));
        when(sessionRepository.save(any(CoursSession.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = service.cloturer(7L);

        assertEquals(7L, response.id());
        assertEquals(Instant.parse("2026-01-01T10:00:00Z"), response.clotureeAt());
        assertEquals(true, session.isCloturee());
        verify(sessionRepository).save(session);
    }

    @Test
    void uneSecondeClotureNeRemplacePasLaDateInitiale() {
        CoursSession session = new CoursSession("Cours Java", new Promotion("Promotion A"), "ABC234",
                Instant.parse("2026-01-01T10:00:00Z"), Instant.parse("2026-01-01T10:15:00Z"));
        ReflectionTestUtils.setField(session, "id", 7L);
        Instant premiereCloture = Instant.parse("2026-01-01T09:00:00Z");
        session.cloturer(premiereCloture);
        when(sessionRepository.findById(7L)).thenReturn(Optional.of(session));
        when(sessionRepository.save(any(CoursSession.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = service.cloturer(7L);

        assertEquals(premiereCloture, response.clotureeAt());
    }

    @Test
    void refuseUnePromotionInconnue() {
        when(promotionRepository.findById(99L)).thenReturn(Optional.empty());

        ApiException exception = assertThrows(ApiException.class,
                () -> service.ouvrir(new OuvrirSessionRequest("Cours Java", 99L)));

        assertEquals("PROMOTION_INTROUVABLE", exception.getCode());
    }
}
