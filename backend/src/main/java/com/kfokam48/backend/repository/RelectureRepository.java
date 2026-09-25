package com.kfokam48.backend.repository;

import com.kfokam48.backend.entity.Relecture;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RelectureRepository extends JpaRepository<Relecture, Long> {
    boolean existsByExerciceIdAndStatut(Long exerciceId, String statut);
}
