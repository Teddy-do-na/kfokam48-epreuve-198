package com.kfokam48.backend.service;

import com.kfokam48.backend.dto.SoumettreRelectureRequest;
import com.kfokam48.backend.entity.Etudiant;
import com.kfokam48.backend.entity.Relecture;
import com.kfokam48.backend.exception.ApiException;
import com.kfokam48.backend.repository.EtudiantRepository;
import com.kfokam48.backend.repository.RelectureRepository;
import java.time.Clock;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RelectureService {
    private final RelectureRepository relectureRepository;
    private final EtudiantRepository etudiantRepository;
    private final Clock clock;

    public RelectureService(RelectureRepository relectureRepository, EtudiantRepository etudiantRepository, Clock clock) {
        this.relectureRepository = relectureRepository;
        this.etudiantRepository = etudiantRepository;
        this.clock = clock;
    }

    @Transactional
    public void soumettre(Long relectureId, SoumettreRelectureRequest request) {
        Relecture relecture = relectureRepository.findById(relectureId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "RELECTURE_INTROUVABLE", "La relecture demandée est introuvable."));
        Etudiant etudiant = etudiantRepository.findById(request.etudiantId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "ETUDIANT_INTROUVABLE", "L’étudiant demandé est introuvable."));
        if (relecture.getExercice().getEtudiant().getId().equals(etudiant.getId())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "AUTO_RELECTURE_INTERDITE", "Un étudiant ne peut pas relire son propre exercice.");
        }
        if (!relecture.getRelecteur().getId().equals(etudiant.getId())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "RELECTEUR_NON_ASSIGNE", "Cet étudiant n’est pas le relecteur assigné.");
        }
        relecture.soumettre(request.note().shortValue(), request.commentaire().trim(), clock.instant());
    }
}
