package com.kfokam48.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import com.kfokam48.backend.entity.Etudiant;
import com.kfokam48.backend.entity.Promotion;
import com.kfokam48.backend.entity.TentativeCode;
import com.kfokam48.backend.exception.ApiException;
import com.kfokam48.backend.repository.CoursSessionRepository;
import com.kfokam48.backend.repository.EtudiantRepository;
import com.kfokam48.backend.repository.PresenceRepository;
import com.kfokam48.backend.repository.TentativeCodeRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PresenceLockoutTest {
    @Mock private CoursSessionRepository sessionRepository;
    @Mock private EtudiantRepository etudiantRepository;
    @Mock private PresenceRepository presenceRepository;
    @Mock private TentativeCodeRepository tentativeCodeRepository;
    @Mock private TentativeCodeService tentativeCodeService;
    @Mock private RelecteurAssignmentService assignmentService;
    private PresenceService service;

    @BeforeEach
    void setUp() {
        service = new PresenceService(sessionRepository, etudiantRepository, presenceRepository, tentativeCodeRepository,
                tentativeCodeService, assignmentService, Clock.fixed(Instant.parse("2026-01-01T10:01:00Z"), ZoneOffset.UTC));
    }

    @Test
    void bloqueLetudiantApresCinqEchecsPendantDeuxMinutes() {
        Etudiant student = new Etudiant("Étudiant A", new Promotion("Promotion A"));
        List<TentativeCode> failures = java.util.stream.IntStream.range(0, 5)
                .mapToObj(index -> new TentativeCode(student, Instant.parse("2026-01-01T10:00:00Z").plusSeconds(index)))
                .toList();
        when(etudiantRepository.findById(1L)).thenReturn(Optional.of(student));
        when(tentativeCodeRepository.findByEtudiantIdAndCreatedAtAfterOrderByCreatedAtDesc(1L,
                Instant.parse("2025-12-31T23:59:00Z"))).thenReturn(failures);

        ApiException exception = assertThrows(ApiException.class, () -> service.marquer("ABC234", 1L));

        assertEquals(429, exception.getStatus().value());
        assertEquals("ETUDIANT_BLOQUE", exception.getCode());
        assertEquals(59L, exception.getRetryAfterSeconds());
    }
}
