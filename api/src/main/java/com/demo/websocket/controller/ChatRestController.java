package com.demo.websocket.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.demo.websocket.config.NotificacaoEvent;

import java.io.IOException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*") // Permitir solicitações de qualquer origem
public class ChatRestController {

  @Autowired
  private ApplicationEventPublisher eventPublisher;

  // Executor para agendar tarefas em background sem travar o endpoint HTTP
  private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
  /**
   * Mantém uma lista de emissores ativos (ex: usuários conectados)
   */
  private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();

  @PostMapping("/chat")
  public ResponseEntity<MessageResponse> postMessage(@RequestBody MessageRequest request) {
    var response = new MessageResponse(request.message(), java.time.LocalDateTime.now().toString());

    eventPublisher.publishEvent(new NotificacaoEvent(this, request.message()));

    return ResponseEntity.ok(response);
  }

  @GetMapping(value = "/acompanhar", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
  public SseEmitter getMethodName() {
    // Cria um evento com timeout de 30 segundos
    SseEmitter emitter = new SseEmitter(30000L);
    this.emitters.add(emitter);
    emmitterEvent();
    System.out.println("Emitter created: " + emitter.toString());

    // Remove da lista quando a conexão terminar ou der erro
    emitter.onCompletion(() -> this.emitters.remove(emitter));
    emitter.onTimeout(() -> this.emitters.remove(emitter));
    emitter.onError((ex) -> this.emitters.remove(emitter));
    return emitter;
  }

  private void emmitterEvent() {
    /*
     * var count = 0;
     * var emitter = emitters.get(0);
     * try {
     * while (emitter.getTimeout() > 0) {
     * count++;
     * Thread.sleep(1000);
     * System.out.println("Send message: " + count);
     * emitter.send(new MessageResponse("teste-" + count,
     * LocalDateTime.now().toString()));
     * }
     * } catch (Exception e) {
     * emitter.complete();
     * }
     * }
     */
    AtomicInteger count = new AtomicInteger(0);
    var emitter = emitters.get(0);
    // Agenda uma tarefa para rodar a cada 1 segundo (substitui o Thread.sleep e o
    // while)
    var tarefaAgendada = scheduler.scheduleAtFixedRate(() -> {
      try {
        int atual = count.incrementAndGet();
        System.out.println("Send message: " + atual);

        // Envia a mensagem para o Angular
        emitter.send(SseEmitter.event()
            .name("notificacao-messages") // O mesmo nome que o Angular escuta no addEventListener
            .data(new MessageResponse("teste-" + atual, LocalDateTime.now().toString())));

      } catch (IOException e) {
        // Se o cliente desconectar, encerra o envio para este emitter
        System.out.println("Cliente desconectou. Parando envio.");
        emitter.complete();
      }
    }, 0, 1, TimeUnit.SECONDS);

    // Importante: Se o emitter expirar (timeout) ou completar, cancela o agendador
    // em background
    emitter.onCompletion(() -> tarefaAgendada.cancel(true));
    emitter.onTimeout(() -> tarefaAgendada.cancel(true));
  }

}

record MessageRequest(String message) {
}

record MessageResponse(String message, String timestamp) {
}