import { Injectable } from '@angular/core';
import Swal from 'sweetalert2';
import { UserGamificationService, UserBadge } from './user-gamification.service';

@Injectable({
    providedIn: 'root'
})
export class GamificationNotificationService {

    constructor(
        private gamificationService: UserGamificationService
    ) { }

    /**
     * Vérifie s'il y a de nouveaux badges ou défis et affiche des alertes
     */
    checkForNewAchievements() {
        // 1. Vérifier les badges
        this.gamificationService.getUserBadges('new').subscribe({
            next: (badges) => {
                if (badges && badges.length > 0) {
                    this.showBadgeNotifications(badges);
                }
            }
        });

        // 2. Vérifier les défis
        this.gamificationService.getUserChallenges().subscribe({
            next: (challenges) => {
                const newChallenges = challenges.filter(c => c.isCompleted && c.isNew);
                if (newChallenges.length > 0) {
                    this.showChallengeNotifications(newChallenges);
                }
            }
        });
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
