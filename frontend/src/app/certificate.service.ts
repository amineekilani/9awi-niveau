import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class CertificateService {
  private apiUrl = 'http://localhost:8080/api/certificates';

  constructor(private http: HttpClient) {}

  private getHeaders(): HttpHeaders {
    const token = localStorage.getItem('auth-token');
    return new HttpHeaders({
      'Authorization': `Bearer ${token}`
    });
  }

  /**
   * Télécharger un certificat de parcours par ID de parcours
   */
  downloadCertificateByParcours(parcoursId: number): Observable<Blob> {
    const headers = this.getHeaders();
    return this.http.get(`${this.apiUrl}/download/parcours/${parcoursId}`, {
      headers,
      responseType: 'blob'
    });
  }

  /**
   * Télécharger un certificat de parcours
   */
  downloadCertificate(inscriptionId: number): Observable<Blob> {
    const headers = this.getHeaders();
    return this.http.get(`${this.apiUrl}/download/${inscriptionId}`, {
      headers,
      responseType: 'blob'
    });
  }

  /**
   * Vérifier si un certificat est disponible
   */
  checkCertificate(inscriptionId: number): Observable<{available: boolean, completed: boolean, generated: boolean}> {
    return this.http.get<{available: boolean, completed: boolean, generated: boolean}>(
      `${this.apiUrl}/check/${inscriptionId}`, 
      { headers: this.getHeaders() }
    );
  }

  /**
   * Régénérer un certificat
   */
  regenerateCertificate(inscriptionId: number): Observable<{message: string, url: string}> {
    return this.http.post<{message: string, url: string}>(
      `${this.apiUrl}/regenerate/${inscriptionId}`, 
      {}, 
      { headers: this.getHeaders() }
    );
  }

  /**
   * Télécharger un certificat par ID de parcours et déclencher le téléchargement dans le navigateur
   */
  downloadAndSaveCertificateByParcours(parcoursId: number, fileName: string): void {
    this.downloadCertificateByParcours(parcoursId).subscribe({
      next: (blob) => {
        // Créer un URL temporaire pour le blob
        const url = window.URL.createObjectURL(blob);
        
        // Créer un lien temporaire et déclencher le téléchargement
        const link = document.createElement('a');
        link.href = url;
        link.download = fileName;
        document.body.appendChild(link);
        link.click();
        
        // Nettoyer
        document.body.removeChild(link);
        window.URL.revokeObjectURL(url);
        
        console.log('✅ Certificat téléchargé:', fileName);
      },
      error: (error) => {
        console.error('❌ Erreur téléchargement certificat:', error);
        alert('Erreur lors du téléchargement du certificat. Veuillez réessayer.');
      }
    });
  }

  /**
   * Télécharger un certificat et déclencher le téléchargement dans le navigateur
   */
  downloadAndSaveCertificate(inscriptionId: number, fileName: string): void {
    this.downloadCertificate(inscriptionId).subscribe({
      next: (blob) => {
        // Créer un URL temporaire pour le blob
        const url = window.URL.createObjectURL(blob);
        
        // Créer un lien temporaire et déclencher le téléchargement
        const link = document.createElement('a');
        link.href = url;
        link.download = fileName;
        document.body.appendChild(link);
        link.click();
        
        // Nettoyer
        document.body.removeChild(link);
        window.URL.revokeObjectURL(url);
        
        console.log('✅ Certificat téléchargé:', fileName);
      },
      error: (error) => {
        console.error('❌ Erreur téléchargement certificat:', error);
        alert('Erreur lors du téléchargement du certificat. Veuillez réessayer.');
      }
    });
  }

  /**
   * Générer un nom de fichier pour le certificat
   */
  generateFileName(parcoursTitle: string, userName: string): string {
    const cleanTitle = parcoursTitle.replace(/[^a-zA-Z0-9\s]/g, '').replace(/\s+/g, '_');
    const cleanName = userName.replace(/[^a-zA-Z0-9\s]/g, '').replace(/\s+/g, '_');
    return `Certificat_${cleanTitle}_${cleanName}.pdf`; // PDF maintenant !
  }
}