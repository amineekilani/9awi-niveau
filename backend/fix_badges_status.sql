-- Corriger le statut des badges
USE 9awi_niveau;

-- Mettre tous les badges par défaut à actif
UPDATE badges SET is_active = 1;

-- Vérifier le résultat
SELECT id, name, is_active FROM badges ORDER BY id;