import { Injectable } from '@angular/core';
import { Router } from '@angular/router';
import { BehaviorSubject, interval, Subscription } from 'rxjs';
import { ParcoursNotificationService, ParcoursNotification } from './parcours-notification.service';
import { UserGamificationService, UserGamificationStats } from './user-gamification.service';
import { LevelNotificationService, LevelNotification } from './level-notification.service';
import { GamificationNotificationService } from './gamification-notification.service';
import { ParcoursService } from './parcours.service';
import Swal from 'sweetalert2';

@Injectable({
  providedIn: 'root'
})
export class ParcoursAutoRefreshService {
  
  private refreshInterval: Subscription | null = null;
  private lastNotificationCheck = 0;
  private lastLevelNotificationCheck = 0;
  private lastStatsCheck = 0;
  
  // Subjects pour notifier les composants des mises à jour
  private parcoursUpdatedSubject = new BehaviorSubject<boolean>(false);
  private statsUpdatedSubject = new BehaviorSubject<UserGamificationStats | null>(null);
  private newNotificationSubject = new BehaviorSubject<ParcoursNotification | null>(null);
  private newLevelNotificationSubject = new BehaviorSubject<LevelNotification | null>(null);
  
  // Observables publics
  public parcoursUpdated$ = this.parcoursUpdatedSubject.asObservable();
  public statsUpdated$ = this.statsUpdatedSubject.asObservable();
  public newNotification$ = this.newNotificationSubject.asObservable();
  public newLevelNotification$ = this.newLevelNotificationSubject.asObservable();

  constructor(
    private parcoursNotificationService: ParcoursNotificationService,
    private userGamificationService: UserGamificationService,
    private levelNotificationService: LevelNotificationService,
    private gamificationNotificationService: GamificationNotificationService,
    private parcoursService: ParcoursService,
    private router: Router
  ) {}

  /**
   * Démarre le service de mise à jour automatique
   */
  startAutoRefresh() {
    console.log('🔄 Démarrage du service de mise à jour automatique des parcours');
    
    // Arrêter l'ancien interval s'il existe
    this.stopAutoRefresh();
    
    // Vérifier immédiatement
    this.checkForUpdates();
    
    // Puis vérifier toutes les 60 secondes (au lieu de 10) pour réduire la charge
    this.refreshInterval = interval(60000).subscribe(() => {
      this.checkForUpdates();
    });
  }

  /**
   * Arrête le service de mise à jour automatique
   */
  stopAutoRefresh() {
    if (this.refreshInterval) {
      this.refreshInterval.unsubscribe();
      this.refreshInterval = null;
      console.log('⏹️ Service de mise à jour automatique arrêté');
    }
  }

  /**
   * Vérifie les mises à jour (notifications + stats)
   */
  private checkForUpdates() {
    this.checkForNewNotifications();
    this.checkForNewAchievements();
    this.checkForStatsUpdates();
  }

  /**
   * Vérifie les nouvelles réalisations (badges, défis, niveaux)
   */
  private checkForNewAchievements() {
    // Utiliser le service de gamification pour vérifier TOUTES les nouvelles réalisations
    // Cela inclut les badges, défis ET niveaux
    this.gamificationNotificationService.checkForNewAchievements();
  }

  /**
   * Vérifie les nouvelles notifications de parcours
   */
  private checkForNewNotifications() {
    this.parcoursNotificationService.getUnreadNotifications().subscribe({
      next: (notifications) => {
        if (notifications && notifications.length > 0) {
          // Vérifier s'il y a de nouvelles notifications depuis la dernière vérification
          const latestNotification = notifications[0];
          const notificationTime = new Date(latestNotification.createdAt).getTime();
          
          if (notificationTime > this.lastNotificationCheck) {
            console.log('🆕 Nouvelle notification de parcours détectée:', latestNotification.title);
            
            // Notifier les composants
            this.newNotificationSubject.next(latestNotification);
            
            // Afficher une alerte de félicitations
            this.showParcoursCompletionAlert(latestNotification);
            
            // Déclencher la mise à jour des vues
            this.triggerParcoursRefresh();
            
            this.lastNotificationCheck = notificationTime;
          }
        }
      },
      error: (error) => {
        console.error('Erreur lors de la vérification des notifications:', error);
      }
    });
  }

  /**
   * Vérifie les mises à jour des statistiques
   */
  private checkForStatsUpdates() {
    this.userGamificationService.getUserStats().subscribe({
      next: (stats) => {
        const currentTime = Date.now();
        
        // Vérifier s'il y a eu des changements significatifs
        if (currentTime - this.lastStatsCheck > 120000) { // Toutes les 2 minutes au lieu de 30 secondes
          console.log('📊 Mise à jour des statistiques utilisateur');
          this.statsUpdatedSubject.next(stats);
          this.lastStatsCheck = currentTime;
        }
      },
      error: (error) => {
        console.error('Erreur lors de la vérification des stats:', error);
      }
    });
  }

  /**
   * Déclenche la mise à jour des vues de parcours
   */
  private triggerParcoursRefresh() {
    console.log('🔄 Déclenchement de la mise à jour des parcours');
    this.parcoursUpdatedSubject.next(true);
  }

  /**
   * Affiche une alerte de félicitations pour un parcours terminé
   */
  private showParcoursCompletionAlert(notification: ParcoursNotification) {
    if (notification.type === 'PARCOURS_COMPLETED') {
      const xpText = notification.xpEarned ? `+${notification.xpEarned} XP` : '';
      
      // Si un certificat est disponible, proposer deux boutons
      if (notification.certificateReady) {
        Swal.fire({
          title: '🎉 Félicitations !',
          html: `
            <div style="text-align: center;">
              <h3 style="color: #28a745; margin-bottom: 15px;">Parcours Terminé !</h3>
              <p style="font-size: 16px; margin-bottom: 10px;">${notification.message}</p>
              ${xpText ? `<div style="background: #f8f9fa; padding: 10px; border-radius: 8px; margin: 15px 0;">
                <span style="font-size: 18px; font-weight: bold; color: #007bff;">${xpText}</span>
              </div>` : ''}
              <p style="color: #17a2b8; font-size: 16px; margin-top: 15px;">
                <i class="fas fa-certificate"></i> Votre certificat est prêt !
              </p>
            </div>
          `,
          icon: 'success',
          showCancelButton: true,
          confirmButtonText: 'Voir Certificat',
          cancelButtonText: 'Plus tard',
          confirmButtonColor: '#17a2b8',
          cancelButtonColor: '#28a745',
          timer: 10000,
          timerProgressBar: true,
          showClass: {
            popup: 'animate__animated animate__bounceIn'
          },
          hideClass: {
            popup: 'animate__animated animate__bounceOut'
          }
        }).then((result) => {
          if (result.isConfirmed) {
            // Rediriger vers la page mes-parcours avec le Router Angular
            this.router.navigate(['/mes-parcours']);
          }
          
          // Marquer la notification comme lue
          this.parcoursNotificationService.markNotificationAsRead(notification.id).subscribe({
            next: () => {
              console.log('✅ Notification marquée comme lue:', notification.id);
            },
            error: (error) => {
              console.error('Erreur lors du marquage de la notification:', error);
            }
          });
        });
      } else {
        // Pas de certificat, juste une notification simple
        Swal.fire({
          title: '🎉 Félicitations !',
          html: `
            <div style="text-align: center;">
              <h3 style="color: #28a745; margin-bottom: 15px;">Parcours Terminé !</h3>
              <p style="font-size: 16px; margin-bottom: 10px;">${notification.message}</p>
              ${xpText ? `<div style="background: #f8f9fa; padding: 10px; border-radius: 8px; margin: 15px 0;">
                <span style="font-size: 18px; font-weight: bold; color: #007bff;">${xpText}</span>
              </div>` : ''}
            </div>
          `,
          icon: 'success',
          confirmButtonText: 'Super !',
          confirmButtonColor: '#28a745',
          timer: 8000,
          timerProgressBar: true,
          showClass: {
            popup: 'animate__animated animate__bounceIn'
          },
          hideClass: {
            popup: 'animate__animated animate__bounceOut'
          }
        }).then(() => {
          // Marquer la notification comme lue
          this.parcoursNotificationService.markNotificationAsRead(notification.id).subscribe({
            next: () => {
              console.log('✅ Notification marquée comme lue:', notification.id);
            },
            error: (error) => {
              console.error('Erreur lors du marquage de la notification:', error);
            }
          });
        });
      }
    }
  }

  /**
   * Force une vérification immédiate des mises à jour
   */
  forceCheck() {
    console.log('🔄 Vérification forcée des mises à jour');
    this.checkForUpdates();
  }

  /**
   * Réinitialise les timestamps de vérification
   */
  resetCheckTimestamps() {
    this.lastNotificationCheck = 0;
    this.lastLevelNotificationCheck = 0;
    this.lastStatsCheck = 0;
    console.log('🔄 Timestamps de vérification réinitialisés');
  }
}