package com.kfokam48.backend.service;

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

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void enregistrerEchec(Etudiant etudiant, Instant instant) {
        tentativeCodeRepository.save(new TentativeCode(etudiant, instant));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void reinitialiser(Long etudiantId, Instant instant) {
        tentativeCodeRepository.supprimerAvant(etudiantId, instant);
    }
}
