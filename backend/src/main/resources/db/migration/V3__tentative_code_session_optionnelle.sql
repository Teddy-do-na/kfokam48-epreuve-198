-- ISSUE 19 (enveloppe étape 3) — migration versionnée ajoutée, V1 et V2 restent inchangées.
--
-- Un code totalement inconnu ne renvoie aucune session, alors que tentative_code.session_id
-- est NOT NULL depuis V1 : chaque code inconnu plantait en 23502 et l'API répondait 500
-- ERREUR_INTERNE au lieu de 400 CODE_INCONNU. Pire, l'échec n'était jamais enregistré, donc le
-- compteur de blocage après 5 essais (EF5, RG3) ne pouvait jamais se remplir.
--
-- La colonne devient optionnelle : l'échec est compté dans tous les cas, la session est
-- renseignée quand elle est connue (code expiré, promotion incorrecte).
ALTER TABLE tentative_code ALTER COLUMN session_id DROP NOT NULL;

-- EF5 / RG3 : lecture du compteur par étudiant sur la fenêtre glissante de 2 minutes
-- (WHERE etudiant_id = ? AND created_at > now() - interval '2 minutes').
CREATE INDEX IF NOT EXISTS idx_tentative_code_etudiant_created
    ON tentative_code (etudiant_id, created_at DESC);
