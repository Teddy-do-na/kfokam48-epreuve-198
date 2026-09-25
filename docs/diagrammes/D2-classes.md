# D2 — Modèle de données
```mermaid
erDiagram
    PROMOTION ||--o{ ETUDIANT : compte
    PROMOTION ||--o{ SESSION : concerne
    SESSION ||--o{ PRESENCE : genere
    SESSION ||--o{ EXERCICE : recoit
    ETUDIANT ||--o{ PRESENCE : marque
    ETUDIANT ||--o{ EXERCICE : depose
    EXERCICE ||--o| RELECTURE : "a au plus une"
    ETUDIANT ||--o{ RELECTURE : "relit (relecteur)"

    PROMOTION {
        bigint id PK
        string nom
    }
    ETUDIANT {
        bigint id PK
        string nom
        bigint promotion_id FK
    }
    SESSION {
        bigint id PK
        string titre
        bigint promotion_id FK
        string code
        timestamp ouverture_at
        timestamp expiration_at
        boolean cloturee
        timestamp cloturee_at
    }
    PRESENCE {
        bigint id PK
        bigint session_id FK
        bigint etudiant_id FK
        string source
    }
    EXERCICE {
        bigint id PK
        bigint session_id FK
        bigint etudiant_id FK
        string lien
        string statut
    }
    RELECTURE {
        bigint id PK
        bigint exercice_id FK
        bigint relecteur_id FK
        int note
        string commentaire
        string statut
    }