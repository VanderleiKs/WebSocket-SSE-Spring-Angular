# WebsocketClient
# Frontend: Consumindo Server-Sent Events (SSE) no Angular

Este projeto consome um fluxo de dados contínuo (Streaming) enviado pelo backend via SSE, renderizando em tempo real uma barra de progresso responsiva utilizando **Tailwind CSS**.

## 🚀 Decisões de Arquitetura (O que fizemos e por quê?)

### 1. Por que encapsular o EventSource em um Observable do RxJS?
A API nativa do navegador (`EventSource`) trabalha com callbacks puros no estilo Javascript antigo. Ao envelopá-la em um `Observable`, integramos o fluxo de dados diretamente no ecossistema reativo do Angular, permitindo gerenciar o ciclo de vida da conexão com métodos como `.subscribe()` e `.unsubscribe()`.

### 2. O papel crítico do `NgZone.run()`
A API `EventSource` opera de forma assíncrona fora da infraestrutura de monitoramento padrão do Angular. Sem o `this.zone.run()`, os dados chegariam do Java, as variáveis do TypeScript seriam atualizadas na memória, mas o mecanismo de **Change Detection** do Angular ficaria cego. A tela permaneceria congelada até que o usuário forçasse uma interação física. O `NgZone` força a atualização visual no exato milissegundo em que o dado atinge a rede.

### 3. A Estratégia de Encerramento Limpo (Evitando loops infinitos)
Por padrão, se o servidor fecha uma conexão SSE (via timeout ou finalização), o navegador interpreta como queda de sinal e tenta **reconectar infinitamente a cada 3 segundos**. Criamos o evento personalizado `'fim'` enviado pelo Java. Quando o Angular captura esse evento, ele executa explicitamente o método `eventSource.close()`, abortando o loop de reconexão do navegador de forma definitiva.

---

## 💻 Implementação do Código

### 1. Interface de Dados (`progresso-sse.model.ts`)
```typescript
export interface ProgressoSse {
  status: 'PROCESSANDO' | 'CONCLUIDO';
  porcentagem?: number;
  url?: string;
}
```

### 2. O Serviço Reativo (`sse.service.ts`)
```typescript
import { Injectable, NgZone } from '@angular/core';
import { Observable } from 'rxjs';
import { ProgressoSse } from '../models/progresso-sse.model';

@Injectable({
  providedIn: 'root'
})
export class SseService {

  constructor(private zone: NgZone) { }

  ouvirProgresso(url: string): Observable<ProgressoSse> {
    return new Observable<ProgressoSse>(observer => {
      const eventSource = new EventSource(url);

      // Escuta canal parcial enviado via .name("progresso") no Java
      eventSource.addEventListener('progresso', (event: MessageEvent) => {
        this.zone.run(() => {
          const dados: ProgressoSse = JSON.parse(event.data);
          observer.next(dados); // Empurra para o componente
        });
      });

      // Escuta canal de término enviado via .name("fim") no Java
      eventSource.addEventListener('fim', () => {
        this.zone.run(() => {
          eventSource.close();   // Mata a conexão física do navegador
          observer.complete();  // Encerra o fluxo do RxJS de forma definitiva
        });
      });

      eventSource.onerror = (error) => {
        this.zone.run(() => {
          console.warn('Conexão finalizada ou instável na rede.');
        });
      };

      // Destrutor de Segurança: Executa se o usuário sair da rota do componente
      return () => {
        eventSource.close();
      };
    });
  }
}
```

### 3. O Componente (`relatorio.component.ts`)
```typescript
import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { SseService } from '../../services/sse.service';
import { Subscription } from 'rxjs';

@Component({
  selector: 'app-relatorio',
  standalone: true,
  imports: [CommonModule], // Libera o uso de [ngClass] e *ngIf no HTML standalone
  templateUrl: './relatorio.component.html'
})
export class RelatorioComponent implements OnInit, OnDestroy {

  statusProcesso = 'Aguardando inicialização...';
  porcentagemProgresso = 0;
  linkDownload: string | null = null;
  private sseSubscription!: Subscription;

  constructor(private sseService: SseService) { }

  ngOnInit(): void {
    const urlBackend = 'http://localhost:8080/api/v1/relatorios/processar';

    this.sseSubscription = this.sseService.ouvirProgresso(urlBackend).subscribe({
      next: (evento) => {
        if (evento.status === 'PROCESSANDO') {
          this.statusProcesso = 'Extraindo dados da base corporativa...';
          this.porcentagemProgresso = evento.porcentagem || 0;
        }
      },
      error: (err) => {
        this.statusProcesso = 'Ocorreu uma falha crítica no processamento.';
        console.error(err);
      },
      complete: () => {
        // Executado no segundo exato em que o service dispara o observer.complete()
        this.statusProcesso = 'Relatório gerado com sucesso!';
        this.porcentagemProgresso = 100;
        this.linkDownload = 'http://localhost:8080/arquivos/relatorio.pdf';
      }
    });
  }

  ngOnDestroy(): void {
    if (this.sseSubscription) {
      this.sseSubscription.unsubscribe(); // Previne vazamentos de memória (Memory Leak)
    }
  }
}
```

### 4. O Design Responsivo (`relatorio.component.html`)
```html
<div class="min-h-screen bg-gray-100 flex items-center justify-center p-6">
  <div class="w-full max-w-md bg-white rounded-xl shadow-md p-6 border border-gray-200">
    
    <!-- Status Superior -->
    <div class="flex justify-between items-center mb-2">
      <span class="text-sm font-medium text-gray-700">{{ statusProcesso }}</span>
      <span class="text-sm font-semibold text-blue-600">{{ porcentagemProgresso }}%</span>
    </div>

    <!-- Container Físico da Barra de Progresso -->
    <div class="w-full bg-gray-200 rounded-full h-4 mb-6 overflow-hidden">
      <!-- Binding de Estilo Dinâmico do Angular unido a utilitários Tailwind -->
      <div class="h-4 rounded-full transition-all duration-500 ease-out"
           [style.width.%]="porcentagemProgresso"
           [ngClass]="{
              'bg-blue-600 animate-pulse': porcentagemProgresso < 100, 
              'bg-green-500': porcentagemProgresso === 100
           }">
      </div>
    </div>

    <!-- Feedback Visual Inferior Dinâmico -->
    <div *ngIf="porcentagemProgresso < 100" class="flex items-center justify-center space-x-2 text-sm text-gray-500 bg-gray-50 py-3 rounded-lg border border-dashed border-gray-300">
      <span class="inline-block animate-spin rounded-full h-4 w-4 border-2 border-blue-600 border-t-transparent"></span>
      <span>O servidor está processando. Não mude de página.</span>
    </div>

    <div *ngIf="porcentagemProgresso === 100 && linkDownload" class="animate-bounce">
      <a [href]="linkDownload" target="_blank" class="w-full flex items-center justify-center space-x-2 bg-green-600 hover:bg-green-700 text-white font-medium py-3 rounded-lg shadow transition">
        <span>Baixar Documento PDF</span>
      </a>
    </div>

  </div>
</div>
```

# Backend: Server-Sent Events (SSE) com Spring Boot MVC

Este microsserviço gerencia o processamento assíncrono de tarefas pesadas (ex: geração de relatórios) e notifica o frontend em tempo real utilizando **Server-Sent Events (SSE)** sobre a infraestrutura estável do HTTP tradicional.

## 🚀 Decisões de Arquitetura (O que fizemos e por quê?)

### 1. Por que escolhemos SSE em vez de WebSocket puro ou STOMP?
* **Unidirecionalidade:** O caso de uso exige apenas que o servidor envie atualizações de progresso para o cliente (Server-to-Client). O cliente não envia dados em tempo real durante o processo.
* **Simplicidade de Infraestrutura:** O SSE roda sobre o protocolo HTTP padrão (`text/event-stream`). Passa facilmente por firewalls/proxies corporativos e possui **reconexão automática nativa** gerenciada pelo navegador, sem necessidade de bibliotecas extras.

### 2. Por que usamos Spring MVC + `@Async` em vez de Spring WebFlux?
* **Maturidade e Compatibilidade:** O mercado corporativo massivo utiliza ecossistemas baseados em Servlets e bancos de dados relacionais (JPA/Hibernate) com drivers síncronos. O WebFlux exigiria drivers reativos (R2DBC) em toda a stack, elevando a complexidade.
* **A Revolução do Java Moderno (Virtual Threads):** Ao habilitar as Virtual Threads do Java, mitigamos o antigo gargalo do Spring MVC (prender uma thread física por conexão). O servidor agora escala de forma reativa mantendo o código imperativo simples.

### 3. A Regra de Ouro do Proxy do Spring (A separação em classes)
* **O Erro (Self-Invocation):** Se o método `@Async` for colocado dentro da mesma classe do `@RestController` e chamado diretamente, o Spring **ignora o Proxy** de interceptação. O código executa de forma síncrona na thread do Tomcat, represando o buffer de rede e enviando todas as mensagens de uma vez só no final.
* **A Solução:** O método `@Async` deve obrigatoriamente residir em uma classe `@Service` separada. A chamada atravessa a barreira do Proxy do Spring, disparando o paralelismo real e permitindo o gotejamento dos dados na rede segundo a segundo.

---

## 🛠️ Configuração do Ambiente (`application.properties`)

```properties
# Ativa o suporte massivo a escala utilizando Virtual Threads (Java 21+)
spring.threads.virtual.enabled=true

# GARANTIA DE STREAMING: Desativa a compressão global de HTTP.
# Se true, o Tomcat acumula o stream para zipar, quebrando o tempo real do SSE.
server.compression.enabled=false
```

---

## 💻 Implementação do Código

### 1. Classe Principal (Ativação do Recurso)
```java
@EnableAsync // OBRIGATÓRIO para ativar o processamento em background do @Async
@SpringBootApplication
public class SseApplication {
    public static void main(String[] args) {
        SpringApplication.run(SseApplication.class, args);
    }
}
```

### 2. Camada de Controle (`RelatorioController.java`)
```java
@CrossOrigin(origins = "http://localhost:4200") // Evita bloqueio de CORS do Angular
@RestController
@RequestMapping("/api/v1/relatorios")
public class RelatorioController {

    private final RelatorioService relatorioService;

    public RelatorioController(RelatorioService relatorioService) {
        this.relatorioService = relatorioService;
    }

    @GetMapping(value = "/processar", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter processarRelatorio() {
        // Timeout de segurança de 5 minutos (300.000 ms)
        SseEmitter emitter = new SseEmitter(300000L);

        // Dispara a tarefa pesada através do Proxy do Service (Assíncrono real)
        relatorioService.executarTarefaPesada(emitter);

        // Retorna IMEDIATAMENTE (em < 1ms), abrindo o canal HTTP streaming com o cliente
        return emitter;
    }
}
```

### 3. Camada de Serviço (`RelatorioService.java`)
```java
@Service
public class RelatorioService {

    /**
     * Executa o processamento do relatório de forma assíncrona.
     * Envia o progresso e o sinal de término usando blocos nomeados para o Angular.
     */
    @Async
    public void executarTarefaPesada(SseEmitter emitter) {
        try {
            // Simulando etapas de processamento de negócio
            for (int porcentagem = 10; porcentagem <= 100; porcentagem += 30) {
                Thread.sleep(1500); // Simula carga/I/O pesado
                
                // Envia evento parcial nomeado como "progresso"
                emitter.send(SseEmitter.event()
                        .name("progresso")
                        .data("{\"status\":\"PROCESSANDO\", \"porcentagem\":" + porcentagem + "}"));
            }

            // --- FIM DA TAREFA ---
            // Poison Pill/Sinalizador: Avisa o frontend para fechar a conexão de forma limpa
            emitter.send(SseEmitter.event()
                    .name("fim")
                    .data("{\"status\":\"CONCLUIDO\", \"url\":\"/arquivos/relatorio.pdf\"}"));
            
            emitter.complete(); // Encerra o emitter no servidor

        } catch (Exception e) {
            // Se o usuário fechar a aba (IOException) ou ocorrer erro interno, limpa recursos
            emitter.completeWithError(e);
        }
    }
}
```
