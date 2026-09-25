package com.kfokam48.backend.service;

import com.kfokam48.backend.dto.PresenceResponse;
import com.kfokam48.backend.entity.CoursSession;
import com.kfokam48.backend.entity.Etudiant;
import com.kfokam48.backend.entity.Presence;
import com.kfokam48.backend.exception.ApiException;
import com.kfokam48.backend.repository.CoursSessionRepository;
import com.kfokam48.backend.repository.EtudiantRepository;
import com.kfokam48.backend.repository.PresenceRepository;
import java.time.Clock;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PresenceService {
    private final CoursSessionRepository sessionRepository;
    private final EtudiantRepository etudiantRepository;
    private final PresenceRepository presenceRepository;
    private final Clock clock;

    public PresenceService(CoursSessionRepository sessionRepository, EtudiantRepository etudiantRepository,
                           PresenceRepository presenceRepository, Clock clock) {
        this.sessionRepository = sessionRepository;
        this.etudiantRepository = etudiantRepository;
        this.presenceRepository = presenceRepository;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public CoursSession verifierCodeActif(String code) {
        CoursSession session = sessionRepository.findByCode(code.trim().toUpperCase())
                .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "CODE_INCONNU", "Le code de présence est inconnu."));
        if (!clock.instant().isBefore(session.getExpirationAt())) {
            throw new ApiException(HttpStatus.GONE, "CODE_EXPIRE", "Le code de présence a expiré.");
        }
        return session;
    }

    @Transactional
    public PresenceResponse marquer(String code, Long etudiantId) {
        CoursSession session = verifierCodeActif(code);
        Etudiant etudiant = etudiantRepository.findById(etudiantId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "ETUDIANT_INTROUVABLE", "L’étudiant demandé est introuvable."));
        if (!session.getPromotion().getId().equals(etudiant.getPromotion().getId())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "PROMOTION_INCORRECTE", "Cet étudiant ne fait pas partie de la promotion de la session.");
        }
        if (presenceRepository.existsBySessionIdAndEtudiantId(session.getId(), etudiantId)) {
            throw new ApiException(HttpStatus.CONFLICT, "DEJA_PRESENT", "La présence de cet étudiant est déjà enregistrée.");
        }
        Presence saved = presenceRepository.save(new Presence(session, etudiant, "ETUDIANT"));
        return new PresenceResponse(saved.getId(), session.getId(), etudiantId, saved.getSource());
    }
}
