# D3 — Séquence : marquer sa présence
```mermaid
sequenceDiagram
    participant E as Étudiant
    participant F as Front
    participant API as PresenceController
    participant S as PresenceService
    participant DB as PostgreSQL

    E->>F: saisit le code
    F->>API: POST /api/presences
    API->>S: marquer(code, etudiantId)
    S->>DB: SELECT ... FOR UPDATE sur la session (RG14, sérialise les écritures concurrentes)
    alt code inconnu (EF3)
        S->>DB: comptabilise l'échec (session optionnelle, V3)
        S-->>API: ApiException
        API-->>F: 400 { code: "CODE_INCONNU" }
    else code expiré (RG1, EF3)
        S->>DB: comptabilise l'échec
        S-->>API: ApiException
        API-->>F: 410 { code: "CODE_EXPIRE" }
    else déjà présent (EF4)
        S-->>API: ApiException
        API-->>F: 409 { code: "DEJA_PRESENT" }
    else 6e échec dans la fenêtre (EF5, RG3)
        S-->>API: ApiException
        API-->>F: 429 { code: "ETUDIANT_BLOQUE" }
    else cas nominal (EF2)
        S->>DB: INSERT presence (horodatée, même transaction)
        S->>DB: INSERT relecture si exercice en attente (RG5, horodatée)
        S-->>API: Presence
        API-->>F: 201 { id, sessionId, etudiantId, source }
    end
    Note over S,DB: L'assignation ne peut plus annuler la présence : horodatages systématiques<br/>de la relecture + verrou de session (RG14). Aucun 500 contractuel sur ce parcours.
```
