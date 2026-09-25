package com.kfokam48.backend.repository;

import com.kfokam48.backend.dto.TableauEtudiantResponse;
import com.kfokam48.backend.entity.Etudiant;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EtudiantRepository extends JpaRepository<Etudiant, Long> {
    @Query("select new com.kfokam48.backend.dto.TableauEtudiantResponse(e.id, e.nom, " +
            "(select count(p) from Presence p where p.etudiant = e and p.session.promotion.id = :promotionId), " +
            "(select count(x) from Exercice x where x.etudiant = e and x.session.promotion.id = :promotionId), " +
            "(select avg(r.note) from Relecture r where r.exercice.etudiant = e and r.exercice.session.promotion.id = :promotionId), " +
            "(select count(x) from Exercice x where x.etudiant = e and x.session.promotion.id = :promotionId " +
            "and not exists (select r from Relecture r where r.exercice = x and r.statut = 'RENDUE'))) " +
            "from Etudiant e where e.promotion.id = :promotionId order by e.id")
    List<TableauEtudiantResponse> findTableauByPromotionId(@Param("promotionId") Long promotionId);

    @Query("select count(x) from Exercice x where x.etudiant.id = :etudiantId and x.session.promotion.id = :promotionId " +
            "and not exists (select r from Relecture r where r.exercice = x)")
    long countExercicesSansRelecture(@Param("etudiantId") Long etudiantId, @Param("promotionId") Long promotionId);
}
