package com.kfokam48.backend.repository;

import com.kfokam48.backend.entity.Presence;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PresenceRepository extends JpaRepository<Presence, Long> {
    boolean existsBySessionIdAndEtudiantId(Long sessionId, Long etudiantId);
}
