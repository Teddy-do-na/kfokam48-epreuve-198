-- Jeu de données de démonstration pour permettre une prise en main immédiate.
INSERT INTO promotion (nom) VALUES ('Promotion KFOKAM48') ON CONFLICT DO NOTHING;

INSERT INTO etudiant (nom, promotion_id)
SELECT demo.nom, p.id
FROM (VALUES ('Ada Lovelace'), ('Alan Turing'), ('Grace Hopper')) AS demo(nom)
CROSS JOIN (SELECT id FROM promotion WHERE nom = 'Promotion KFOKAM48' ORDER BY id LIMIT 1) p
WHERE NOT EXISTS (
    SELECT 1 FROM etudiant e WHERE e.nom = demo.nom AND e.promotion_id = p.id
);

INSERT INTO session (titre, promotion_id, code, ouverture_at, expiration_at, cloturee, cloturee_at)
SELECT 'Session de démonstration', p.id, 'DEMO48', now(), now() + INTERVAL '15 minutes', FALSE, NULL
FROM promotion p
WHERE p.nom = 'Promotion KFOKAM48'
  AND NOT EXISTS (SELECT 1 FROM session s WHERE s.code = 'DEMO48');
