import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse, HttpHeaders } from '@angular/common/http';
import { Router } from '@angular/router';
import { BehaviorSubject, Observable, throwError, of } from 'rxjs';
import { tap, catchError, shareReplay } from 'rxjs/operators';

export interface Profile {
  id?: number;
  email?: string;
  provider?: string;
  emailVerified?: boolean;
  firstName?: string;
  lastName?: string;
  dateOfBirth?: string;
  profileImage?: string;
  role?: string;
  createdAt?: number;
  phoneNumber?: string;
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  private apiUrl = 'http://localhost:8080/api/auth';
  private profileUrl = 'http://localhost:8080/api/profile';
  private tokenKey = 'auth-token';
  private emailKey = 'auth-email';
  private roleKey = 'auth-role';
  private loggedIn = new BehaviorSubject<boolean>(this.hasToken());

  // Profile management
  private userProfileSubject = new BehaviorSubject<Profile | null>(null);
  userProfile$ = this.userProfileSubject.asObservable();
  private profileLoaded = false;

  constructor(private http: HttpClient, private router: Router) {
    // Si on est connecté, on charge le profil au démmarage
    if (this.hasToken()) {
      this.loadUserProfile();
    }
  }

  loadUserProfile(): void {
    if (!this.hasToken()) return;

    this.http.get<Profile>(this.profileUrl, { headers: this.getAuthHeaders() }).subscribe({
      next: (profile) => {
        const normalized = this.normalizeProfile(profile);
        this.userProfileSubject.next(normalized);
        this.profileLoaded = true;
      },
      error: (err) => console.error('Erreur chargement profil', err)
    });
  }

  updateState(profile: Profile) {
    const normalized = this.normalizeProfile(profile);
    this.userProfileSubject.next(normalized);
  }

  private normalizeProfile(profile: Profile): Profile {
    if (!profile || !profile.profileImage) return profile;
    if (profile.profileImage.startsWith('http') || profile.profileImage.startsWith('data:')) {
      return profile;
    }
    return {
      ...profile,
      profileImage: `http://localhost:8080/images/users/${profile.profileImage}`
    };
  }

  getProfileImage(): Observable<string | undefined> {
    return new Observable(observer => {
      if (this.userProfileSubject.value) {
        observer.next(this.userProfileSubject.value.profileImage);
        observer.complete();
      } else {
        this.userProfile$.subscribe(p => {
          observer.next(p?.profileImage);
          // Ne pas compléter ici car c'est un flux continu, mais pour un usage one-shot c'est ok
        });
      }
    });
  }

  login(credentials: { email: string; password: string }): Observable<any> {
    return this.http.post(`${this.apiUrl}/login`, credentials).pipe(
      tap((res: any) => {
        localStorage.setItem(this.tokenKey, res.token);
        localStorage.setItem(this.emailKey, res.email);
        localStorage.setItem(this.roleKey, res.role);
        this.loggedIn.next(true);
      }),
      catchError((error: HttpErrorResponse) => {
        // S'assurer que l'erreur contient le message du backend
        return throwError(() => error);
      })
    );
  }

  loginWithGoogle(googleToken: string): Observable<any> {
    return this.http.post(`${this.apiUrl}/google`, { token: googleToken }).pipe(
      tap((res: any) => {
        localStorage.setItem(this.tokenKey, res.token);
        localStorage.setItem(this.emailKey, res.email);
        localStorage.setItem(this.roleKey, res.role);
        this.loggedIn.next(true);
      }),
      catchError((error: HttpErrorResponse) => {
        return throwError(() => error);
      })
    );
  }

  register(credentials: { email: string; password: string; firstName: string; lastName: string; dateOfBirth: string; phoneNumber?: string }): Observable<any> {
    return this.http.post(`${this.apiUrl}/register`, credentials, { responseType: 'text' as 'json' });
  }

  logout() {
    localStorage.removeItem(this.tokenKey);
    localStorage.removeItem(this.emailKey);
    localStorage.removeItem(this.roleKey);
    this.loggedIn.next(false);
    this.router.navigate(['/login']);
  }

  getToken(): string | null {
    return localStorage.getItem(this.tokenKey);
  }

  getEmail(): string | null {
    return localStorage.getItem(this.emailKey);
  }

  getRole(): string | null {
    return localStorage.getItem(this.roleKey);
  }

  isFormateur(): boolean {
    return this.getRole() === 'FORMATEUR';
  }

  isAdmin(): boolean {
    return this.getRole() === 'ADMIN';
  }

  isLoggedIn(): Observable<boolean> {
    return this.loggedIn.asObservable();
  }

  private hasToken(): boolean {
    return !!localStorage.getItem(this.tokenKey);
  }

  verifyEmail(token: string): Observable<any> {
    return this.http.get(`${this.apiUrl}/verify-email?token=${token}`);
  }

  forgotPassword(email: string): Observable<any> {
    return this.http.post(`${this.apiUrl}/forgot-password`, { email });
  }

  resetPassword(token: string, newPassword: string): Observable<any> {
    return this.http.post(`${this.apiUrl}/reset-password`, { token, newPassword });
  }

  uploadProfileImage(file: File): Observable<any> {
    const formData = new FormData();
    formData.append('file', file);

    const headers = this.getAuthHeaders();
    return this.http.post('http://localhost:8080/api/profile/upload-image', formData, { headers });
  }

  uploadProfileImageAfterRegister(file: File, email: string): Observable<any> {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('email', email);

    // No authentication needed for post-registration upload
    return this.http.post('http://localhost:8080/api/profile/upload-image-after-register', formData);
  }

  private getAuthHeaders() {
    const token = this.getToken();
    const headerObj: any = {};
    if (token) {
      headerObj['Authorization'] = `Bearer ${token}`;
    }
    // Note: Don't set Content-Type, let the browser set it automatically for FormData
    return new HttpHeaders(headerObj);
  }
}