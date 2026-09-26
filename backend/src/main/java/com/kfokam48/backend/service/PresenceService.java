package com.kfokam48.backend.service;

import com.kfokam48.backend.dto.PresenceResponse;
import com.kfokam48.backend.entity.CoursSession;
import com.kfokam48.backend.entity.Etudiant;
import com.kfokam48.backend.entity.Presence;
import com.kfokam48.backend.entity.TentativeCode;
import com.kfokam48.backend.exception.ApiException;
import com.kfokam48.backend.repository.CoursSessionRepository;
import com.kfokam48.backend.repository.EtudiantRepository;
import com.kfokam48.backend.repository.PresenceRepository;
import com.kfokam48.backend.repository.TentativeCodeRepository;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PresenceService {
    private static final int MAX_FAILURES = 5;
    private static final Duration BLOCK_DURATION = Duration.ofMinutes(2);
    private final CoursSessionRepository sessionRepository;
    private final EtudiantRepository etudiantRepository;
    private final PresenceRepository presenceRepository;
    private final TentativeCodeRepository tentativeCodeRepository;
    private final TentativeCodeService tentativeCodeService;
    private final RelecteurAssignmentService assignmentService;
    private final Clock clock;

    public PresenceService(CoursSessionRepository sessionRepository, EtudiantRepository etudiantRepository,
                           PresenceRepository presenceRepository, TentativeCodeRepository tentativeCodeRepository,
                           TentativeCodeService tentativeCodeService, RelecteurAssignmentService assignmentService, Clock clock) {
        this.sessionRepository = sessionRepository;
        this.etudiantRepository = etudiantRepository;
        this.presenceRepository = presenceRepository;
        this.tentativeCodeRepository = tentativeCodeRepository;
        this.tentativeCodeService = tentativeCodeService;
        this.assignmentService = assignmentService;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public CoursSession verifierCodeActif(String code) {
        return trouverCodeActif(code);
    }

    @Transactional
    public PresenceResponse ajouterManuellement(Long sessionId, Long etudiantId) {
        // Verrou de session : l'insertion de présence et l'assignation éventuelle sont sérialisées
        CoursSession session = sessionRepository.findByIdForUpdate(sessionId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "SESSION_INTROUVABLE", "La session demandée est introuvable."));
        Etudiant etudiant = etudiantRepository.findById(etudiantId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "ETUDIANT_INTROUVABLE", "L’étudiant demandé est introuvable."));
        if (!session.getPromotion().getId().equals(etudiant.getPromotion().getId())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "PROMOTION_INCORRECTE", "Cet étudiant ne fait pas partie de la promotion de la session.");
        }
        if (presenceRepository.existsBySessionIdAndEtudiantId(sessionId, etudiantId)) {
            throw new ApiException(HttpStatus.CONFLICT, "DEJA_PRESENT", "La présence de cet étudiant est déjà enregistrée.");
        }
        Presence saved = presenceRepository.save(new Presence(session, etudiant, "FORMATEUR"));
        assignmentService.reassignerEnAttente(session);
        return new PresenceResponse(saved.getId(), sessionId, etudiantId, saved.getSource());
    }

    @Transactional
    public PresenceResponse marquer(String code, Long etudiantId) {
        Etudiant etudiant = etudiantRepository.findById(etudiantId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "ETUDIANT_INTROUVABLE", "L’étudiant demandé est introuvable."));
        verifierBlocage(etudiantId);
        CoursSession session = sessionRepository.findByCodeForUpdate(code.trim().toUpperCase()).orElse(null);
        if (session == null || !clock.instant().isBefore(session.getExpirationAt())) {
            tentativeCodeService.enregistrerEchec(etudiant, clock.instant());
            throw new ApiException(session == null ? HttpStatus.BAD_REQUEST : HttpStatus.GONE,
                    session == null ? "CODE_INCONNU" : "CODE_EXPIRE",
                    session == null ? "Le code de présence est inconnu." : "Le code de présence a expiré.");
        }
        if (!session.getPromotion().getId().equals(etudiant.getPromotion().getId())) {
            tentativeCodeService.enregistrerEchec(etudiant, clock.instant());
            throw new ApiException(HttpStatus.FORBIDDEN, "PROMOTION_INCORRECTE", "Cet étudiant ne fait pas partie de la promotion de la session.");
        }
        if (presenceRepository.existsBySessionIdAndEtudiantId(session.getId(), etudiantId)) {
            throw new ApiException(HttpStatus.CONFLICT, "DEJA_PRESENT", "La présence de cet étudiant est déjà enregistrée.");
        }
        tentativeCodeService.reinitialiser(etudiantId, clock.instant());
        Presence saved = presenceRepository.save(new Presence(session, etudiant, "ETUDIANT"));
        assignmentService.reassignerEnAttente(session);
        return new PresenceResponse(saved.getId(), session.getId(), etudiantId, saved.getSource());
    }

    private CoursSession trouverCodeActif(String code) {
        CoursSession session = sessionRepository.findByCode(code.trim().toUpperCase())
                .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "CODE_INCONNU", "Le code de présence est inconnu."));
        if (!clock.instant().isBefore(session.getExpirationAt())) {
            throw new ApiException(HttpStatus.GONE, "CODE_EXPIRE", "Le code de présence a expiré.");
        }
        return session;
    }

    private void verifierBlocage(Long etudiantId) {
        Instant now = clock.instant();
        List<TentativeCode> failures = tentativeCodeRepository.findByEtudiantIdAndCreatedAtAfterOrderByCreatedAtDesc(
                etudiantId, now.minus(BLOCK_DURATION));
        if (failures.size() >= MAX_FAILURES) {
            Instant unblockAt = failures.get(0).getCreatedAt().plus(BLOCK_DURATION);
            if (now.isBefore(unblockAt)) {
                long seconds = Math.max(1, Duration.between(now, unblockAt).toSeconds());
                throw new ApiException(HttpStatus.TOO_MANY_REQUESTS, "ETUDIANT_BLOQUE", "Trop de codes incorrects. Réessayez après le délai de blocage.", seconds);
            }
        }
    }
}
