-- Nettoyer les anciennes notifications de parcours avec certificats
-- Cela supprimera les notifications qui causent l'affichage du bouton

-- 1. Voir les notifications actuelles avec certificats
SELECT 
    'Notifications avec certificats avant nettoyage' as status,
    pn.id,
    u.email,
    pn.title,
    pn.certificate_ready,
    pn.certificate_url,
    FROM_UNIXTIME(pn.created_at/1000) as created_date
FROM parcours_notifications pn
JOIN users u ON pn.user_id = u.id
WHERE pn.certificate_ready = TRUE
ORDER BY pn.created_at DESC;

-- 2. Marquer toutes les notifications avec certificats comme lues (optionnel)
UPDATE parcours_notifications 
SET is_read = TRUE 
WHERE certificate_ready = TRUE;

-- 3. Ou supprimer complètement les anciennes notifications avec certificats (plus radical)
-- Décommentez la ligne suivante si vous voulez supprimer complètement ces notifications
-- DELETE FROM parcours_notifications WHERE certificate_ready = TRUE;

-- 4. Vérifier le résultat
SELECT 
    'Notifications avec certificats après nettoyage' as status,
    COUNT(*) as count
FROM parcours_notifications 
WHERE certificate_ready = TRUE AND is_read = FALSE;

SELECT '✅ Nettoyage terminé' as result;