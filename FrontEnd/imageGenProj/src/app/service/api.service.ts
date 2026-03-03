import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { AuthService } from '../auth/auth.service';

@Injectable({
  providedIn: 'root'
})
export class ApiService {

  private readonly BASE_URL = 'http://localhost:8080/api/v1';
  private readonly ADMIN_URL = 'http://localhost:8080/api/v1/admin';

  constructor(private authService: AuthService) {}

  // ── Chat (SSE via fetch — supports Authorization header) ──

  getStreamedResponse(prompt: string): Observable<string> {
    const url = `${this.BASE_URL}/chat?prompt=${encodeURIComponent(prompt)}`;
    return this.fetchSse(url);
  }

  getCricketStreamedResponse(prompt: string): Observable<string> {
    const url = `${this.BASE_URL}/chat/cricket?prompt=${encodeURIComponent(prompt)}`;
    return this.fetchSse(url);
  }

  // ── Admin ─────────────────────────────────────────────────

  getAllUsers(): Observable<any> {
    return new Observable(observer => {
      fetch(`${this.ADMIN_URL}/users`, { headers: this.authHeaders() })
        .then(res => res.json())
        .then(data => { observer.next(data); observer.complete(); })
        .catch(err => observer.error(err));
    });
  }

  updateUserRole(id: number, role: string): Observable<any> {
    return new Observable(observer => {
      fetch(`${this.ADMIN_URL}/users/${id}/role`, {
        method: 'PUT',
        headers: this.authHeaders(),
        body: JSON.stringify({ role })
      })
        .then(res => res.json())
        .then(data => { observer.next(data); observer.complete(); })
        .catch(err => observer.error(err));
    });
  }

  deleteUser(id: number): Observable<any> {
    return new Observable(observer => {
      fetch(`${this.ADMIN_URL}/users/${id}`, {
        method: 'DELETE',
        headers: this.authHeaders()
      })
        .then(res => {
          if (res.ok) { observer.next({ success: true }); observer.complete(); }
          else observer.error(new Error('Failed to delete user'));
        })
        .catch(err => observer.error(err));
    });
  }

  getChatAnalytics(): Observable<any> {
    return new Observable(observer => {
      fetch(`${this.ADMIN_URL}/analytics`, { headers: this.authHeaders() })
        .then(res => res.json())
        .then(data => { observer.next(data); observer.complete(); })
        .catch(err => observer.error(err));
    });
  }

  // ── Private helpers ───────────────────────────────────────

  private authHeaders(): HeadersInit {
    const token = this.authService.getToken();
    return {
      'Content-Type': 'application/json',
      ...(token ? { 'Authorization': `Bearer ${token}` } : {})
    };
  }

  // ✅ Uses fetch() instead of EventSource so we can send Authorization header
  private fetchSse(url: string): Observable<string> {
    return new Observable(observer => {
      const token = this.authService.getToken();
      let cancelled = false;

      fetch(url, {
        headers: {
          'Accept': 'text/event-stream',
          ...(token ? { 'Authorization': `Bearer ${token}` } : {})
        }
      }).then(response => {
        if (!response.ok) {
          observer.error(`HTTP error: ${response.status}`);
          return;
        }

        const reader = response.body!.getReader();
        const decoder = new TextDecoder();
        let buffer = '';

        const read = () => {
          if (cancelled) return;
          reader.read().then(({ done, value }) => {
            if (done) { observer.complete(); return; }

            buffer += decoder.decode(value, { stream: true });
            const lines = buffer.split('\n');
            buffer = lines.pop() || '';  // keep incomplete last line

            for (const line of lines) {
              if (line.startsWith('data:')) {
                const jsonStr = line.slice(5).trim();
                if (!jsonStr) continue;
                try {
                  const data = JSON.parse(jsonStr);
                  if (data.content === '[DONE]') {
                    observer.complete();
                    return;
                  }
                  if (data.content) observer.next(data.content);
                } catch (e) {
                  console.error('Failed to parse SSE chunk:', e);
                }
              }
            }
            read(); // read next chunk
          }).catch(err => observer.error(err));
        };

        read();
      }).catch(err => observer.error(err));

      // cleanup on unsubscribe
      return () => { cancelled = true; };
    });
  }
}
