-- Schéma initial, cohérent avec le contrat d'API (api/contrat.yaml)
-- et le diagramme D2 (docs/diagrammes/D2-classes.md) à maintenir en parallèle.

CREATE TABLE promotion (
    id          BIGSERIAL PRIMARY KEY,
    nom         VARCHAR(255) NOT NULL
);

CREATE TABLE etudiant (
    id              BIGSERIAL PRIMARY KEY,
    nom             VARCHAR(255) NOT NULL,
    promotion_id    BIGINT NOT NULL REFERENCES promotion(id)
);

CREATE TABLE session (
    id              BIGSERIAL PRIMARY KEY,
    titre           VARCHAR(255) NOT NULL,
    promotion_id    BIGINT NOT NULL REFERENCES promotion(id),
    code            VARCHAR(12) NOT NULL UNIQUE,
    ouverture_at    TIMESTAMPTZ NOT NULL,
    expiration_at   TIMESTAMPTZ NOT NULL,
    cloturee        BOOLEAN NOT NULL DEFAULT FALSE,
    cloturee_at     TIMESTAMPTZ
);

-- RG12 : source = ETUDIANT | FORMATEUR (ajout manuel, cf. Q14)
CREATE TABLE presence (
    id              BIGSERIAL PRIMARY KEY,
    session_id      BIGINT NOT NULL REFERENCES session(id),
    etudiant_id     BIGINT NOT NULL REFERENCES etudiant(id),
    source          VARCHAR(20) NOT NULL CHECK (source IN ('ETUDIANT', 'FORMATEUR')),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (session_id, etudiant_id)
);

-- RG3 : suivi des tentatives de code pour le blocage après 5 échecs
CREATE TABLE tentative_code (
    id              BIGSERIAL PRIMARY KEY,
    session_id      BIGINT NOT NULL REFERENCES session(id),
    etudiant_id     BIGINT NOT NULL REFERENCES etudiant(id),
    reussie         BOOLEAN NOT NULL,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE exercice (
    id              BIGSERIAL PRIMARY KEY,
    session_id      BIGINT NOT NULL REFERENCES session(id),
    etudiant_id     BIGINT NOT NULL REFERENCES etudiant(id),
    lien            VARCHAR(2048) NOT NULL,
    statut          VARCHAR(30) NOT NULL DEFAULT 'EN_ATTENTE_RELECTURE'
                        CHECK (statut IN ('EN_ATTENTE_RELECTURE', 'RELU')),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (session_id, etudiant_id)
);

-- RG5 : un seul relecteur par exercice (UNIQUE exercice_id)
-- RG4 : relecteur != auteur de l'exercice, contrôlé en base par l'application
CREATE TABLE relecture (
    id              BIGSERIAL PRIMARY KEY,
    exercice_id     BIGINT NOT NULL REFERENCES exercice(id),
    relecteur_id    BIGINT NOT NULL REFERENCES etudiant(id),
    note            SMALLINT CHECK (note BETWEEN 0 AND 20),
    commentaire     TEXT,
    statut          VARCHAR(20) NOT NULL DEFAULT 'EN_ATTENTE'
                        CHECK (statut IN ('EN_ATTENTE', 'RENDUE')),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (exercice_id)
);

CREATE INDEX idx_presence_session ON presence(session_id);
CREATE INDEX idx_exercice_session ON exercice(session_id);
CREATE INDEX idx_relecture_relecteur ON relecture(relecteur_id);
