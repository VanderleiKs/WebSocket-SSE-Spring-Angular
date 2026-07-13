package com.demo.websocket.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;

@AllArgsConstructor
@Builder
public class QuadradoImpl implements Quadrado {

  @Override
  public String quadrado() {
    return nome;
  }

}
