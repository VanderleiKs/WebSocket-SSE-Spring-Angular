import { HttpClient } from "@angular/common/http";
import { Injectable } from "@angular/core";
import { Observable } from "rxjs/internal/Observable";

@Injectable({
  providedIn: 'root'
})
export class RestService {
  private URL_BASE = 'http://localhost:8080/api';
  constructor(private http: HttpClient) { }

  sendMessage(message: string): Observable<ChatMessage> {
    const url = `${this.URL_BASE}/chat`;
    return this.http.post<ChatMessage>(url, { message });
  }

  acompanharMessages(): Observable<ChatMessage> {
    return this.http.get<ChatMessage>(`${this.URL_BASE}/acompanhar`);
  }

  processarRelatorio(): Observable<void> {
    return this.http.get<void>(this.URL_BASE + "/processar-relatorio")
  }

}

export interface ChatMessage {
  message: string;
  timestamp: string;
}