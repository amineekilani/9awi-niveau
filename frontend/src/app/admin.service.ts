import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface UserAdminResponse {
  id: number;
  email: string;
  firstName: string;
  lastName: string;
  role: string;
  emailVerified: boolean;
  archived: boolean;
  createdAt: number;
  phoneNumber?: string;
  dateOfBirth?: string;
  failedLoginAttempts: number;
  accountLockedUntil?: number;
}

export interface UserStatsResponse {
  totalUsers: number;
  activeUsers: number;
  adminUsers: number;
  formateurUsers: number;
  etudiantUsers: number;
}

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}

export interface CreateUserRequest {
  email: string;
  firstName: string;
  lastName: string;
  role: string;
  phoneNumber?: string;
  dateOfBirth?: string;
  emailVerified?: boolean;
}

export interface UpdateUserRequest {
  firstName: string;
  lastName: string;
  phoneNumber?: string;
  dateOfBirth?: string;
  emailVerified: boolean;
}

export interface BulkActionRequest {
  userIds: number[];
  action: string;
  newRole?: string;
}

@Injectable({
  providedIn: 'root'
})
export class AdminService {
  private apiUrl = 'http://localhost:8080/api/admin';

  constructor(private http: HttpClient) {}

  private getAuthHeaders(): HttpHeaders {
    const token = localStorage.getItem('auth-token');
    return new HttpHeaders({
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    });
  }

  getAllUsers(page: number = 0, size: number = 10, sortBy: string = 'createdAt', sortDir: string = 'desc', search?: string, role?: string, archived?: boolean): Observable<PageResponse<UserAdminResponse>> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString())
      .set('sortBy', sortBy)
      .set('sortDir', sortDir);

    if (search) params = params.set('search', search);
    if (role) params = params.set('role', role);
    if (archived !== undefined) params = params.set('archived', archived.toString());

    return this.http.get<PageResponse<UserAdminResponse>>(`${this.apiUrl}/users`, {
      headers: this.getAuthHeaders(),
      params
    });
  }

  getAllActiveUsers(): Observable<UserAdminResponse[]> {
    return this.http.get<UserAdminResponse[]>(`${this.apiUrl}/users/active`, {
      headers: this.getAuthHeaders()
    });
  }

  getUserById(userId: number): Observable<UserAdminResponse> {
    return this.http.get<UserAdminResponse>(`${this.apiUrl}/users/${userId}`, {
      headers: this.getAuthHeaders()
    });
  }

  changeUserRole(userId: number, role: string): Observable<UserAdminResponse> {
    return this.http.put<UserAdminResponse>(`${this.apiUrl}/users/change-role`, {
      userId,
      role
    }, {
      headers: this.getAuthHeaders()
    });
  }

  toggleUserStatus(userId: number, archived: boolean): Observable<UserAdminResponse> {
    return this.http.put<UserAdminResponse>(`${this.apiUrl}/users/toggle-status`, {
      userId,
      archived
    }, {
      headers: this.getAuthHeaders()
    });
  }

  unlockUserAccount(userId: number): Observable<any> {
    return this.http.put(`${this.apiUrl}/users/${userId}/unlock`, {}, {
      headers: this.getAuthHeaders()
    });
  }

  createUser(request: CreateUserRequest): Observable<UserAdminResponse> {
    return this.http.post<UserAdminResponse>(`${this.apiUrl}/users`, request, {
      headers: this.getAuthHeaders()
    });
  }

  updateUser(userId: number, request: UpdateUserRequest): Observable<UserAdminResponse> {
    return this.http.put<UserAdminResponse>(`${this.apiUrl}/users/${userId}`, request, {
      headers: this.getAuthHeaders()
    });
  }

  deleteUser(userId: number): Observable<any> {
    return this.http.delete(`${this.apiUrl}/users/${userId}`, {
      headers: this.getAuthHeaders()
    });
  }

  bulkAction(request: BulkActionRequest): Observable<any> {
    return this.http.post(`${this.apiUrl}/users/bulk-action`, request, {
      headers: this.getAuthHeaders()
    });
  }

  resetUserPassword(userId: number): Observable<any> {
    return this.http.put(`${this.apiUrl}/users/${userId}/reset-password`, {}, {
      headers: this.getAuthHeaders()
    });
  }

  exportUsers(): Observable<Blob> {
    return this.http.get(`${this.apiUrl}/users/export`, {
      headers: this.getAuthHeaders(),
      responseType: 'blob'
    });
  }

  getUserStats(): Observable<UserStatsResponse> {
    return this.http.get<UserStatsResponse>(`${this.apiUrl}/stats/users`, {
      headers: this.getAuthHeaders()
    });
  }
}