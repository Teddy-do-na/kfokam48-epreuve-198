package com.kfokam48.backend.service;

import com.kfokam48.backend.entity.CoursSession;
import com.kfokam48.backend.exception.ApiException;
import com.kfokam48.backend.repository.CoursSessionRepository;
import java.time.Clock;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PresenceService {
    private final CoursSessionRepository sessionRepository;
    private final Clock clock;

    public PresenceService(CoursSessionRepository sessionRepository, Clock clock) {
        this.sessionRepository = sessionRepository;
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
}
