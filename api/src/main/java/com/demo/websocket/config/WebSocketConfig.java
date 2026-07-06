package com.demo.websocket.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import com.demo.websocket.controller.WebsocketHandler;

//WEBSOCKET CONFIGURATION STOMP
/* @Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

  @Override
  public void configureMessageBroker(MessageBrokerRegistry registry) {
    // Prefixo para as rotas onde os clientes se inscrevem para receber mensagens
    registry.enableSimpleBroker("/topic");

    // Prefixo para as mensagens enviadas pelos clientes para o servidor
    registry.setApplicationDestinationPrefixes("/app");
  }

  @Override
  public void registerStompEndpoints(StompEndpointRegistry registry) {
    // Endpoint para os clientes se conectarem ao WebSocket
    registry.addEndpoint("/ws-connect")
        .setAllowedOriginPatterns("*");
  } */

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

  private WebsocketHandler WebsocketHandler;

  public WebSocketConfig(WebsocketHandler socket) {
    this.WebsocketHandler = new WebsocketHandler();
  }

  @Override
  public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
    registry.addHandler(WebsocketHandler, "/ws-connect")
        .setAllowedOriginPatterns("*");
  }

}
