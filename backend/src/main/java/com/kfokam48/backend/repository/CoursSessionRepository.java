package com.kfokam48.backend.repository;

import com.kfokam48.backend.entity.CoursSession;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CoursSessionRepository extends JpaRepository<CoursSession, Long> {
    boolean existsByCode(String code);
    Optional<CoursSession> findByCode(String code);
}
