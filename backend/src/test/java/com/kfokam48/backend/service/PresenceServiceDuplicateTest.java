package com.kfokam48.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import com.kfokam48.backend.entity.CoursSession;
import com.kfokam48.backend.entity.Etudiant;
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
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class PresenceServiceDuplicateTest {
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
                tentativeCodeService, assignmentService, Clock.fixed(Instant.parse("2026-01-01T10:05:00Z"), ZoneOffset.UTC));
    }

    @Test
    void renvoieConflitQuandLetudiantEstDejaPresent() {
        Promotion promotion = new Promotion("Promotion A");
        ReflectionTestUtils.setField(promotion, "id", 5L);
        CoursSession session = new CoursSession("Cours Java", promotion, "ABC234",
                Instant.parse("2026-01-01T10:00:00Z"), Instant.parse("2026-01-01T10:15:00Z"));
        Etudiant etudiant = new Etudiant("Étudiant A", promotion);
        ReflectionTestUtils.setField(session, "id", 2L);
        ReflectionTestUtils.setField(etudiant, "id", 1L);
        when(sessionRepository.findByCode("ABC234")).thenReturn(Optional.of(session));
        when(etudiantRepository.findById(1L)).thenReturn(Optional.of(etudiant));
        when(presenceRepository.existsBySessionIdAndEtudiantId(2L, 1L)).thenReturn(true);

        ApiException exception = assertThrows(ApiException.class, () -> service.marquer("ABC234", 1L));

        assertEquals(409, exception.getStatus().value());
        assertEquals("DEJA_PRESENT", exception.getCode());
    }
}
