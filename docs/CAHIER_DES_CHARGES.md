# Cahier des charges — Suivi de présence et relecture par les pairs — KFOKAM48

Auteur : <198> · Version 1 · Frontend choisi : React, parce qu'il permet de construire rapidement une SPA avec une gestion claire des états de chargement et d'erreur.

## 1. Contexte et objectif

La direction de la formation KFOKAM48 gère des sessions de cours en présentiel. Elle veut automatiser trois processus aujourd'hui manuels : la prise de présence par code, le dépôt des exercices, et leur relecture croisée anonyme entre étudiants, le tout consolidé dans un tableau de suivi par étudiant et par promotion.

## 2. Acteurs et rôles

| Acteur | Ce qu'il peut faire |
|---|---|
| Formateur | Ouvre une session et obtient un code de présence, ajoute une présence manuellement, clôture une session, consulte le tableau de suivi |
| Étudiant | Saisit un code pour marquer sa présence, dépose ou remplace le lien de son exercice, consulte la note et le commentaire reçus |
| Relecteur | Un étudiant assigné ponctuellement par le système à la relecture de l'exercice d'un pair : il note et commente |

Le relecteur n'est pas un rôle permanent : c'est un étudiant, assigné automatiquement pour une relecture précise (cf. Q7).

## 3. Périmètre

**Inclus**
- Ouverture de session, génération et expiration du code de présence
- Marquage de présence par l'étudiant, ou ajout manuel par le formateur
- Dépôt et remplacement du lien d'exercice
- Assignation aléatoire d'un relecteur parmi les étudiants présents à la session
- Saisie et correction de la note (0–20, entier) et du commentaire, jusqu'à clôture de la session
- Tableau de bord formateur : présence, dépôts, moyenne, relectures en attente

**Exclu**
- Authentification par mot de passe (Q1)
- Gestion de plusieurs relecteurs par exercice (Q6 : un seul)
- Notation autre que 0–20 entier
- Export ou historique inter-sessions au-delà du tableau demandé
- Gestion fine des droits (plusieurs formateurs, administration)

## 4. Exigences fonctionnelles

| Réf | Exigence | Critère d'acceptation | Priorité |
|---|---|---|---|
| EF1 | Le formateur ouvre une session et obtient un code | `POST /api/sessions` renvoie 201 avec un code unique, une date d'ouverture et une expiration à +15 min | Must |
| EF2 | L'étudiant marque sa présence avec un code valide | Un code valide et non expiré, saisi par un étudiant pas encore présent, renvoie 201 et la présence apparaît dans le tableau — même si un exercice est en attente d'assignation et même si un pair saisit le code au même instant, et sans réessai | Must |
| EF3 | Un code expiré ou inconnu est refusé | Un code saisi après `expirationAt` renvoie 410 `CODE_EXPIRE` ; un code inconnu renvoie 400 `CODE_INCONNU`. Jamais de 500 pour l'une ou l'autre situation | Must |
| EF4 | Un étudiant ne peut pas se marquer présent deux fois | Une deuxième tentative valide sur la même session renvoie 409 `DEJA_PRESENT` ; en cas de soumission simultanée, exactement un 201 et un 409, jamais de 500 | Must |
| EF5 | Après 5 échecs consécutifs, l'étudiant est bloqué 2 minutes | La 6e tentative dans la fenêtre est refusée, même avec un code correct, jusqu'à écoulement du délai | Should |
| EF6 | Le formateur ajoute une présence manuellement | La présence créée porte `source=FORMATEUR` et s'affiche distinctement | Must |
| EF7 | L'étudiant dépose le lien de son exercice | `POST /api/exercices` renvoie 201, statut `EN_ATTENTE_RELECTURE` | Must |
| EF8 | L'étudiant remplace son lien tant que la relecture n'a pas commencé | Accepté si aucune relecture `RENDUE` n'existe pour l'exercice, refusé sinon | Should |
| EF9 | Le dépôt reste possible après la fin de la session, jusqu'à sa clôture | Un dépôt après `expirationAt` mais avant clôture renvoie 201 | Must |
| EF10 | Le système assigne un relecteur au hasard parmi les présents | Le relecteur assigné ≠ auteur de l'exercice et figure parmi les présents de la session | Must |
| EF11 | Un étudiant ne relit jamais son propre exercice | `POST /api/relectures/{id}` par l'auteur renvoie 403 | Must |
| EF12 | Le relecteur note et commente l'exercice | Note entière 0–20 acceptée (200), sinon 400 | Must |
| EF13 | Le relecteur corrige sa note tant que la session n'est pas clôturée | Nouvelle soumission avant clôture → 200 et remplacement ; après clôture → 409 | Must |
| EF14 | L'étudiant relu voit sa note et le commentaire, jamais l'identité du relecteur | Aucune donnée d'identification du relecteur n'est exposée à l'étudiant | Must |
| EF15 | Un exercice sans relecture rendue reste "en attente" dans le tableau | `relecturesEnAttente` compte l'exercice tant qu'aucune relecture `RENDUE` n'existe | Must |
| EF16 | Le formateur consulte le tableau de suivi | `GET /api/tableau?promotionId=` renvoie, par étudiant : présences, exercices déposés, moyenne, relectures en attente | Must |
EF17 | Le formateur clôture une session | Une fois clôturée, aucun dépôt ni aucune note ne peut plus être modifié (409 sur toute tentative) | Must |

## 5. Exigences non fonctionnelles

| Réf | Exigence | Comment on la vérifie |
|---|---|---|
| ENF1 | Utilisable sur mobile (l'étudiant saisit son code depuis son téléphone) | Les 3 écrans restent utilisables à 375px de large |
| ENF2 | Réponse perçue < 1 s sur les opérations de présence | Mesure manuelle en environnement de démonstration |
| ENF3 | Volumétrie cible : promotion ≤ 60 étudiants, session ≤ 60 présences | Le tableau reste lisible sans pagination à cette échelle |
| ENF4 | Aucune donnée technique (stack trace) exposée au client | Toutes les erreurs respectent le format `{code, message}` imposé |

## 6. Règles de gestion

| Réf | Règle | Source |
|---|---|---|
| RG1 | Le code de présence expire 15 minutes après l'ouverture de la session | Q2 |
| RG2 | Impossible de marquer sa présence une fois le code expiré | Q2, Q3 |
| RG3 | 5 échecs de code consécutifs → blocage de 2 minutes ; tout échec compte, y compris pour un code inconnu qui ne rattache à aucune session | Q4 |
| RG4 | Un étudiant ne peut jamais relire son propre exercice | Q5 |
| RG5 | Un exercice a exactement un relecteur, choisi au hasard parmi les présents à la session | Q6, Q7 |
| RG6 | L'étudiant relu voit note et commentaire, jamais l'identité du relecteur | Q8 |
| RG7 | La note est un entier de 0 à 20 | Q9 |
| RG8 | Le relecteur peut modifier sa note tant que le formateur n'a pas clôturé la session | Q10 (voir arbitrage RG11 / section 7) |
| RG9 | Un exercice sans relecture rendue reste "en attente" et doit être visible comme tel dans le tableau | Q11 |
| RG10 | Un exercice peut être déposé jusqu'à la clôture de la session, même après sa fin programmée | Q12 |
| RG11 | Le lien d'un exercice peut être remplacé tant qu'aucune relecture n'a été rendue | Q13 |
| RG12 | Une présence ajoutée manuellement par le formateur est marquée `source=FORMATEUR` | Q14 |
| RG13 | Une fois la session clôturée par le formateur, aucune note ne peut plus être modifiée | Arbitrage Q10 / Q15, voir section 7 |
| RG14 | Une présence n'est jamais perdue à cause de l'assignation d'un relecteur : la relecture créée porte systématiquement ses horodatages et les écritures d'une même session sont sérialisées par un verrou pessimiste (dépôt de la session) | Rapport client enveloppe étape 3, EF2 / EF4 / EF5 |

## 7. Zones d'ombre, hypothèses et contradictions tranchées

| Point | Réponse client (Qx) ou hypothèse | Décision retenue | Pourquoi |
|---|---|---|---|
| Contradiction sur la modification de note | Q10 : correction possible tant que la session n'est pas clôturée. Q15 : la note est définitive une fois envoyée | Je retiens Q10 comme règle opérationnelle (RG8/RG13) : la note reste modifiable jusqu'à la clôture explicite de la session par le formateur. Je lis Q15 comme la description de l'état final *après* clôture, pas comme une règle indépendante et antérieure | Sans la notion de clôture, Q15 rendrait Q10 incohérente et interdirait toute correction d'une erreur de saisie — ce qui contredirait l'intention affichée en Q15 ("plus honnête pour tout le monde") : une note erronée non corrigible n'est pas plus honnête |
| Trou : qui clôture une session, et quand ? | Aucune question ne le demande, alors que Q10, Q12, Q13 et Q15 reposent tous sur la notion de "session clôturée" | Seul le formateur peut clôturer une session, par une action explicite distincte de l'expiration du code. Tant qu'elle n'est pas clôturée : dépôts et corrections de note restent possibles | Le code expire (RG1) mais la session continue de vivre pour les dépôts (Q12) : il faut un second événement, contrôlé par le formateur, qui ferme définitivement le dossier. Sans lui, RG8/RG10/RG11 n'ont pas de borne temporelle claire |
| Session avec un seul étudiant présent : impossible d'assigner un relecteur distinct | Non traité par le client | L'exercice reste en statut "en attente d'assignation" (compté comme en attente dans le tableau) tant qu'aucun autre étudiant éligible n'est présent ; réévaluation à chaque nouvelle présence sur la même session | Évite un blocage ou une violation de RG4 (relecture de soi-même) |
| Un même étudiant peut-il relire plusieurs exercices sur une session ? | Non traité | Oui, aucune limite posée par défaut | Aucune contrainte contraire dans les 16 réponses ; à corriger si le client le précise |
| Rapport client (enveloppe étape 3) : « deux étudiants ont tapé le code presque en même temps et il n'y en a qu'un seul qui apparaît » — conséquence 2 : « un seul relecteur, ça ne marche pas » | Bug reproduit avant tout correctif : l'assignation échouait en base (`relecture` sans horodatages → 23502) et annulait la transaction de présence ; deux défauts annexes dérivés : 500 au lieu de 400/410 sur un code inconnu, 500 au lieu de 409 sur une double présence simultanée | Bug et évolution séparés. **Bug (issues 18, 19, 20)** : présence et assignation restent dans la même transaction, mais l'assignation est rendue infaillible (horodatages systématiques, verrou de session pour sérialiser les écritures concurrentes) ; migration V3 rend `tentative_code.session_id` optionnel pour que chaque échec compte. **Évolution (changement de besoin)** : deux paires de relecteurs et note = moyenne des deux, réservée à une branche et une PR distinctes | Postgres annule intégralement une transaction dès la première erreur SQL : déplacer l'assignation dans une autre transaction l'empêcherait de voir la présence en cours de validation ; rendre l'écriture infaillible conserve l'atomicité demandée par EF2 sans violer RG4/RG5 |

## 8. Contraintes techniques

- Backend : Java 17+, Spring Boot, Maven, wrapper `mvnw` commité
- Frontend : React
- Base de données : PostgreSQL, schéma versionné par Flyway, `ddl-auto=update` interdit hors tests
- Contrat `api/contrat.yaml` respecté à la lettre ; format d'erreur unique `{code, message}`
- Séparation contrôleur / service / repository ; DTO uniquement en sortie JSON, aucune entité JPA exposée
- Démarrage reproductible depuis un clone vierge (`docker compose up` ou ≤ 3 commandes documentées), avec données de démonstration

## 9. Livrables

- Dépôt GitHub public, historique Git lisible
- `docs/CAHIER_DES_CHARGES.md`, `docs/diagrammes/` (D1, D2, D3, et D4 en bonus)
- `api/contrat.yaml` complété
- Backend Spring Boot avec migrations Flyway et tests (unitaire + intégration)
- Frontend React avec 3 écrans (formateur, étudiant, relecteur)
- `README.md` d'installation testé, `CHANGELOG.md`, `JOURNAL.md`

## 10. Démarche prévue

1. Analyse et conception (ce document, 3 diagrammes, backlog en issues, contrat d'API) → jalon `[JALON] analyse`
2. Construction de la v0.1 sur les stories Must uniquement, une branche + une PR par ticket → jalon `[JALON] v0.1`
3. Ouverture de l'enveloppe : bug et évolution traités dans des commits/PR séparés, migration de schéma versionnée, contrat et analyse mis à jour
4. Finalisation v1.0 : `CHANGELOG.md`, README testé depuis un clone vierge, backlog restant trié → jalon `[JALON] v1.0`
5. Épreuve Git indépendante sur le dépôt fourni (dépôt séparé)
6. Soumission sur la plateforme avant 18h00

**Definition of Done** : un ticket est terminé quand son code est mergé sur `main` via une PR liée à son issue, que les tests associés passent, et que le comportement observé correspond au critère d'acceptation de l'EFx ou de la RGx citée.
