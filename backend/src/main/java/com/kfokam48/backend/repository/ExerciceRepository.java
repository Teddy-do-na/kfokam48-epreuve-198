package com.kfokam48.backend.repository;

import com.kfokam48.backend.entity.Exercice;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExerciceRepository extends JpaRepository<Exercice, Long> {
    boolean existsBySessionIdAndEtudiantId(Long sessionId, Long etudiantId);
    Optional<Exercice> findBySessionIdAndEtudiantId(Long sessionId, Long etudiantId);
}
