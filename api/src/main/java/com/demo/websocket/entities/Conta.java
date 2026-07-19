package com.demo.websocket.entities;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Conta {

  private @Id @GeneratedValue(strategy = GenerationType.IDENTITY) Long id;

  @ManyToMany(fetch = FetchType.EAGER)
  @JoinTable(name = "conta_objeto", joinColumns = @JoinColumn(name = "conta_id"),
    inverseJoinColumns = @JoinColumn(name = "objeto_id"))
  private List<Objeto> objetos = new ArrayList<>();

}
