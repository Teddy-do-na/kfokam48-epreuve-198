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
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "etudiant")
public class Etudiant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nom;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "promotion_id", nullable = false)
    private Promotion promotion;

    @jakarta.persistence.OneToMany(mappedBy = "etudiant")
    private List<Presence> presences = new ArrayList<>();

    protected Etudiant() {
    }

    public Etudiant(String nom, Promotion promotion) {
        this.nom = nom;
        this.promotion = promotion;
    }

    public Long getId() {
        return id;
    }

    public String getNom() {
        return nom;
    }

    public Promotion getPromotion() {
        return promotion;
    }

    public List<Presence> getPresences() {
        return presences;
    }
}
