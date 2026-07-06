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
