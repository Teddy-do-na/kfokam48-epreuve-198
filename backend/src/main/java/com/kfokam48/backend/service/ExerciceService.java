package com.kfokam48.backend.service;

import com.kfokam48.backend.dto.DeposerExerciceRequest;
import com.kfokam48.backend.dto.ExerciceResponse;
import com.kfokam48.backend.entity.CoursSession;
import com.kfokam48.backend.entity.Etudiant;
import com.kfokam48.backend.entity.Exercice;
import com.kfokam48.backend.exception.ApiException;
import com.kfokam48.backend.repository.CoursSessionRepository;
import com.kfokam48.backend.repository.EtudiantRepository;
import com.kfokam48.backend.repository.ExerciceRepository;
import com.kfokam48.backend.repository.RelectureRepository;
import java.net.URI;
import java.time.Clock;
import java.time.Instant;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ExerciceService {
    private final CoursSessionRepository sessionRepository;
    private final EtudiantRepository etudiantRepository;
    private final ExerciceRepository exerciceRepository;
    private final RelectureRepository relectureRepository;
    private final RelecteurAssignmentService assignmentService;
    private final Clock clock;

    public ExerciceService(CoursSessionRepository sessionRepository, EtudiantRepository etudiantRepository,
                           ExerciceRepository exerciceRepository, RelectureRepository relectureRepository,
                           RelecteurAssignmentService assignmentService, Clock clock) {
        this.sessionRepository = sessionRepository;
        this.etudiantRepository = etudiantRepository;
        this.exerciceRepository = exerciceRepository;
        this.relectureRepository = relectureRepository;
        this.assignmentService = assignmentService;
        this.clock = clock;
    }

    @Transactional
    public ExerciceResponse deposer(DeposerExerciceRequest request) {
        CoursSession session = sessionRepository.findById(request.sessionId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "SESSION_INTROUVABLE", "La session demandée est introuvable."));
        Etudiant etudiant = etudiantRepository.findById(request.etudiantId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "ETUDIANT_INTROUVABLE", "L’étudiant demandé est introuvable."));
        if (session.isCloturee()) {
            throw new ApiException(HttpStatus.CONFLICT, "SESSION_CLOTUREE", "La session est clôturée et n’accepte plus de dépôt.");
        }
        if (!session.getPromotion().getId().equals(etudiant.getPromotion().getId())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "PROMOTION_INCORRECTE", "Cet étudiant ne fait pas partie de la promotion de la session.");
        }
        validerLien(request.lien());
        if (exerciceRepository.existsBySessionIdAndEtudiantId(request.sessionId(), request.etudiantId())) {
            throw new ApiException(HttpStatus.CONFLICT, "EXERCICE_DEJA_DEPOSE", "Un exercice a déjà été déposé pour cette session.");
        }
        Instant now = clock.instant();
        Exercice exercice = exerciceRepository.save(new Exercice(session, etudiant, request.lien().trim(), now));
        assignmentService.assigner(exercice);
        return new ExerciceResponse(exercice.getId(), exercice.getStatut());
    }

    @Transactional
    public ExerciceResponse remplacer(Long exerciceId, Long etudiantId, String lien) {
        Exercice exercice = exerciceRepository.findByIdAndEtudiantId(exerciceId, etudiantId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "EXERCICE_INTROUVABLE", "L’exercice demandé est introuvable."));
        if (exercice.getSession().isCloturee()) {
            throw new ApiException(HttpStatus.CONFLICT, "SESSION_CLOTUREE", "La session est clôturée et ne permet plus de modification.");
        }
        validerLien(lien);
        if (relectureRepository.existsByExerciceIdAndStatut(exerciceId, "RENDUE")) {
            throw new ApiException(HttpStatus.CONFLICT, "RELECTURE_DEJA_RENDUE", "Le lien ne peut plus être remplacé après une relecture rendue.");
        }
        exercice.remplacerLien(lien.trim(), clock.instant());
        return new ExerciceResponse(exercice.getId(), exercice.getStatut());
    }

    private void validerLien(String lien) {
        try {
            URI uri = URI.create(lien.trim());
            if (!"https".equalsIgnoreCase(uri.getScheme()) || uri.getHost() == null || uri.getUserInfo() != null) {
                throw new IllegalArgumentException();
            }
        } catch (IllegalArgumentException exception) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "LIEN_INVALIDE", "Le lien doit être une adresse HTTPS valide.");
        }
    }
}
