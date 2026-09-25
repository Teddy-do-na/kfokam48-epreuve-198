package com.kfokam48.backend.service;

import com.kfokam48.backend.entity.CoursSession;
import com.kfokam48.backend.entity.Etudiant;
import com.kfokam48.backend.entity.Exercice;
import com.kfokam48.backend.entity.Relecture;
import com.kfokam48.backend.repository.RelectureRepository;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RelecteurAssignmentService {
    private final RelectureRepository relectureRepository;
    private final SecureRandom random;

    public RelecteurAssignmentService(RelectureRepository relectureRepository) {
        this(relectureRepository, new SecureRandom());
    }

    RelecteurAssignmentService(RelectureRepository relectureRepository, SecureRandom random) {
        this.relectureRepository = relectureRepository;
        this.random = random;
    }

    @Transactional
    public void assigner(Exercice exercice) {
        if (relectureRepository.existsByExerciceId(exercice.getId())) {
            return;
        }
        CoursSession session = exercice.getSession();
        List<Etudiant> candidats = new ArrayList<>();
        for (var presence : session.getPresences()) {
            Etudiant candidate = presence.getEtudiant();
            if (!candidate.getId().equals(exercice.getEtudiant().getId())) {
                candidats.add(candidate);
            }
        }
        if (!candidats.isEmpty()) {
            relectureRepository.save(new Relecture(exercice, candidats.get(random.nextInt(candidats.size()))));
        }
    }

    @Transactional
    public void reassignerEnAttente(CoursSession session) {
        session.getPresences();
        for (Exercice exercice : session.getExercices()) {
            assigner(exercice);
        }
    }
}
