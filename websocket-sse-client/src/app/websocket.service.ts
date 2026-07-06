import { Injectable } from "@angular/core";
import { Client } from "@stomp/stompjs";
import { Observable, Subject } from "rxjs";
import { webSocket, WebSocketSubject } from "rxjs/webSocket";

@Injectable({
  providedIn: 'root'
})
export class WebSocketService {
  // private stompClient!: Client;
  private messageSubject = new Subject<any>();

  //Utilizando WebSocket do RxJS
  subject = webSocket('ws://localhost:8080/ws-connect');

  initConnection() {
    this.subject.subscribe({
      next: (message) => {
        console.log("Mensagem recebida do WebSocket:", message);
        this.messageSubject.next(message);
      },
      error: (error) => {
        console.error("Erro na conexão WebSocket:", error);
      },
      complete: () => {
        console.log("Conexão WebSocket encerrada.");
      }
    }
    );
  }

  //Utilizando STOMP
  /*initConnection() {
    //const socket = new SockJS('http://localhost:8080/ws-connect');
    console.log("Conectando ao Websocket do Spring!!!");
      this.stompClient = new Client({
       brokerURL: 'ws://localhost:8080/ws-connect',
       debug: (str) => console.log(str),
       reconnectDelay: 5000
     }); 
    /*   this.stompClient.onConnect = (frame: any) => {
        console.log("Conectado ao Websocket do Spring!!!👌");
  
        this.stompClient.subscribe('/topic/messages', (message: any) => {
          if (message.body) {
            this.messageSubject.next(message.body);
          }
        });
      };
      this.stompClient.activate();
    } 
  }

  getMessages(): Observable<string> {
    return this.messageSubject.asObservable();
  }
  */

  getMessages(): Observable<string> {
    return this.messageSubject.asObservable();
  }
  sendMessage(msg: string) {
    this.subject.next(msg);
  }

}