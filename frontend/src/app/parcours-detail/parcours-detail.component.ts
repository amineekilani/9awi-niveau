import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router, ActivatedRoute } from '@angular/router';
import { NavbarComponent } from '../navbar/navbar.component';
import { AuthService } from '../auth';
import { ParcoursService, ParcoursResponse, ParcoursEtapeResponse, NiveauDifficulte } from '../parcours.service';
import { CertificateService } from '../certificate.service';

declare const feather: any;

@Component({
  selector: 'app-parcours-detail',
  standalone: true,
  imports: [CommonModule, RouterModule, NavbarComponent],
  templateUrl: './parcours-detail.component.html',
  styleUrls: ['./parcours-detail.component.css']
})
export class ParcoursDetailComponent implements OnInit {
  parcours?: ParcoursResponse;
  loading = false;
  error = '';
  success = '';
  
  // Actions
  inscriptionLoading = false;
  
  parcoursId!: number;
  Math = Math; // Exposer Math pour le template

  constructor(
    private parcoursService: ParcoursService,
    public authService: AuthService,
    private certificateService: CertificateService,
    private router: Router,
    private route: ActivatedRoute
  ) {}

  ngOnInit() {
    this.route.params.subscribe(params => {
      this.parcoursId = +params['id'];
      this.loadParcours();
    });

    // Initialiser Feather icons
    setTimeout(() => {
      if (typeof feather !== 'undefined') {
        feather.replace();
      }
    }, 100);
  }

  loadParcours() {
    this.loading = true;
    this.error = '';

    this.parcoursService.getParcoursById(this.parcoursId).subscribe({
      next: (parcours) => {
        this.parcours = parcours;
        this.loading = false;
        
        // Réinitialiser les icônes
        setTimeout(() => {
          if (typeof feather !== 'undefined') {
            feather.replace();
          }
        }, 100);
      },
      error: (err) => {
        this.error = 'Erreur lors du chargement du parcours';
        this.loading = false;
        console.error('Erreur:', err);
      }
    });
  }

  sInscrire() {
    if (!this.parcours) return;

    this.inscriptionLoading = true;
    this.error = '';

    this.parcoursService.sInscrireAuParcours(this.parcours.id).subscribe({
      next: () => {
        this.success = 'Inscription réussie ! Vous pouvez maintenant commencer le parcours.';
        this.inscriptionLoading = false;
        this.loadParcours(); // Recharger pour mettre à jour le statut
        setTimeout(() => this.success = '', 5000);
      },
      error: (err) => {
        this.error = this.extractErrorMessage(err);
        this.inscriptionLoading = false;
      }
    });
  }

  seDesinscrire() {
    if (!this.parcours) return;

    if (!confirm('Êtes-vous sûr de vouloir vous désinscrire de ce parcours ? Votre progression sera perdue.')) {
      return;
    }

    this.inscriptionLoading = true;
    this.error = '';

    this.parcoursService.seDesinscrireDuParcours(this.parcours.id).subscribe({
      next: () => {
        this.success = 'Désinscription réussie.';
        this.inscriptionLoading = false;
        this.loadParcours(); // Recharger pour mettre à jour le statut
        setTimeout(() => this.success = '', 3000);
      },
      error: (err) => {
        this.error = this.extractErrorMessage(err);
        this.inscriptionLoading = false;
      }
    });
  }

  commencerEtape(etape: ParcoursEtapeResponse) {
    // Vérifier d'abord si l'utilisateur est inscrit au cours
    if (!this.isUserEnrolledInCourse(etape.coursId)) {
      // Rediriger vers la page du cours pour inscription
      this.router.navigate(['/cours', etape.coursId]);
      return;
    }

    // Si l'étape n'est pas débloquée (parcours linéaire)
    if (!etape.isDebloque) {
      this.error = this.getEtapeBloqueeMessage(etape);
      setTimeout(() => this.error = '', 5000);
      return;
    }

    // Rediriger vers le cours
    this.router.navigate(['/cours', etape.coursId]);
  }

  // Nouvelle méthode pour vérifier l'inscription au cours
  isUserEnrolledInCourse(coursId: number): boolean {
    // Cette logique devrait être implémentée côté service
    // Pour l'instant, on assume que si l'utilisateur est inscrit au parcours,
    // il peut accéder aux cours (sera amélioré)
    return this.parcours?.isInscrit || false;
  }

  // Nouvelle méthode pour les messages d'étapes bloquées
  getEtapeBloqueeMessage(etape: ParcoursEtapeResponse): string {
    if (this.parcours?.typeParcours === 'LINEAIRE') {
      const etapePrecedente = etape.ordreEtape - 1;
      return `Cette étape est verrouillée. Terminez d'abord l'étape ${etapePrecedente} pour la débloquer.`;
    }
    return 'Cette étape n\'est pas encore accessible.';
  }

  // Nouvelle méthode pour le texte des boutons
  getEtapeButtonText(etape: ParcoursEtapeResponse): string {
    // Si pas inscrit au parcours, toujours "Commencer"
    if (!this.parcours?.isInscrit) {
      return 'Commencer';
    }

    // Si inscrit au parcours
    if (etape.isComplete) {
      return 'Revoir';
    }

    if (etape.isDebloque === true) {
      return 'Commencer';
    }

    // Pour les étapes verrouillées en mode linéaire
    if (this.parcours?.typeParcours === 'LINEAIRE') {
      return `Terminez l'étape ${etape.ordreEtape - 1}`;
    }

    return 'Non disponible';
  }

  // Nouvelle méthode pour savoir si le bouton doit être cliquable
  isEtapeButtonClickable(etape: ParcoursEtapeResponse): boolean {
    // Toujours cliquable si pas inscrit (pour rediriger vers inscription)
    if (!this.parcours?.isInscrit) {
      return true;
    }

    // Cliquable si débloquée (gérer le cas undefined)
    return etape.isDebloque === true;
  }

  // Helper pour vérifier si une étape est débloquée (gestion du undefined)
  isEtapeDebloquee(etape: ParcoursEtapeResponse): boolean {
    return etape.isDebloque === true;
  }

  forcerMiseAJourProgression() {
    if (!this.parcours) return;

    this.parcoursService.forcerMiseAJourProgression(this.parcours.id).subscribe({
      next: () => {
        this.success = 'Progression mise à jour';
        this.loadParcours();
        setTimeout(() => this.success = '', 3000);
      },
      error: (err) => {
        this.error = this.extractErrorMessage(err);
      }
    });
  }

  // Utilitaires d'affichage
  getNiveauDisplayName(niveau: NiveauDifficulte | undefined): string {
    if (!niveau) return 'Non défini';
    return this.parcoursService.getNiveauDisplayName(niveau);
  }

  getNiveauColor(niveau: NiveauDifficulte | undefined): string {
    if (!niveau) return 'bg-gray-100 text-gray-800';
    
    switch (niveau) {
      case NiveauDifficulte.DEBUTANT:
        return 'bg-green-100 text-green-800';
      case NiveauDifficulte.INTERMEDIAIRE:
        return 'bg-yellow-100 text-yellow-800';
      case NiveauDifficulte.AVANCE:
        return 'bg-red-100 text-red-800';
      case NiveauDifficulte.EXPERT:
        return 'bg-purple-100 text-purple-800';
      default:
        return 'bg-gray-100 text-gray-800';
    }
  }

  formatDuree(heures: number | undefined): string {
    if (!heures) return 'Non définie';
    
    if (heures < 1) {
      return `${Math.round(heures * 60)} min`;
    } else if (heures === 1) {
      return '1 heure';
    } else {
      return `${heures} heures`;
    }
  }

  getProgressionColor(progression: number): string {
    if (progression >= 80) return 'bg-green-500';
    if (progression >= 50) return 'bg-yellow-500';
    return 'bg-blue-500';
  }

  getEtapeStatusIcon(etape: ParcoursEtapeResponse): string {
    if (etape.isComplete) return 'check-circle';
    if (etape.isDebloque === true) return 'play-circle';
    return 'lock';
  }

  getEtapeStatusColor(etape: ParcoursEtapeResponse): string {
    if (etape.isComplete) return 'text-green-600';
    if (etape.isDebloque === true) return 'text-blue-600';
    return 'text-gray-400';
  }

  getEtapeStatusText(etape: ParcoursEtapeResponse): string {
    if (etape.isComplete) return 'Terminée';
    if (etape.isDebloque === true) return 'Disponible';
    return 'Verrouillée';
  }

  getNiveauEtapeLabel(niveau: number): string {
    const labels: { [key: number]: string } = {
      1: 'Fondamental',
      2: 'Intermédiaire',
      3: 'Avancé'
    };
    return labels[niveau] || `Niveau ${niveau}`;
  }

  getNiveauEtapeColor(niveau: number): string {
    const colors: { [key: number]: string } = {
      1: 'bg-green-100 text-green-800',
      2: 'bg-yellow-100 text-yellow-800',
      3: 'bg-red-100 text-red-800'
    };
    return colors[niveau] || 'bg-gray-100 text-gray-800';
  }

  extractErrorMessage(error: any): string {
    if (error.error && typeof error.error === 'string') {
      return error.error;
    }
    if (error.message) {
      return error.message;
    }
    return 'Une erreur est survenue';
  }

  clearMessages() {
    this.error = '';
    this.success = '';
  }

  getImageUrl(thumbnailUrl: string | undefined): string {
    if (!thumbnailUrl) return '/assets/images/default-parcours.jpg';
    return this.parcoursService.getImageUrl(thumbnailUrl);
  }

  telechargerCertificat() {
    if (!this.parcours || !this.parcours.certificatGenere) {
      this.error = 'Le certificat n\'est pas disponible pour ce parcours.';
      return;
    }

    // Nous avons besoin de l'ID d'inscription pour télécharger le certificat
    // Pour l'instant, nous utilisons l'ID du parcours comme approximation
    // TODO: Récupérer le vrai ID d'inscription depuis l'API
    const inscriptionId = this.parcours.id; // Temporaire
    
    // Générer un nom de fichier approprié
    const currentProfile = this.authService.getCurrentProfile();
    const userName = (currentProfile?.firstName || '') + '_' + (currentProfile?.lastName || '') || 'Utilisateur';
    const fileName = this.certificateService.generateFileName(this.parcours.titre, userName);
    
    console.log('🏆 Téléchargement certificat:', this.parcours.titre);
    this.certificateService.downloadAndSaveCertificate(inscriptionId, fileName);
  }
}