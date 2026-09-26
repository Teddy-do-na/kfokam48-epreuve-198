package com.kfokam48.backend.service;

import com.kfokam48.backend.entity.CoursSession;
import com.kfokam48.backend.entity.Etudiant;
import com.kfokam48.backend.entity.TentativeCode;
import com.kfokam48.backend.repository.TentativeCodeRepository;
import java.time.Instant;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TentativeCodeService {
    private final TentativeCodeRepository tentativeCodeRepository;

    public TentativeCodeService(TentativeCodeRepository tentativeCodeRepository) {
        this.tentativeCodeRepository = tentativeCodeRepository;
    }

    /**
     * ISSUE 19 : la session est renseignée quand elle est connue (code expiré, promotion
     * incorrecte). Pour un code totalement inconnu il n'y en a pas — V3 rend la colonne
     * optionnelle — mais l'échec doit rester comptabilisé pour le blocage après 5 essais (EF5).
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void enregistrerEchec(CoursSession session, Etudiant etudiant, Instant instant) {
        tentativeCodeRepository.save(new TentativeCode(session, etudiant, instant));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void reinitialiser(Long etudiantId, Instant instant) {
        tentativeCodeRepository.supprimerAvant(etudiantId, instant);
    }
}
