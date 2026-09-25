package com.kfokam48.backend.service;

import com.kfokam48.backend.dto.TableauEtudiantResponse;
import com.kfokam48.backend.exception.ApiException;
import com.kfokam48.backend.repository.EtudiantRepository;
import com.kfokam48.backend.repository.PromotionRepository;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TableauService {
    private final PromotionRepository promotionRepository;
    private final EtudiantRepository etudiantRepository;

    public TableauService(PromotionRepository promotionRepository, EtudiantRepository etudiantRepository) {
        this.promotionRepository = promotionRepository;
        this.etudiantRepository = etudiantRepository;
    }

    @Transactional(readOnly = true)
    public List<TableauEtudiantResponse> consulter(Long promotionId) {
        if (!promotionRepository.existsById(promotionId)) {
            throw new ApiException(HttpStatus.NOT_FOUND, "PROMOTION_INTROUVABLE", "La promotion demandée est introuvable.");
        }
        return etudiantRepository.findTableauByPromotionId(promotionId).stream()
                .map(etudiant -> new TableauEtudiantResponse(etudiant.etudiantId(), etudiant.nom(), etudiant.presences(),
                        etudiant.exercicesDeposes(), etudiant.moyenne(),
                        etudiant.relecturesEnAttente() + etudiantRepository.countExercicesSansRelecture(etudiant.etudiantId(), promotionId)))
                .toList();
    }
}
