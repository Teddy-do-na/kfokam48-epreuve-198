# D3 — Séquence : marquer sa présence
```mermaid
sequenceDiagram
    participant E as Étudiant
    participant F as Front
    participant API as PresenceController
    participant S as PresenceService

    E->>F: saisit le code
    F->>API: POST /api/presences
    API->>S: enregistrer(code, etudiantId)
    alt code expiré (RG1, EF3)
        S-->>API: CodeExpireException
        API-->>F: 410 { code: "CODE_EXPIRE" }
    else déjà présent (EF4)
        S-->>API: DejaPresentException
        API-->>F: 409 { code: "DEJA_PRESENT" }
    else cas nominal (EF2)
        S-->>API: Presence
        API-->>F: 201 { id, sessionId, etudiantId, source }
    end
```