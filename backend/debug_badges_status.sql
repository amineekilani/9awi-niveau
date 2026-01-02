-- Debug du statut des badges
USE 9awi_niveau;

-- Voir la structure de la colonne is_active
DESCRIBE badges;

-- Voir les valeurs actuelles
SELECT id, name, is_active, CAST(is_active AS SIGNED) as is_active_int FROM badges ORDER BY id;

-- Forcer tous les badges à actif (sauf ID 2 pour tester)
UPDATE badges SET is_active = 1 WHERE id != 2;
UPDATE badges SET is_active = 0 WHERE id = 2;

-- Vérifier le résultat
SELECT id, name, is_active, CAST(is_active AS SIGNED) as is_active_int FROM badges ORDER BY id;