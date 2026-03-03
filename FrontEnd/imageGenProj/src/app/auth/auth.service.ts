import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, BehaviorSubject, tap } from 'rxjs';

export interface AuthResponse {
  token: string;
  refreshToken: string;
  username: string;
  email: string;
  role: string;
  expiresIn: number;
}

export interface User {
  username: string;
  email: string;
  role: string;
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private readonly API_URL = 'http://localhost:8080/api/v1/auth';
  private readonly TOKEN_KEY = 'auth_token';
  private readonly REFRESH_TOKEN_KEY = 'refresh_token';
  private readonly USER_KEY = 'user';

  private currentUserSubject = new BehaviorSubject<User | null>(null);
  public currentUser$ = this.currentUserSubject.asObservable();

  constructor(private http: HttpClient) {
    this.loadUserFromStorage();
  }

  private loadUserFromStorage(): void {
    const userJson = localStorage.getItem(this.USER_KEY);
    if (userJson) {
      this.currentUserSubject.next(JSON.parse(userJson));
    }
  }

  login(username: string, password: string): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.API_URL}/login`, { username, password })
      .pipe(tap(response => this.handleAuthResponse(response)));
  }

  register(username: string, email: string, password: string, role?: string): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.API_URL}/register`, { username, email, password, role })
      .pipe(tap(response => this.handleAuthResponse(response)));
  }

  refreshToken(): Observable<AuthResponse> {
    const refreshToken = this.getRefreshToken();
    return this.http.post<AuthResponse>(`${this.API_URL}/refresh`, { refreshToken })
      .pipe(tap(response => this.handleAuthResponse(response)));
  }

  logout(): void {
    localStorage.removeItem(this.TOKEN_KEY);
    localStorage.removeItem(this.REFRESH_TOKEN_KEY);
    localStorage.removeItem(this.USER_KEY);
    this.currentUserSubject.next(null);
  }

  getToken(): string | null {
    return localStorage.getItem(this.TOKEN_KEY);
  }

  getRefreshToken(): string | null {
    return localStorage.getItem(this.REFRESH_TOKEN_KEY);
  }

  isAuthenticated(): boolean {
    return !!this.getToken();
  }

  isAdmin(): boolean {
    const user = this.currentUserSubject.value;
    return user?.role === 'ROLE_ADMIN';
  }

  getCurrentUser(): User | null {
    return this.currentUserSubject.value;
  }

  private handleAuthResponse(response: AuthResponse): void {
    localStorage.setItem(this.TOKEN_KEY, response.token);
    localStorage.setItem(this.REFRESH_TOKEN_KEY, response.refreshToken);

    const user: User = {
      username: response.username,
      email: response.email,
      role: response.role
    };
    localStorage.setItem(this.USER_KEY, JSON.stringify(user));
    this.currentUserSubject.next(user);
  }
}
