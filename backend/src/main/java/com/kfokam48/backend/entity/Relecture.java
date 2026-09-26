package com.kfokam48.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "relecture")
public class Relecture {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "exercice_id", nullable = false, unique = true)
    private Exercice exercice;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "relecteur_id", nullable = false)
    private Etudiant relecteur;

    @Column
    private Short note;

    @Column(columnDefinition = "text")
    private String commentaire;

    @Column(nullable = false, length = 20)
    private String statut;

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    protected Relecture() {
    }

    public Relecture(Exercice exercice, Etudiant relecteur) {
        this.exercice = exercice;
        this.relecteur = relecteur;
        this.statut = "EN_ATTENTE";
    }

    /**
     * ISSUE 18 : {@code relecture.created_at / updated_at} sont NOT NULL depuis V1. Sans cet
     * horodatage, la première assignation lève un 23502 qui annule la transaction de présence
     * en cours et fait disparaître l'étudiant du tableau du formateur.
     */
    @PrePersist
    void horodaterAvantEnregistrement() {
        Instant maintenant = Instant.now();
        if (this.createdAt == null) {
            this.createdAt = maintenant;
        }
        if (this.updatedAt == null) {
            this.updatedAt = maintenant;
        }
    }

    public void soumettre(short note, String commentaire, Instant now) {
        this.note = note;
        this.commentaire = commentaire;
        this.statut = "RENDUE";
        if (this.createdAt == null) {
            this.createdAt = now;
        }
        this.updatedAt = now;
    }

    public Long getId() {
        return id;
    }

    public Exercice getExercice() {
        return exercice;
    }

    public Etudiant getRelecteur() {
        return relecteur;
    }

    public Short getNote() {
        return note;
    }

    public String getCommentaire() {
        return commentaire;
    }

    public String getStatut() {
        return statut;
    }
}
