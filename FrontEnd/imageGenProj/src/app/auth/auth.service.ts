import { Injectable } from '@angular/core';
import { Router } from '@angular/router';
import { BehaviorSubject, Observable } from 'rxjs';

export interface User {
  username: string;
  email: string;
  role: string;
  token: string;
  refreshToken?: string;
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private currentUserSubject = new BehaviorSubject<User | null>(null);
  public currentUser$ = this.currentUserSubject.asObservable();

  private readonly TOKEN_KEY = 'auth_token';
  private readonly REFRESH_TOKEN_KEY = 'refresh_token';
  private readonly USER_KEY = 'current_user';

  constructor(private router: Router) {
    this.loadStoredUser();
  }

  private loadStoredUser(): void {
    const token = localStorage.getItem(this.TOKEN_KEY);
    const userJson = localStorage.getItem(this.USER_KEY);
    if (token && userJson) {
      try {
        const user = JSON.parse(userJson);
        this.currentUserSubject.next(user);
      } catch (e) {
        console.error('Error parsing stored user:', e);
        this.logout();
      }
    }
  }

  login(username: string, password: string): Observable<any> {
    return new Observable(observer => {
      setTimeout(() => {
        const mockResponse = {
          token: 'mock_jwt_token_' + Date.now(),
          refreshToken: 'mock_refresh_token_' + Date.now(),
          username: username,
          role: username === 'admin' ? 'ROLE_ADMIN' : 'ROLE_USER',
          email: username + '@example.com'
        };
        this.handleOAuth2Success(
          mockResponse.token,
          mockResponse.refreshToken,
          mockResponse.username,
          mockResponse.role,
          mockResponse.email
        );
        observer.next(mockResponse);
        observer.complete();
      }, 500);
    });
  }

  register(username: string, email: string, password: string): Observable<any> {
    return new Observable(observer => {
      setTimeout(() => {
        const mockResponse = {
          token: 'mock_jwt_token_' + Date.now(),
          refreshToken: 'mock_refresh_token_' + Date.now(),
          username: username,
          role: 'ROLE_USER',
          email: email
        };
        this.handleOAuth2Success(
          mockResponse.token,
          mockResponse.refreshToken,
          mockResponse.username,
          mockResponse.role,
          mockResponse.email
        );
        observer.next(mockResponse);
        observer.complete();
      }, 500);
    });
  }

  handleOAuth2Success(
    token: string,
    refreshToken: string | null,
    username: string,
    role: string,
    email: string
  ): void {
    const user: User = {
      username,
      email,
      role,
      token,
      refreshToken: refreshToken || undefined
    };

    localStorage.setItem(this.TOKEN_KEY, token);
    if (refreshToken) {
      localStorage.setItem(this.REFRESH_TOKEN_KEY, refreshToken);
    }
    localStorage.setItem(this.USER_KEY, JSON.stringify(user));

    this.currentUserSubject.next(user);
  }

  logout(): void {
    localStorage.removeItem(this.TOKEN_KEY);
    localStorage.removeItem(this.REFRESH_TOKEN_KEY);
    localStorage.removeItem(this.USER_KEY);
    this.currentUserSubject.next(null);
    this.router.navigate(['/login']);
  }

  getToken(): string | null {
    return localStorage.getItem(this.TOKEN_KEY);
  }

  getRefreshToken(): string | null {
    return localStorage.getItem(this.REFRESH_TOKEN_KEY);
  }

  getCurrentUser(): User | null {
    return this.currentUserSubject.value;
  }

  isAuthenticated(): boolean {
    return !!this.getToken();
  }

  isAdmin(): boolean {
    const user = this.getCurrentUser();
    return user?.role === 'ROLE_ADMIN';
  }

  refreshToken(): Observable<any> {
    const refreshTokenValue = this.getRefreshToken();
    if (!refreshTokenValue) {
      return new Observable(observer => {
        observer.error('No refresh token');
      });
    }

    return new Observable(observer => {
      setTimeout(() => {
        const newToken = 'refreshed_jwt_token_' + Date.now();
        localStorage.setItem(this.TOKEN_KEY, newToken);

        const currentUser = this.getCurrentUser();
        if (currentUser) {
          currentUser.token = newToken;
          localStorage.setItem(this.USER_KEY, JSON.stringify(currentUser));
          this.currentUserSubject.next(currentUser);
        }

        observer.next({ token: newToken });
        observer.complete();
      }, 500);
    });
  }
}
