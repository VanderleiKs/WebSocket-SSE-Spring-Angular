package com.demo.websocket.controller;

import com.demo.websocket.entities.Objeto;
import com.demo.websocket.repositories.ObjetoRepository;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/teste")
@CrossOrigin("*")
@RequiredArgsConstructor
public class Teste {

  private final Logger logger;
  private final ObjetoRepository objetoRepository;

  @PostMapping
  public ResponseEntity<Objeto> getTeste(@RequestBody String nome) {
    var obj = Objeto.builder()
      .descricao(String.format("descricao %s", nome))
      .tamanho(15)
      .build();

    obj = objetoRepository.save(obj);

    logger.info("Teste");

    return ResponseEntity.ok().body(obj);
  }
}
