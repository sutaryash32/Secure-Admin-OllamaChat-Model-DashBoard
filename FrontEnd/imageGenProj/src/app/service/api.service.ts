import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { CricketResponse } from '../model/cricket.response';

@Injectable({
  providedIn: 'root'
})
export class ApiService {

  private readonly BASE_URL = 'http://localhost:8080/api/v1/chat';

  getStreamedResponse(prompt: string): Observable<string> {
    const url = `${this.BASE_URL}?prompt=${encodeURIComponent(prompt)}`;
    return this.openSseConnection(url);
  }

  getCricketStreamedResponse(prompt: string): Observable<string> {
    const url = `${this.BASE_URL}/cricket?prompt=${encodeURIComponent(prompt)}`;
    return this.openSseConnection(url);
  }

  private openSseConnection(url: string): Observable<string> {
    return new Observable<string>((observer) => {
      const eventSource = new EventSource(url);

      eventSource.onmessage = (event) => {
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

        } catch (e) {
          console.error('Failed to parse SSE message:', e);
        }
      };

      eventSource.onerror = () => {
        if (eventSource.readyState === EventSource.CLOSED) {
          observer.complete();
          eventSource.close();
        } else if (eventSource.readyState === EventSource.CONNECTING) {
          // ✅ close immediately — prevent auto-reconnect causing duplicate responses
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
