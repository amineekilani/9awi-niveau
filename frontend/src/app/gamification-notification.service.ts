import { Injectable } from '@angular/core';
import Swal from 'sweetalert2';
import { UserGamificationService, UserBadge } from './user-gamification.service';
import { ParcoursNotificationService, ParcoursNotification } from './parcours-notification.service';
import { LevelNotificationService, LevelNotification } from './level-notification.service';

@Injectable({
    providedIn: 'root'
})
export class GamificationNotificationService {

    constructor(
        private gamificationService: UserGamificationService,
        private parcoursNotificationService: ParcoursNotificationService,
        private levelNotificationService: LevelNotificationService
    ) { }

    /**
     * Vérifie s'il y a de nouveaux badges, défis, niveaux ou parcours et affiche des alertes
     */
    checkForNewAchievements() {
        console.log('🔍 GamificationNotificationService.checkForNewAchievements() appelé');
        
        // 1. Vérifier les badges
        this.gamificationService.getUserBadges('new').subscribe({
            next: (badges) => {
                console.log('🏆 Badges nouveaux trouvés:', badges?.length || 0);
                if (badges && badges.length > 0) {
                    this.showBadgeNotifications(badges);
                }
            },
            error: (error) => {
                console.error('❌ Erreur lors de la récupération des badges:', error);
            }
        });

        // 2. Vérifier les montées de niveau
        this.levelNotificationService.getNewLevelNotifications().subscribe({
            next: (levelNotifications) => {
                console.log('📈 Notifications de niveau nouvelles trouvées:', levelNotifications?.length || 0);
                if (levelNotifications && levelNotifications.length > 0) {
                    console.log('🎉 Affichage des notifications de niveau:', levelNotifications);
                    this.showLevelNotifications(levelNotifications);
                }
            },
            error: (error) => {
                console.error('❌ Erreur lors de la récupération des notifications de niveau:', error);
            }
        });

        // 3. Vérifier les défis
        this.gamificationService.getUserChallenges().subscribe({
            next: (challenges) => {
                const newChallenges = challenges.filter(c => c.isCompleted && c.isNew);
                console.log('🎯 Défis nouveaux trouvés:', newChallenges?.length || 0);
                if (newChallenges.length > 0) {
                    this.showChallengeNotifications(newChallenges);
                }
            },
            error: (error) => {
                console.error('❌ Erreur lors de la récupération des défis:', error);
            }
        });

        // 4. Vérifier les notifications de parcours
        this.parcoursNotificationService.getUnreadNotifications().subscribe({
            next: (notifications) => {
                console.log('📚 Notifications de parcours non lues trouvées:', notifications?.length || 0);
                if (notifications && notifications.length > 0) {
                    this.showParcoursNotifications(notifications);
                }
            },
            error: (error) => {
                console.error('❌ Erreur lors de la récupération des notifications de parcours:', error);
            }
        });
    }

    private async showLevelNotifications(levelNotifications: LevelNotification[]) {
        // Afficher les alertes une par une (séquentiel)
        for (const notification of levelNotifications) {
            // Créer un message personnalisé
            const message = `Félicitations ! Vous êtes passé du niveau ${notification.oldLevel} au niveau ${notification.newLevel} !<br><br>
                            <strong>🎯 ${notification.levelName}</strong><br>
                            <strong>⭐ Total XP:</strong> ${notification.totalXP.toLocaleString()} points<br>
                            <strong>🚀 XP gagnés:</strong> +${notification.xpGained.toLocaleString()} points`;

            // Afficher l'alerte avec un style spécial pour les niveaux
            await Swal.fire({
                title: '🎉 Montée de niveau !',
                html: message,
                icon: 'success',
                confirmButtonText: 'Fantastique !',
                confirmButtonColor: '#10b981', // Vert pour les niveaux
                color: '#1f2937',
                background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
                padding: '2rem',
                backdrop: `rgba(16, 185, 129, 0.1)`,
                customClass: {
                    popup: 'rounded-xl shadow-2xl border-2 border-green-200',
                    title: 'text-2xl font-bold text-white',
                    htmlContainer: 'text-white',
                    confirmButton: 'font-bold px-8 py-3 rounded-lg shadow-lg transform hover:scale-105 transition-transform'
                },
                showClass: {
                    popup: 'animate__animated animate__bounceIn animate__faster'
                },
                hideClass: {
                    popup: 'animate__animated animate__fadeOut animate__faster'
                }
            });

            // Marquer comme vue APRÈS que l'utilisateur ait cliqué sur OK
            this.levelNotificationService.markNotificationAsViewed(notification.id).subscribe({
                next: () => {
                    console.log('✅ Notification de niveau marquée comme vue:', notification.id);
                },
                error: (error) => {
                    console.error('❌ Erreur lors du marquage de la notification de niveau:', error);
                }
            });
        }
    }

    private async showParcoursNotifications(notifications: ParcoursNotification[]) {
        for (const notification of notifications) {
            let icon: 'success' | 'info' = 'success';
            let title = notification.title;
            let message = notification.message;

            // Personnaliser selon le type de notification
            if (notification.type === 'PARCOURS_COMPLETED') {
                icon = 'success';
                title = '🎉 ' + title;
                if (notification.certificateReady) {
                    message += '\n\n📜 Votre certificat est prêt à être téléchargé !';
                }
            } else if (notification.type === 'CERTIFICATE_READY') {
                icon = 'info';
                title = '📜 ' + title;
            }

            const result = await Swal.fire({
                title: title,
                html: message.replace(/\n/g, '<br>'), // Convertir les retours à la ligne en HTML
                icon: icon,
                confirmButtonText: notification.certificateReady ? 'Voir le certificat' : 'Génial !',
                showCancelButton: notification.certificateReady,
                cancelButtonText: notification.certificateReady ? 'Plus tard' : undefined,
                confirmButtonColor: '#063cdf',
                cancelButtonColor: '#6b7280',
                background: '#ffffff',
                padding: '2rem',
                backdrop: `rgba(0,123,0,0.1)`,
                customClass: {
                    popup: 'rounded-xl shadow-2xl',
                    title: 'text-2xl font-bold text-gray-900',
                    confirmButton: 'font-bold px-6 py-2 rounded-lg',
                    cancelButton: 'font-bold px-6 py-2 rounded-lg'
                }
            });

            // Gérer les actions après fermeture de l'alerte
            if (result.isConfirmed && notification.certificateReady && notification.certificateUrl) {
                // Ouvrir le certificat dans un nouvel onglet
                window.open(notification.certificateUrl, '_blank');
            }

            // Marquer comme lue après que l'utilisateur ait cliqué
            this.parcoursNotificationService.markNotificationAsRead(notification.id).subscribe({
                next: () => {
                    console.log('Notification de parcours marquée comme lue:', notification.id);
                },
                error: (error) => {
                    console.error('Erreur lors du marquage de la notification:', error);
                }
            });
        }
    }

    private async showBadgeNotifications(badges: UserBadge[]) {
        // Afficher les alertes une par une (séquentiel)
        for (const badge of badges) {

            // Logique simplifiée pour l'image (similaire à mes-recompenses)
            // 1. Image par défaut
            let imageUrl = '/badges/default-badge.svg';

            // 2. Mapping basé sur le type (Prioritaire pour les icônes internes pour éviter les liens brisés)
            if (badge.criteriaType) {
                const iconMap: { [key: string]: string } = {
                    'FIRST_COURSE': '/badges/first-course.svg',
                    'COURS_COMPLETED': '/badges/first-course.svg',
                    'QUIZ_PASSED': '/badges/quiz-master.svg',
                    'FIRST_QUIZ': '/badges/quiz-master.svg',
                    'PERFECT_SCORE': '/badges/perfect-score.svg',
                    'DAILY_LOGIN': '/badges/streak-master.svg',
                    'EARN_XP': '/badges/default-badge.svg',
                    'COMPLETE_MODULE': '/badges/first-course.svg',
                    'EARN_BADGES': '/badges/default-badge.svg'
                };
                if (iconMap[badge.criteriaType]) {
                    imageUrl = iconMap[badge.criteriaType];
                }
            }

            // 3. Surcharge uniquement si URL externe absolue (http/https)
            // On ignore les chemins relatifs issus de la DB pour éviter les erreur 404 sur les vieux noms de fichiers
            if (badge.iconUrl && (badge.iconUrl.startsWith('http') || badge.iconUrl.startsWith('https'))) {
                imageUrl = badge.iconUrl;
            }

            // 4. Si on n'a toujours rien trouvé de specifique, on essaie l'URL relative de la DB en dernier recours
            if (imageUrl === '/badges/default-badge.svg' && badge.iconUrl && !badge.iconUrl.startsWith('http')) {
                // Attention: risque de 404 ici si le fichier n'existe pas, mais c'est mieux que rien si pas de type
                // On suppose que si le type est manquant, l'URL est peut-etre bonne
                const cleanUrl = badge.iconUrl.startsWith('/') ? badge.iconUrl.substring(1) : badge.iconUrl;
                // imageUrl = `/badges/${cleanUrl}`; // Désactivé pour sécurité, on préfère le défaut si doute
            }

            // Afficher l'alerte
            await Swal.fire({
                title: 'Félicitations ! 🎉',
                text: `Vous avez débloqué le badge "${badge.name}" !`,
                imageUrl: imageUrl,
                imageWidth: 120,
                imageHeight: 120,
                imageAlt: badge.name,
                confirmButtonText: 'Génial !',
                confirmButtonColor: '#063cdf',
                color: '#1f2937',
                background: '#ffffff',
                padding: '2rem',
                backdrop: `
          rgba(0,0,123,0.1)
        `,
                customClass: {
                    popup: 'rounded-xl shadow-2xl',
                    title: 'text-2xl font-bold text-gray-900',
                    confirmButton: 'font-bold px-6 py-2 rounded-lg'
                }
            });

            // Marquer comme vu APRES que l'utilisateur ait cliqué sur OK
            this.gamificationService.markBadgeAsViewed(badge.id).subscribe();
        }
    }

    private async showChallengeNotifications(challenges: any[]) {
        for (const challenge of challenges) {
            await Swal.fire({
                title: 'Défi Relevé ! 🎯',
                text: `Félicitations ! Vous avez terminé le défi "${challenge.name}" et gagné ${challenge.xpReward} XP !`,
                icon: 'success',
                confirmButtonText: 'Super !',
                confirmButtonColor: '#063cdf',
                color: '#1f2937',
                background: '#ffffff',
                padding: '2rem',
                backdrop: `
          rgba(0,123,0,0.1)
        `,
                customClass: {
                    popup: 'rounded-xl shadow-2xl',
                    title: 'text-2xl font-bold text-gray-900',
                    confirmButton: 'font-bold px-6 py-2 rounded-lg'
                }
            });

            // Marquer comme vu
            this.gamificationService.markChallengeAsViewed(challenge.id).subscribe();
        }
    }
}
