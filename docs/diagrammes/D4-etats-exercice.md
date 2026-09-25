# D4 — États-transitions : cycle de vie d'un exercice (bonus +3)

```mermaid
stateDiagram-v2
    [*] --> Depose : dépôt du lien (EF7)
    Depose --> Depose : lien remplacé (EF8, avant assignation)
    Depose --> EnAttenteRelecture : relecteur assigné (EF10)
    EnAttenteRelecture --> EnAttenteRelecture : lien remplacé (EF8, avant relecture rendue)
    EnAttenteRelecture --> Relu : relecture rendue (EF12)
    Relu --> Relu : note corrigée (EF13, avant clôture)
    Relu --> [*] : session clôturée (EF17)
    EnAttenteRelecture --> [*] : session clôturée sans relecture rendue (reste "en attente" dans le tableau, RG9)
```
