-- Migration pour supprimer les récompenses des niveaux
-- Les récompenses étaient purement descriptives et non implémentées

-- Supprimer la colonne reward_description de la table levels
ALTER TABLE levels DROP COLUMN reward_description;

-- Message de confirmation
SELECT 'Colonne reward_description supprimée avec succès!' as message;