package com.kfokam48.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import com.kfokam48.backend.entity.CoursSession;
import com.kfokam48.backend.entity.Promotion;
import com.kfokam48.backend.exception.ApiException;
import com.kfokam48.backend.repository.CoursSessionRepository;
import com.kfokam48.backend.repository.EtudiantRepository;
import com.kfokam48.backend.repository.PresenceRepository;
import com.kfokam48.backend.repository.TentativeCodeRepository;
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
class PresenceServiceTest {
    @Mock
    private CoursSessionRepository sessionRepository;
    @Mock
    private EtudiantRepository etudiantRepository;
    @Mock
    private PresenceRepository presenceRepository;
    @Mock
    private TentativeCodeRepository tentativeCodeRepository;
    private PresenceService service;

    @BeforeEach
    void setUp() {
        service = new PresenceService(sessionRepository, etudiantRepository, presenceRepository, tentativeCodeRepository,
                Clock.fixed(Instant.parse("2026-01-01T10:15:00Z"), ZoneOffset.UTC));
    }

    @Test
    void refuseUnCodeExpireExactementALaDateDExpiration() {
        CoursSession session = session(Instant.parse("2026-01-01T10:15:00Z"));
        when(sessionRepository.findByCode("ABC234")).thenReturn(Optional.of(session));

        ApiException exception = assertThrows(ApiException.class, () -> service.verifierCodeActif("abc234"));

        assertEquals(410, exception.getStatus().value());
        assertEquals("CODE_EXPIRE", exception.getCode());
    }

    @Test
    void refuseUnCodeInconnu() {
        when(sessionRepository.findByCode("ABC234")).thenReturn(Optional.empty());

        ApiException exception = assertThrows(ApiException.class, () -> service.verifierCodeActif("abc234"));

        assertEquals(400, exception.getStatus().value());
        assertEquals("CODE_INCONNU", exception.getCode());
    }

    private CoursSession session(Instant expirationAt) {
        return new CoursSession("Cours Java", new Promotion("Promotion A"), "ABC234",
                expirationAt.minusSeconds(900), expirationAt);
    }
}
