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
     * PRIORITÉ: Niveau > Badge > Défi > Parcours
     */
    async checkForNewAchievements() {
        console.log('🔍 GamificationNotificationService.checkForNewAchievements() appelé');
        
        try {
            // 🎯 PRIORITÉ 1: Vérifier les montées de niveau EN PREMIER
            try {
                const levelNotifications = await this.levelNotificationService.getNewLevelNotifications().toPromise();
                console.log('📈 Notifications de niveau nouvelles trouvées:', levelNotifications?.length || 0);
                if (levelNotifications && levelNotifications.length > 0) {
                    console.log('🎉 Affichage des notifications de niveau (PRIORITÉ):', levelNotifications);
                    await this.showLevelNotifications(levelNotifications);
                }
            } catch (error) {
                console.warn('⚠️ Service de notifications de niveau non disponible:', error);
            }

            // 🏆 PRIORITÉ 2: Vérifier les badges
            try {
                const badges = await this.gamificationService.getUserBadges('new').toPromise();
                console.log('🏆 Badges nouveaux trouvés:', badges?.length || 0);
                if (badges && badges.length > 0) {
                    await this.showBadgeNotifications(badges);
                }
            } catch (error) {
                console.warn('⚠️ Service de badges non disponible:', error);
            }

            // 🎯 PRIORITÉ 3: Vérifier les défis
            try {
                const challenges = await this.gamificationService.getUserChallenges().toPromise();
                const newChallenges = challenges ? challenges.filter(c => c.isCompleted && c.isNew) : [];
                console.log('🎯 Défis nouveaux trouvés:', newChallenges?.length || 0);
                if (newChallenges.length > 0) {
                    await this.showChallengeNotifications(newChallenges);
                }
            } catch (error) {
                console.warn('⚠️ Service de défis non disponible:', error);
            }

            // 📚 PRIORITÉ 4: Vérifier les notifications de parcours
            try {
                const parcoursNotifications = await this.parcoursNotificationService.getUnreadNotifications().toPromise();
                console.log('📚 Notifications de parcours non lues trouvées:', parcoursNotifications?.length || 0);
                if (parcoursNotifications && parcoursNotifications.length > 0) {
                    await this.showParcoursNotifications(parcoursNotifications);
                }
            } catch (error) {
                console.warn('⚠️ Service de notifications de parcours non disponible:', error);
            }

        } catch (error) {
            console.error('❌ Erreur générale lors de la vérification des achievements:', error);
        }
    }

    private async showLevelNotifications(levelNotifications: LevelNotification[]) {
        // Afficher les alertes une par une (séquentiel) avec style spécial pour les niveaux
        for (const notification of levelNotifications) {
            // Créer un message personnalisé avec plus d'emphase
            const message = `
                <div style="text-align: center; padding: 1rem;">
                    <div style="font-size: 4rem; margin-bottom: 1rem;">🎉</div>
                    <div style="font-size: 1.5rem; font-weight: bold; margin-bottom: 1rem; color: #fbbf24;">
                        MONTÉE DE NIVEAU !
                    </div>
                    <div style="font-size: 1.2rem; margin-bottom: 1rem;">
                        Vous êtes passé du <strong>niveau ${notification.oldLevel}</strong> au <strong>niveau ${notification.newLevel}</strong> !
                    </div>
                    <div style="background: rgba(255,255,255,0.2); padding: 1rem; border-radius: 0.5rem; margin: 1rem 0;">
                        <div style="font-size: 1.1rem; font-weight: bold; color: #fbbf24;">🎯 ${notification.levelName}</div>
                        <div style="margin-top: 0.5rem;">⭐ <strong>Total XP:</strong> ${notification.totalXP.toLocaleString()} points</div>
                        <div>🚀 <strong>XP gagnés:</strong> +${notification.xpGained.toLocaleString()} points</div>
                    </div>
                </div>
            `;

            // Afficher l'alerte avec un style spécial pour les niveaux (plus visible)
            await Swal.fire({
                title: '',
                html: message,
                icon: undefined, // Pas d'icône par défaut, on utilise notre emoji
                confirmButtonText: '🎉 Fantastique !',
                confirmButtonColor: '#f59e0b', // Orange/jaune pour les niveaux
                color: '#ffffff',
                background: 'linear-gradient(135deg, #f59e0b 0%, #d97706 50%, #92400e 100%)', // Gradient orange
                padding: '1rem',
                backdrop: `rgba(245, 158, 11, 0.3)`, // Fond orange semi-transparent
                width: '500px',
                customClass: {
                    popup: 'rounded-2xl shadow-2xl border-4 border-yellow-300',
                    title: 'text-3xl font-bold text-white',
                    htmlContainer: 'text-white',
                    confirmButton: 'font-bold px-10 py-4 rounded-xl shadow-lg transform hover:scale-110 transition-all duration-200 text-lg'
                },
                showClass: {
                    popup: 'animate__animated animate__bounceIn animate__slow'
                },
                hideClass: {
                    popup: 'animate__animated animate__fadeOut animate__fast'
                },
                allowOutsideClick: false, // Forcer l'utilisateur à cliquer pour fermer
                allowEscapeKey: false
            });

            // Marquer comme vue APRÈS que l'utilisateur ait cliqué sur OK
            try {
                await this.levelNotificationService.markNotificationAsViewed(notification.id).toPromise();
                console.log('✅ Notification de niveau marquée comme vue:', notification.id);
            } catch (error) {
                console.error('❌ Erreur lors du marquage de la notification de niveau:', error);
            }
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
