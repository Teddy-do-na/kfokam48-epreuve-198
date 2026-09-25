# Backlog — issues à créer sur GitHub

Copie chaque bloc "Titre" comme titre d'issue, et le contenu "Description" + "Critères d'acceptation" + "Référence" comme corps. Ajoute un label de priorité (`must` / `should` / `could`).

> **Ajout au cahier des charges avant de créer les issues** : ajoute dans la section 4 (Exigences fonctionnelles) :
> **EF17 | Le formateur clôture une session | Une fois clôturée, aucun dépôt ni aucune note ne peut plus être modifié (409 sur toute tentative) | Must**

---

### 1. [Must] Ouvrir une session et générer un code de présence
**Description** : Le formateur ouvre une session pour une promotion et obtient un code utilisable pendant 15 minutes.
**Critères d'acceptation** :
- `POST /api/sessions` avec `{titre, promotionId}` renvoie 201 et `{id, code, ouvertureAt, expirationAt}`
- `expirationAt` = `ouvertureAt` + 15 min
- Champ manquant → 400
**Référence** : EF1, RG1

---

### 2. [Must] Expirer automatiquement le code de présence
**Description** : Passé le délai, le code ne doit plus permettre de marquer une présence.
**Critères d'acceptation** :
- Un code saisi après `expirationAt` renvoie 410 `CODE_EXPIRE`
**Référence** : EF3, RG1, RG2

---

### 3. [Must] Marquer sa présence avec un code valide
**Description** : Un étudiant saisit le code de la session en cours pour être marqué présent.
**Critères d'acceptation** :
- `POST /api/presences` avec un code valide et non expiré renvoie 201
- La présence apparaît ensuite dans le tableau du formateur
- Code inconnu → 400
**Référence** : EF2

---

### 4. [Must] Refuser une double présence
**Description** : Un étudiant déjà marqué présent sur une session ne peut pas l'être une seconde fois.
**Critères d'acceptation** :
- Une deuxième tentative valide sur la même session renvoie 409 `DEJA_PRESENT`
**Référence** : EF4

---

### 5. [Should] Bloquer un étudiant après 5 échecs de code consécutifs
**Description** : Empêcher le "brute force" du code entre étudiants (cf. Q4).
**Critères d'acceptation** :
- La 6e tentative dans la fenêtre est refusée pendant 2 minutes, même avec un code correct
- Le compteur repart après un succès ou après le délai de blocage
**Référence** : EF5, RG3

---

### 6. [Must] Ajouter une présence manuellement (formateur)
**Description** : Le formateur peut ajouter une présence pour un étudiant ayant eu un souci technique.
**Critères d'acceptation** :
- La présence créée porte `source=FORMATEUR`
- Elle est visiblement distinguée dans le tableau ("ajouté par le formateur")
**Référence** : EF6, RG12

---

### 7. [Must] Déposer le lien de son exercice
**Description** : Un étudiant dépose le lien de son exercice pour une session, même après sa fin programmée.
**Critères d'acceptation** :
- `POST /api/exercices` renvoie 201 avec statut `EN_ATTENTE_RELECTURE`
- Accepté après `expirationAt` tant que la session n'est pas clôturée
- Lien invalide → 400 · dépôt en double → 409
**Référence** : EF7, EF9, RG10

---

### 8. [Should] Remplacer le lien de son exercice
**Description** : Tant que personne n'a commencé à relire, l'étudiant peut corriger son lien.
**Critères d'acceptation** :
- Remplacement accepté si aucune relecture `RENDUE` n'existe pour cet exercice
- Refusé sinon
**Référence** : EF8, RG11

---

### 9. [Must] Assigner un relecteur au hasard parmi les présents
**Description** : Dès qu'un exercice est déposé, le système choisit un relecteur parmi les étudiants présents à la session.
**Critères d'acceptation** :
- Le relecteur assigné ≠ auteur de l'exercice
- Le relecteur assigné fait partie des présents de cette session
- Si aucun autre étudiant présent n'est éligible, l'exercice reste "en attente d'assignation" et est réévalué à chaque nouvelle présence
**Référence** : EF10, RG4, RG5

---

### 10. [Must] Empêcher un étudiant de relire son propre exercice
**Description** : Garde-fou côté API, indépendant de l'assignation.
**Critères d'acceptation** :
- `POST /api/relectures/{id}` par l'auteur de l'exercice renvoie 403
**Référence** : EF11, RG4

---

### 11. [Must] Noter et commenter un exercice (relecture)
**Description** : Le relecteur envoie une note entière de 0 à 20 et un commentaire.
**Critères d'acceptation** :
- Note entière 0–20 acceptée → 200
- Note hors bornes ou non entière → 400
**Référence** : EF12, RG7

---

### 12. [Must] Corriger sa note tant que la session n'est pas clôturée
**Description** : Le relecteur peut renvoyer une nouvelle note/commentaire avant la clôture de la session par le formateur.
**Critères d'acceptation** :
- Nouvelle soumission avant clôture → 200, remplace la précédente
- Toute soumission après clôture → 409
**Référence** : EF13, RG8, RG13, EF17

---

### 13. [Must] Masquer l'identité du relecteur à l'étudiant relu
**Description** : L'étudiant voit sa note et le commentaire, jamais qui l'a noté.
**Critères d'acceptation** :
- Aucune donnée d'identification du relecteur dans la réponse consultée par l'étudiant
**Référence** : EF14, RG6

---

### 14. [Must] Afficher le tableau de suivi du formateur
**Description** : Vue consolidée par étudiant pour une promotion.
**Critères d'acceptation** :
- `GET /api/tableau?promotionId=` renvoie pour chaque étudiant : présences, exercices déposés, moyenne, relectures en attente
- Un exercice sans relecture `RENDUE` compte dans `relecturesEnAttente`
- promotion inconnue → 404
**Référence** : EF15, EF16, RG9

---

### 15. [Must] Clôturer une session
**Description** : Action explicite du formateur qui ferme définitivement dépôts et notation pour une session.
**Critères d'acceptation** :
- Après clôture : tout dépôt ou toute note renvoie 409
- La clôture est horodatée (`clotureeAt`)
**Référence** : EF17, RG13

---

### 16. [Could] Gérer le cas d'une session avec un seul étudiant présent
**Description** : Aucun relecteur éligible ne peut être assigné sans violer RG4.
**Critères d'acceptation** :
- L'exercice reste marqué "en attente d'assignation", visible comme tel dans le tableau
- Dès qu'un second étudiant devient présent sur la même session, l'assignation est retentée
**Référence** : Section 7 (hypothèse), RG4, RG5

---

### 17. [Should] Charger des données de démonstration au démarrage
**Description** : Le correcteur doit pouvoir ouvrir une application déjà peuplée.
**Critères d'acceptation** :
- Une migration Flyway (`V2__donnees_demo.sql`) crée une promotion, des étudiants, une session ouverte
**Référence** : Contraintes techniques, section 8

---

### 18. [Must] Ne pas perdre une présence quand l'assignation d'un relecteur échoue
**Description** : Rapport client (enveloppe étape 3) : « J'ai ouvert une session ce matin avec deux étudiants côte à côte. Ils ont tapé le code presque en même temps et il n'y en a qu'un seul qui apparaît dans ma liste. J'ai réessayé une fois, cette fois les deux sont passés. »
**Traduction technique** : `PresenceService.marquer` appelle `RelecteurAssignmentService.reassignerEnAttente` dans la **même transaction**. L'insertion de la `Relecture` échoue (`created_at` viole la contrainte NOT NULL, code SQL `23502` : `Relecture(exercice, relecteur)` n'initialise ni `created_at` ni `updated_at`), toute la transaction de présence est alors annulée : l'étudiant disparaît du tableau et l'API répond 500 `ERREUR_INTERNE`.
**Reproduction (déterministe)** :
1. Ouvrir une session (`POST /api/sessions`) puis marquer la présence de l'étudiant 1 (`POST /api/presences` → 201)
2. Seul présent, l'étudiant 1 dépose son exercice (`POST /api/exercices` → 201, aucun relecteur éligible : la relecture reste « en attente d'assignation »)
3. Marquer la présence de l'étudiant 2 avec le même code → **500 `ERREUR_INTERNE`** au lieu d'un 201
4. `GET /api/tableau?promotionId=` : aucune présence ajoutée pour l'étudiant 2 ; chaque nouvelle tentative renvoie encore 500
**Variante concurrente** : deux étudiants valident le même code en même temps alors qu'un exercice est en attente → même symptôme. Une fois les horodatages corrigés, la course sur `UNIQUE (exercice_id)` (aucun verrou avant l'insertion) continue de faire perdre une présence sur deux.
**Critères d'acceptation** :
- Le scénario ci-dessus répond 201 et la présence apparaît dans le tableau du formateur, sans réessai
- Deux soumissions simultanées du même code pour deux étudiants différents enregistrent les deux présences
- Un test d'intégration échoue avant le correctif et passe après
- V1 et V2 ne sont pas modifiées (aucune migration réécrite en place)
**Référence** : EF2, EF10, RG4, RG5 — rapport client enveloppe étape 3

---

### 19. [Should] Renvoyer 400/410 sur un code inconnu ou expiré au lieu de 500
**Description** : Défaut annexe repéré pendant la reproduction de l'issue 18 : `TentativeCode(etudiant, instant)` ne renseigne jamais `session`, or `tentative_code.session_id` est NOT NULL en base. Chaque échec de code plante donc (`23502`) : `POST /api/presences` avec un code inconnu renvoie 500 au lieu de 400 `CODE_INCONNU`, et le compteur de blocage après 5 échecs (EF5/RG3) ne se remplit jamais.
**Critères d'acceptation** :
- Code inconnu → 400 `CODE_INCONNU` · code expiré → 410 `CODE_EXPIRE`
- La 6e tentative dans la fenêtre de 2 minutes est refusée même avec un code correct
- Test de non-régression qui échoue avant le correctif
**Référence** : EF3, EF5, RG1, RG3

---

### 20. [Should] Répondre 409 sur une présence en double soumise en même temps
**Description** : Défaut annexe repéré pendant la reproduction de l'issue 18 : `existsBySessionIdAndEtudiantId` puis `save` forme un check-then-act non atomique. Deux soumissions simultanées pour le **même** étudiant passent toutes les deux le contrôle, la seconde viole `UNIQUE (session_id, etudiant_id)` → 500 `ERREUR_INTERNE` au lieu de 409 `DEJA_PRESENT`.
**Critères d'acceptation** :
- Deux soumissions simultanées du même étudiant : exactement une 201 et une 409 `DEJA_PRESENT`
- Aucun 500 sur ce scénario
**Référence** : EF4, RG2
