-- Script pour nettoyer les doublons dans la table user_xp
USE 9awi_niveau;

-- Vérifier les doublons
SELECT user_id, COUNT(*) as count 
FROM user_xp 
GROUP BY user_id 
HAVING COUNT(*) > 1;

-- Supprimer les doublons en gardant l'enregistrement avec l'ID le plus élevé
DELETE ux1 FROM user_xp ux1
INNER JOIN user_xp ux2 
WHERE ux1.id < ux2.id AND ux1.user_id = ux2.user_id;

-- Vérifier qu'il n'y a plus de doublons
SELECT user_id, COUNT(*) as count 
FROM user_xp 
GROUP BY user_id 
HAVING COUNT(*) > 1;

SELECT 'Doublons supprimés avec succès!' as message;