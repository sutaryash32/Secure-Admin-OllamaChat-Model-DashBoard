import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { CricketResponse } from '../model/cricket.response';
import { AuthService } from '../auth/auth.service';

@Injectable({
  providedIn: 'root'
})
export class ApiService {

  private readonly BASE_URL = 'http://localhost:8080/api/v1';
  private readonly ADMIN_URL = 'http://localhost:8080/api/v1/admin';

  constructor(private authService: AuthService) {}

  getStreamedResponse(prompt: string): Observable<string> {
    const url = `${this.BASE_URL}/chat?prompt=${encodeURIComponent(prompt)}`;
    return this.openSseConnection(url);
  }

  getCricketStreamedResponse(prompt: string): Observable<string> {
    const url = `${this.BASE_URL}/chat/cricket?prompt=${encodeURIComponent(prompt)}`;
    return this.openSseConnection(url);
  }

  getAllUsers(): Observable<any> {
    return new Observable<any>((observer: any) => {
      fetch(`${this.ADMIN_URL}/users`, {
        headers: this.getAuthHeaders()
      })
      .then((res: any) => res.json())
      .then((data: any) => {
        observer.next(data);
        observer.complete();
      })
      .catch((err: any) => observer.error(err));
    });
  }

  updateUserRole(username: string, role: string): Observable<any> {
    return new Observable<any>((observer: any) => {
      fetch(`${this.ADMIN_URL}/users/${username}/role`, {
        method: 'PUT',
        headers: this.getAuthHeaders(),
        body: JSON.stringify({ role })
      })
      .then((res: any) => res.json())
      .then((data: any) => {
        observer.next(data);
        observer.complete();
      })
      .catch((err: any) => observer.error(err));
    });
  }

  deleteUser(username: string): Observable<any> {
    return new Observable<any>((observer: any) => {
      fetch(`${this.ADMIN_URL}/users/${username}`, {
        method: 'DELETE',
        headers: this.getAuthHeaders()
      })
      .then((res: any) => {
        if (res.ok) {
          observer.next({ success: true });
          observer.complete();
        } else {
          observer.error(new Error('Failed to delete user'));
        }
      })
      .catch((err: any) => observer.error(err));
    });
  }

  getChatAnalytics(): Observable<any> {
    return new Observable<any>((observer: any) => {
      fetch(`${this.ADMIN_URL}/analytics`, {
        headers: this.getAuthHeaders()
      })
      .then((res: any) => res.json())
      .then((data: any) => {
        observer.next(data);
        observer.complete();
      })
      .catch((err: any) => observer.error(err));
    });
  }

  private getAuthHeaders(): HeadersInit {
    const token = this.authService.getToken();
    return {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`
    };
  }

  private openSseConnection(url: string): Observable<string> {
    return new Observable<string>((observer: any) => {
      const token = this.authService.getToken();
      const authUrl = token ? `${url}&Authorization=Bearer=${encodeURIComponent(token)}` : url;

      const eventSource = new EventSource(authUrl);

      eventSource.onmessage = (event: MessageEvent) => {
        try {
          const data: CricketResponse = JSON.parse(event.data);

          if (data.content === '[DONE]') {
            observer.complete();
            eventSource.close();
            return;
          }

          if (data.content) {
            observer.next(data.content);
          }

        } catch (e: any) {
          console.error('Failed to parse SSE message:', e);
        }
      };

      eventSource.onerror = () => {
        if (eventSource.readyState === EventSource.CLOSED) {
          observer.complete();
          eventSource.close();
        } else if (eventSource.readyState === EventSource.CONNECTING) {
          eventSource.close();
          observer.complete();
        } else {
          observer.error('SSE connection error');
          eventSource.close();
        }
      };

      return () => eventSource.close();
    });
  }
}
