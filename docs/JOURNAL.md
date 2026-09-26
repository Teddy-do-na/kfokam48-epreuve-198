# Journal de bord

## Étape 1 — Analyse et conception
Fait : cahier des charges (10 sections, 17 EF, 13 RG), 4 diagrammes Mermaid (D1 à D4),
17 issues créées sur le backlog, contrat d'API complété, commit [JALON] analyse poussé.
Bloqué : ~15 min sur la contradiction Q10/Q15 (modification de note), tranchée en
faveur de Q10 en introduisant la clôture de session comme frontière (ajout EF17).
IA : utilisée pour structurer le cahier des charges à partir de SUJET.md et CLIENT.md,
proposer les diagrammes et rédiger le backlog. Vérifié en recroisant chaque EF/RG avec
la question Qx source, et en relisant D2 face à ma migration Flyway pour cohérence.

## Étape 2 — v0.1
Fait :
Bloqué :
IA :

## Étape 3 — Enveloppe
Fait : contenu de l'enveloppe lu (PDF joint au dépôt) puis traduit en trois issues techniques
(`ISSUES.md` 18, 19, 20). Écriture d'abord de 5 tests d'intégration **rouges** sur PostgreSQL
réel (conteneur testcontainers si Docker, sinon la base locale `kfokam48_test`) — vus échouer
avec les codes 23502 exacts du rapport client. Correctif ensuite sur la branche dédiée
`fix/presences-issues-18-19-20`, **un commit par issue** : horodatages systématiques de la
`Relecture` + verrou de session (ISSUE 18), `tentative_code.session_id` rendu optionnel par la
nouvelle migration **V3** (ISSUE 19), traduction des violations d'unicité en 409 (ISSUE 20).
V1 et V2 sont intactes. Analyse mise à jour dans le même temps : cahier des charges (EF2, EF3,
EF4, RG3, nouveau RG14, section 7), contrat (`/api/presences` désormais décrit avec le format
d'erreur unique) et diagramme D3. 32 tests verts, dont 5 d'intégration.
Bloqué : pas de token GitHub sur la machine — les issues 18/19/20 et les deux PR sont à créer
à la main sur github.com (textes fournis) ; Docker non démarré ici, le test d'intégration bascule
donc sur le Postgres local tout en conservant le conteneur pour un correcteur avec Docker ;
le changement de besoin (deux paires de relecteurs, note = moyenne) n'est pas dans cette branche.
IA : utilisée pour extraire le texte du PDF (aucun `pdftotext` installé), reproduire les trois
défauts en HTTP contre Postgres réel et rédiger le correctif. Vérification : scénario du client
rejoué avant/après (500 puis 201), tests rouges constatés puis verts, `mvn test` complet à chaque
commit, et rejeu du proxy Vite ↔ API.
**Périmètre sacrifié** : le changement de besoin « deux relecteurs + moyenne » est reporté hors
de cette branche de correctif (il demanderait migration, contrat, API et 3 écrans — priorité au
bug client) ; l'automatisation des issues/PR GitHub est sacrifiée faute de token ; aucun autre
défaut que 18/19/20 n'est traité ici.

## Étape 4 — v1.0
Fait :
Bloqué :
IA :

## Étape 5 — Épreuve Git
Fait :
Bloqué :
IA :