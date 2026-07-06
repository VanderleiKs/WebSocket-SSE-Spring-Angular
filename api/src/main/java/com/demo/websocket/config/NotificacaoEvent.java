package com.demo.websocket.config;

import org.springframework.context.ApplicationEvent;

public class NotificacaoEvent extends ApplicationEvent {

  private final String mensagem;

  public NotificacaoEvent(Object source, String mensagem) {
    super(source);
    this.mensagem = mensagem;
  }

  public String getMensagem() {
    return this.mensagem;
  }

}
