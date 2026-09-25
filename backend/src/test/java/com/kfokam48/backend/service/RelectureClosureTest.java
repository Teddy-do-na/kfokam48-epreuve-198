package com.kfokam48.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import com.kfokam48.backend.dto.SoumettreRelectureRequest;
import com.kfokam48.backend.entity.CoursSession;
import com.kfokam48.backend.entity.Etudiant;
import com.kfokam48.backend.entity.Exercice;
import com.kfokam48.backend.entity.Promotion;
import com.kfokam48.backend.entity.Relecture;
import com.kfokam48.backend.exception.ApiException;
import com.kfokam48.backend.repository.EtudiantRepository;
import com.kfokam48.backend.repository.RelectureRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RelectureClosureTest {
    @Mock private RelectureRepository relectureRepository;
    @Mock private EtudiantRepository etudiantRepository;

    @Test
    void refuseLaModificationApresCloture() {
        Promotion promotion = new Promotion("Promotion A");
        Etudiant author = new Etudiant("Auteur", promotion);
        Etudiant reviewer = new Etudiant("Relecteur", promotion);
        org.springframework.test.util.ReflectionTestUtils.setField(author, "id", 1L);
        org.springframework.test.util.ReflectionTestUtils.setField(reviewer, "id", 4L);
        CoursSession session = new CoursSession("Cours", promotion, "ABC234", Instant.now(), Instant.now().plusSeconds(900));
        try {
            var field = CoursSession.class.getDeclaredField("cloturee");
            field.setAccessible(true);
            field.setBoolean(session, true);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException(exception);
        }
        Relecture review = new Relecture(new Exercice(session, author, "https://example.org", Instant.now()), reviewer);
        RelectureService service = new RelectureService(relectureRepository, etudiantRepository,
                Clock.fixed(Instant.now(), ZoneOffset.UTC));
        when(relectureRepository.findById(2L)).thenReturn(Optional.of(review));
        when(etudiantRepository.findById(4L)).thenReturn(Optional.of(reviewer));

        ApiException exception = assertThrows(ApiException.class,
                () -> service.soumettre(2L, new SoumettreRelectureRequest(4L, 19, "Correction")));

        assertEquals(409, exception.getStatus().value());
        assertEquals("SESSION_CLOTUREE", exception.getCode());
    }
}
