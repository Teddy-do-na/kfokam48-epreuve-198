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
@Table(name = "presence")
public class Presence {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "session_id", nullable = false)
    private CoursSession session;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "etudiant_id", nullable = false)
    private Etudiant etudiant;

    @Column(nullable = false, length = 20)
    private String source;

    protected Presence() {
    }

    public Presence(CoursSession session, Etudiant etudiant, String source) {
        this.session = session;
        this.etudiant = etudiant;
        this.source = source;
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

    public String getSource() {
        return source;
    }
}
