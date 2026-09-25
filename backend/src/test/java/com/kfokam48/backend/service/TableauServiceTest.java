package com.kfokam48.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.kfokam48.backend.dto.TableauEtudiantResponse;
import com.kfokam48.backend.exception.ApiException;
import com.kfokam48.backend.repository.EtudiantRepository;
import com.kfokam48.backend.repository.PromotionRepository;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TableauServiceTest {
    @Mock private PromotionRepository promotionRepository;
    @Mock private EtudiantRepository etudiantRepository;

    @Test
    void retourneLeTableauDeLaPromotion() {
        var expected = List.of(new TableauEtudiantResponse(9L, "Ada", 2L, 1L, 18.0, 0L));
        when(promotionRepository.existsById(3L)).thenReturn(true);
        when(etudiantRepository.findTableauByPromotionId(3L)).thenReturn(expected);

        var result = new TableauService(promotionRepository, etudiantRepository).consulter(3L);

        assertEquals(expected, result);
    }

    @Test
    void compteUnExerciceSansRelectureCommeUneAssignationEnAttente() {
        when(promotionRepository.existsById(3L)).thenReturn(true);
        when(etudiantRepository.findTableauByPromotionId(3L))
                .thenReturn(List.of(new TableauEtudiantResponse(9L, "Ada", 1L, 1L, null, 1L, 1L)));

        var result = new TableauService(promotionRepository, etudiantRepository).consulter(3L);

        assertEquals(1L, result.get(0).relecturesEnAttente());
        assertEquals(1L, result.get(0).exercicesSansAssignation());
    }

    @Test
    void refuseUnePromotionInexistante() {
        when(promotionRepository.existsById(99L)).thenReturn(false);

        ApiException exception = assertThrows(ApiException.class,
                () -> new TableauService(promotionRepository, etudiantRepository).consulter(99L));

        assertEquals(404, exception.getStatus().value());
        assertEquals("PROMOTION_INTROUVABLE", exception.getCode());
        verify(etudiantRepository, org.mockito.Mockito.never()).findTableauByPromotionId(99L);
    }
}
