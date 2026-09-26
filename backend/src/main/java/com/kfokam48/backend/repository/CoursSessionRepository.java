package com.kfokam48.backend.repository;

import com.kfokam48.backend.entity.CoursSession;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CoursSessionRepository extends JpaRepository<CoursSession, Long> {
    boolean existsByCode(String code);
    Optional<CoursSession> findByCode(String code);

    /**
     * Lecture verrouillée (SELECT ... FOR UPDATE) : sérialise les écritures concurrentes d'une
     * même session — double présence (ISSUE 20) et double assignation d'un exercice
     * (ISSUE 18, UNIQUE exercice_id) — sans jamais produire d'erreur 23502.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from CoursSession s where s.id = :id")
    Optional<CoursSession> findByIdForUpdate(@Param("id") Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from CoursSession s where s.code = :code")
    Optional<CoursSession> findByCodeForUpdate(@Param("code") String code);
}
