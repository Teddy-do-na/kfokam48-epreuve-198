package com.kfokam48.backend.service;

import com.kfokam48.backend.dto.OuvrirSessionRequest;
import com.kfokam48.backend.dto.SessionResponse;
import com.kfokam48.backend.entity.CoursSession;
import com.kfokam48.backend.entity.Promotion;
import com.kfokam48.backend.exception.ApiException;
import com.kfokam48.backend.repository.CoursSessionRepository;
import com.kfokam48.backend.repository.PromotionRepository;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SessionService {
    private static final Duration CODE_VALIDITY = Duration.ofMinutes(15);
    private static final char[] ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789".toCharArray();
    private final CoursSessionRepository sessionRepository;
    private final PromotionRepository promotionRepository;
    private final Clock clock;
    private final SecureRandom secureRandom;

    public SessionService(CoursSessionRepository sessionRepository, PromotionRepository promotionRepository, Clock clock) {
        this(sessionRepository, promotionRepository, clock, new SecureRandom());
    }

    SessionService(CoursSessionRepository sessionRepository, PromotionRepository promotionRepository, Clock clock, SecureRandom secureRandom) {
        this.sessionRepository = sessionRepository;
        this.promotionRepository = promotionRepository;
        this.clock = clock;
        this.secureRandom = secureRandom;
    }

    @Transactional
    public SessionResponse ouvrir(OuvrirSessionRequest request) {
        Promotion promotion = promotionRepository.findById(request.promotionId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "PROMOTION_INTROUVABLE", "La promotion demandée est introuvable."));
        Instant ouvertureAt = clock.instant();
        CoursSession session = new CoursSession(request.titre().trim(), promotion, genererCode(), ouvertureAt, ouvertureAt.plus(CODE_VALIDITY));
        CoursSession saved = sessionRepository.save(session);
        return new SessionResponse(saved.getId(), saved.getCode(), saved.getOuvertureAt(), saved.getExpirationAt());
    }

    private String genererCode() {
        for (int attempt = 0; attempt < 10; attempt++) {
            StringBuilder code = new StringBuilder(6);
            for (int index = 0; index < 6; index++) {
                code.append(ALPHABET[secureRandom.nextInt(ALPHABET.length)]);
            }
            String candidate = code.toString();
            if (!sessionRepository.existsByCode(candidate)) {
                return candidate;
            }
        }
        throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE, "CODE_INDISPONIBLE", "Impossible de générer un code de présence pour le moment.");
    }
}
