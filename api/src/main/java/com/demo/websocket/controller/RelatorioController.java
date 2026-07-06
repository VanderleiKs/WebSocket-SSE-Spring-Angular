package com.demo.websocket.controller;

import java.io.IOException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.demo.websocket.relatorios.GerarRelatorioService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/relatorios")
@CrossOrigin("*")
@RequiredArgsConstructor
public class RelatorioController {

  private final GerarRelatorioService gRelatorioService;

  // 1. O endpoint apenas cria o canal e dispara o processo
  @GetMapping(value = "/processar-relatorio", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
  public SseEmitter processarRelatorio() {
    // Timeout longo de segurança (ex: 5 minutos), caso o processo demore muito
    SseEmitter emitter = new SseEmitter(300000L);

    // Dispara a tarefa pesada em background passando o emitter
    gRelatorioService.executarTarefaPesadaEmBackground(emitter);

    // Retorna imediatamente para liberar o Tomcat e abrir o canal com o Angular
    return emitter;
  }

}
