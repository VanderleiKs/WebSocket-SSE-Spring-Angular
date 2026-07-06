import { Injectable, NgZone, Service } from '@angular/core';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class Sse {
  // NgZone é crucial aqui para avisar ao Angular quando os dados chegarem de fora
  constructor(private zone: NgZone) { }

  acompanharMessages(url: string): Observable<any> {
    return new Observable(observer => {
      // 1. Abre a conexão com o Spring MVC
      const eventSource = new EventSource(url);

      // 2. Escuta o evento específico que configuramos no .name() do Java
      eventSource.addEventListener('notificacao-messages', (event: MessageEvent) => {
        // ngZone.run garante que o Angular detecte a mudança na tela imediatamente
        this.zone.run(() => {
          const dadosConvertidos = JSON.parse(event.data);
          observer.next(dadosConvertidos); // Dispara o dado para quem deu subscribe
        });
      });

      // Se der erro ou der o timeout de 30s do Java, o navegador reconecta sozinho
      eventSource.onerror = (error) => {
        this.zone.run(() => {
          console.log('Conexão SSE em espera ou instável. Reconectando automaticamente...');
        });
      };

      // Limpeza obrigatória: se o componente Angular for destruído, fecha o túnel
      return () => {
        eventSource.close();
      };
    });
  }

  ouvirAcompanhamentoRelatorio(url: string): Observable<AcompanharRelatorio> {
    return new Observable<AcompanharRelatorio>(observer => {
      const eventSource = new EventSource(url);

      // Escuta as mensagens normais de segundo a segundo
      eventSource.addEventListener('Processando', (event: MessageEvent) => {
        this.zone.run(() => {
          const dados: AcompanharRelatorio = JSON.parse(event.data);
          observer.next(dados);
        });
      });

      // NOVO: Escuta o sinal de fim enviado pelo Java
      eventSource.addEventListener('fim', () => {
        this.zone.run(() => {
          console.log('Backend avisou que terminou. Fechando conexão definitivamente...');

          eventSource.close(); // Fecha o túnel HTTP imediatamente pelo lado do cliente
          observer.complete(); // Encerra o Observable do RxJS no Angular
        });
      });

      eventSource.onerror = (error) => {
        this.zone.run(() => {
          // Agora, se o fluxo foi completado manualmente pelo evento 'fim',
          // o onerror não causará o ciclo infinito de reconexão.
          console.log('Conexão encerrada ou oscilando.');
        });
      };

      return () => {
        eventSource.close();
      };
    });
  }

}

export interface AcompanharRelatorio {
  status: string;
  porcentagem: number;
}
