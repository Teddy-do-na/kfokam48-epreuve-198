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
