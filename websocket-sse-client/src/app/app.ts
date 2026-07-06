import { Component, Signal, signal } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { WebSocketService } from './websocket.service';
import { CommonModule } from '@angular/common';
import { FormsModule, NgModel } from '@angular/forms';
import { RestService } from './rest.service';
import { Sse } from './services/sse';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, CommonModule, FormsModule],
  providers: [NgModel],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App {
  newMessage: any;
  chatHistory = signal([] as string[]);

  protected readonly title = signal('websocket-client');

  constructor(
    private websocketService: WebSocketService,
    private restService: RestService,
    private sse: Sse
  ) {

    const socket = new WebSocket('');
    websocketService.initConnection();
    websocketService.getMessages().subscribe((message: string) => {
      this.chatHistory.update((chatHistory) => [...chatHistory, message]);
    });
  }

  acompanharMessages() {
    const url = 'http://localhost:8080/api/acompanhar'
    this.sse.acompanharMessages(url).subscribe({
      next: (message) => {
        this.chatHistory.update((chatHistory) => [message.message, ...chatHistory])
      }
    })
  }


  send() {
    //this.websocketService.sendMessage(this.newMessage);
    this.restService.sendMessage(this.newMessage).subscribe({
      next: (response) => {
        console.log('Mensagem enviada com sucesso:', response);
      },
      error: (error) => {
        console.error('Erro ao enviar mensagem:', error);
      }
    });
  }
}
