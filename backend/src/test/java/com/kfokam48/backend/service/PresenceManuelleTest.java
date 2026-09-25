package com.kfokam48.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.kfokam48.backend.entity.CoursSession;
import com.kfokam48.backend.entity.Etudiant;
import com.kfokam48.backend.entity.Promotion;
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
class PresenceManuelleTest {
    @Mock private CoursSessionRepository sessionRepository;
    @Mock private EtudiantRepository etudiantRepository;
    @Mock private PresenceRepository presenceRepository;
    @Mock private TentativeCodeRepository tentativeCodeRepository;
    @Mock private TentativeCodeService tentativeCodeService;
    private PresenceService service;

    @BeforeEach
    void setUp() {
        service = new PresenceService(sessionRepository, etudiantRepository, presenceRepository, tentativeCodeRepository,
                tentativeCodeService, Clock.fixed(Instant.parse("2026-01-01T10:05:00Z"), ZoneOffset.UTC));
    }

    @Test
    void marqueLaPresenceManuelleAvecLaSourceFormateur() {
        Promotion promotion = new Promotion("Promotion A");
        CoursSession session = new CoursSession("Cours Java", promotion, "ABC234",
                Instant.parse("2026-01-01T10:00:00Z"), Instant.parse("2026-01-01T10:15:00Z"));
        Etudiant etudiant = new Etudiant("Étudiant A", promotion);
        when(sessionRepository.findById(2L)).thenReturn(Optional.of(session));
        when(etudiantRepository.findById(1L)).thenReturn(Optional.of(etudiant));
        when(presenceRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var response = service.ajouterManuellement(2L, 1L);

        assertEquals("FORMATEUR", response.source());
        assertEquals(2L, response.sessionId());
    }
}
