package com.demo.websocket.controller;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class ChatController {

  @MessageMapping("/send-message")
  @SendTo("/topic/messages")
  public String broadcastMessage(String message) {
    return String.format("Servidor diz: %s", message);
  }
}
