package com.demo.websocket.entities;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class CuboImpl implements Cubo {

  @Override
  public String redondo() {
    return nome;
  }

}
