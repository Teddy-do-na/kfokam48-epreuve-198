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
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "session")
public class CoursSession {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String titre;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "promotion_id", nullable = false)
    private Promotion promotion;

    @Column(nullable = false, unique = true, length = 12)
    private String code;

    @Column(name = "ouverture_at", nullable = false)
    private Instant ouvertureAt;

    @Column(name = "expiration_at", nullable = false)
    private Instant expirationAt;

    @Column(nullable = false)
    private boolean cloturee;

    @Column(name = "cloturee_at")
    private Instant clotureeAt;

    @jakarta.persistence.OneToMany(mappedBy = "session")
    private List<Presence> presences = new ArrayList<>();

    @jakarta.persistence.OneToMany(mappedBy = "session")
    private List<Exercice> exercices = new ArrayList<>();

    protected CoursSession() {
    }

    public CoursSession(String titre, Promotion promotion, String code, Instant ouvertureAt, Instant expirationAt) {
        this.titre = titre;
        this.promotion = promotion;
        this.code = code;
        this.ouvertureAt = ouvertureAt;
        this.expirationAt = expirationAt;
        this.cloturee = false;
    }

    public Long getId() {
        return id;
    }

    public String getTitre() {
        return titre;
    }

    public Promotion getPromotion() {
        return promotion;
    }

    public String getCode() {
        return code;
    }

    public Instant getOuvertureAt() {
        return ouvertureAt;
    }

    public Instant getExpirationAt() {
        return expirationAt;
    }

    public boolean isCloturee() {
        return cloturee;
    }

    public Instant getClotureeAt() {
        return clotureeAt;
    }

    public void cloturer(Instant instant) {
        if (!cloturee) {
            cloturee = true;
            clotureeAt = instant;
        }
    }

    public List<Presence> getPresences() {
        return presences;
    }

    public List<Exercice> getExercices() {
        return exercices;
    }
}
