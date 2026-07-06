package com.demo.websocket.relatorios;

import java.io.IOException;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Service
public class GerarRelatorioService {

  /**
   * Executa o processamento do relatório de forma assíncrona e envia o progresso
   * em tempo real para o cliente via Server-Sent Events (SSE).
   * <p>
   * <b>Mecânica de Funcionamento:</b>
   * O método roda em uma thread em background separada (Thread Pool gerenciado
   * pelo Spring
   * ou Virtual Thread no Java 21+), permitindo que o Controller libere a
   * requisição HTTP
   * inicial imediatamente. (DEVE FICAR EM CLASSE SEPARADA DO CONTROLLER, senão
   * continua single thread)
   * </p>
   * <p>
   * <b>Eventos disparados para o Frontend (Angular):</b>
   * <ul>
   * <li><b>"progresso":</b> Enviado periodicamente com o percentual atual da
   * tarefa.</li>
   * <li><b>"fim":</b> Enviado quando a tarefa atinge 100%, sinalizando
   * sucesso.</li>
   * </ul>
   * </p>
   * <p>
   * <b>Gerenciamento de Recursos:</b>
   * Ao finalizar ou disparar uma {@link IOException} (caso o usuário feche o
   * navegador),
   * o canal é encerrado via {@code emitter.complete()} para liberar os recursos
   * do servidor.
   * </p>
   * 
   * @param emitter Instância de {@link SseEmitter} criada pelo Controller para
   *                manter
   *                o canal de streaming aberto com o cliente.
   * @see org.springframework.scheduling.annotation.Async
   * @see org.springframework.web.servlet.mvc.method.annotation.SseEmitter
   */
  // 2. A tarefa roda de forma assíncrona (em outra thread)
  @Async
  public void executarTarefaPesadaEmBackground(SseEmitter emitter) {
    try {
      // Simulando etapas de uma tarefa real (ex: buscar dados, gerar PDF, salvar S3)
      for (int porcentagem = 1; porcentagem <= 100; porcentagem += 1) {
        Thread.sleep(500); // Simulando o tempo de processamento de cada etapa

        System.out.println("Processando: " + porcentagem + "%");

        // Envia o progresso real para o Angular
        emitter.send(SseEmitter.event()
            .name("Processando")
            .data("{\"status\":\"PROCESSANDO\", \"porcentagem\":" + porcentagem + "}"));
      }

      // ---- A TAREFA ACABOU AQUI ----
      System.out.println("Tarefa concluída com sucesso! Avisando o frontend.");

      // Envia o evento final com o link do resultado ou sinal de sucesso
      emitter.send(SseEmitter.event()
          .name("fim")
          .data("{\"status\":\"CONCLUIDO\", \"url\":\"/arquivos/relatorio-vendas.pdf\"}"));

      // Fecha o emitter no lado do servidor de forma limpa
      emitter.complete();

    } catch (Exception e) {
      // Se der qualquer erro na tarefa, avisa o front e fecha com erro
      emitter.completeWithError(e);
    }

  }

  /*
   * // Usando o scheduler nativo do Java para garantir o paralelismo real
   * private final ScheduledExecutorService scheduler =
   * Executors.newScheduledThreadPool(1);
   * AtomicInteger porcentagem = new AtomicInteger();
   * 
   * var tarefa = scheduler.scheduleAtFixedRate(() -> {
   * try {
   * int atual = porcentagem.addAndGet(20);
   * if (atual > 100) {
   * emitter.send(SseEmitter.event().name("fim")
   * .data("{\"status\":\"CONCLUIDO\", \"url\":\"/arquivos/relatorio-vendas.pdf\"}"
   * ));
   * emitter.complete();
   * return;
   * }
   * 
   * System.out.println("Processando: " + porcentagem + "%");
   * 
   * emitter.send(SseEmitter.event().name("Processando")
   * .data("{\"status\":\"PROCESSANDO\", \"porcentagem\":" + porcentagem + "}"));
   * } catch (IOException e) {
   * emitter.completeWithError(e);
   * }
   * 
   * }, 0, 2, TimeUnit.SECONDS);
   * 
   * emitter.onCompletion(() -> tarefa.cancel(false));
   * emitter.onTimeout(() -> tarefa.cancel(true));
   */
}
