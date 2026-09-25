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
@Table(name = "tentative_code")
public class TentativeCode {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id")
    private CoursSession session;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "etudiant_id", nullable = false)
    private Etudiant etudiant;

    @Column(nullable = false)
    private boolean reussie;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected TentativeCode() {
    }

    public TentativeCode(Etudiant etudiant, Instant createdAt) {
        this.etudiant = etudiant;
        this.createdAt = createdAt;
        this.reussie = false;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
