package com.kfokam48.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

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

    protected Relecture() {
    }

    public Relecture(Exercice exercice, Etudiant relecteur) {
        this.exercice = exercice;
        this.relecteur = relecteur;
        this.statut = "EN_ATTENTE";
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
