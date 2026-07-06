package com.demo.websocket.controller;

import java.io.IOException;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.random.RandomGenerator;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.demo.websocket.config.NotificacaoEvent;

import tools.jackson.databind.ObjectMapper;

/**
 * Utilitário para gerenciar conexões WebSocket e mensagens recebidas. WebSocket
 * Puro (sem STOMP). Alta velociadade, mas sem recursos avançados como tópicos e
 * mensagens direcionadas.
 * WebsocketHandler
 */
@Component
public class WebsocketHandler extends TextWebSocketHandler {

  private ObjectMapper objectMapper; // Para serializar/deserializar mensagens JSON

  public WebsocketHandler() {
    this.objectMapper = new ObjectMapper();
  }

  // Lista thread-safe para armazenar as sessões ativas dos usuários
  private final List<WebSocketSession> sessions = new CopyOnWriteArrayList<>();

  @Override
  public void afterConnectionEstablished(WebSocketSession session) throws Exception {
    sessions.add(session);
    System.out.println("Nova conexão estabelecida: " + session.getId());
  }

  // Método customizado para o seu Controller REST chamar
  @EventListener
  public void enviarMensagemGlobal(NotificacaoEvent event) {
    System.out.println("Messagem: " + event.getMensagem());
    for (WebSocketSession session : sessions) {
      if (session.isOpen()) {
        try {
          var x = 0;
          while (x < 10000) {
            Thread.sleep(1000); // Atraso de 1 segundo entre os envios
            // var chunkSize = Math.min(100, payload.length() - x);
            var chunk = event.getMensagem() + " - " + new Random().nextInt(1000);
            var res = String.format("Servidor diz: %s", chunk);
            var json = objectMapper.writeValueAsString(res);
            session.sendMessage(new TextMessage(json));
            x++;
          }
          // var res = String.format("Servidor diz: %s", payload);
          // var json = objectMapper.writeValueAsString(res);
          // activeSession.sendMessage(new TextMessage(json));
        } catch (Exception e) {
          System.err.println("Erro ao enviar mensagem para " + e.getMessage());
        }
      }
    }
  }

  /*
   * @Override
   * protected void handleTextMessage(WebSocketSession session, TextMessage
   * message) throws Exception {
   * String payload = message.getPayload();
   * System.out.println("Mensagem recebida de " + session.getId() + ": " +
   * payload);
   * 
   * // Enviar a mensagem para todos os clientes conectados
   * for (WebSocketSession activeSession : sessions) {
   * if (activeSession.isOpen()) {
   * try {
   * var x = 0;
   * while (x < 10000) {
   * Thread.sleep(1000); // Atraso de 1 segundo entre os envios
   * // var chunkSize = Math.min(100, payload.length() - x);
   * var chunk = payload + " - " + new Random().nextInt(1000);
   * var res = String.format("Servidor diz: %s", chunk);
   * var json = objectMapper.writeValueAsString(res);
   * activeSession.sendMessage(new TextMessage(json));
   * x++;
   * }
   * // var res = String.format("Servidor diz: %s", payload);
   * // var json = objectMapper.writeValueAsString(res);
   * // activeSession.sendMessage(new TextMessage(json));
   * } catch (Exception e) {
   * System.err.println("Erro ao enviar mensagem para " + activeSession.getId() +
   * ": " + e.getMessage());
   * }
   * }
   * }
   * 
   * }
   */
  @Override
  public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
    sessions.remove(session);
    System.out.println("Conexão encerrada: " + session.getId() + " com status: " + status);
  }
}
