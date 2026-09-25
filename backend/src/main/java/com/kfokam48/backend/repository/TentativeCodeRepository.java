package com.kfokam48.backend.repository;

import com.kfokam48.backend.entity.TentativeCode;
import java.time.Instant;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TentativeCodeRepository extends JpaRepository<TentativeCode, Long> {
    List<TentativeCode> findByEtudiantIdAndCreatedAtAfterOrderByCreatedAtDesc(Long etudiantId, Instant since);

    @Modifying
    @Query("delete from TentativeCode t where t.etudiant.id = :etudiantId and t.createdAt < :before")
    int supprimerAvant(@Param("etudiantId") Long etudiantId, @Param("before") Instant before);
}
