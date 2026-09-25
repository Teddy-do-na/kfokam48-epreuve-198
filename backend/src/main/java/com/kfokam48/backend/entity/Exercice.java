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
import java.time.Instant;

@Entity
@Table(name = "exercice")
public class Exercice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "session_id", nullable = false)
    private CoursSession session;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "etudiant_id", nullable = false)
    private Etudiant etudiant;

    @Column(nullable = false, length = 2048)
    private String lien;

    @Column(nullable = false, length = 30)
    private String statut;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected Exercice() {
    }

    public Exercice(CoursSession session, Etudiant etudiant, String lien, Instant now) {
        this.session = session;
        this.etudiant = etudiant;
        this.lien = lien;
        this.statut = "EN_ATTENTE_RELECTURE";
        this.createdAt = now;
        this.updatedAt = now;
    }

    public Long getId() {
        return id;
    }

    public CoursSession getSession() {
        return session;
    }

    public Etudiant getEtudiant() {
        return etudiant;
    }

    public String getLien() {
        return lien;
    }

    public String getStatut() {
        return statut;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void remplacerLien(String lien, Instant now) {
        this.lien = lien;
        this.updatedAt = now;
    }

    public void marquerRelu(Instant now) {
        this.statut = "RELU";
        this.updatedAt = now;
    }
}
