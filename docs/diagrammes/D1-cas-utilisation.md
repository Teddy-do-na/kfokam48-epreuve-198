# D1 — Cas d'utilisation

```mermaid
flowchart LR
    Formateur((Formateur))
    Etudiant((Étudiant))
    Relecteur((Relecteur))

    Formateur -->|EF1| UC1[Ouvrir une session]
    Formateur -->|EF17| UC2[Clôturer une session]
    Formateur -->|EF6| UC3[Ajouter une présence manuelle]
    Formateur -->|EF16| UC4[Voir le tableau de suivi]

    Etudiant -->|EF2| UC5[Marquer sa présence]
    Etudiant -->|EF7, EF8| UC6[Déposer / remplacer un exercice]
    Etudiant -->|EF14| UC7[Voir sa note et son commentaire]

    Relecteur -->|EF12, EF13| UC8[Noter et commenter un exercice]
