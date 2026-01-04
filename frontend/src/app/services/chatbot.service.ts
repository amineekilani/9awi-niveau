import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export interface ChatbotRequest {
    message: string;
}

export interface ChatbotResponse {
    response: string;
}

@Injectable({
    providedIn: 'root'
})
export class ChatbotService {
    private apiUrl = `${environment.apiUrl}/chatbot`;

    constructor(private http: HttpClient) { }

    sendMessage(message: string): Observable<ChatbotResponse> {
        return this.http.post<ChatbotResponse>(`${this.apiUrl}/chat`, { message });
    }
}
