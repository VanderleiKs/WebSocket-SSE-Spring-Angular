package com.demo.websocket.controller;

import org.springframework.http.MediaType;
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
