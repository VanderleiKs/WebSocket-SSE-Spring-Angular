package com.demo.websocket.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.demo.websocket.entities.Objeto;

public interface ObjetoRepository extends JpaRepository<Objeto, Long>{
}
