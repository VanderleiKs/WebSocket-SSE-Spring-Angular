package com.demo.websocket.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Objeto implements Cubo, Quadrado {

  // 1. Atributos privados e preferencialmente imutáveis.
  private @Id @GeneratedValue(strategy = GenerationType.IDENTITY) Long id;
  private String descricao;
  private Integer tamanho;

  /**
   * Criação de Builder manual
   */
  /*
   * // 2. Construtor privado que recebe o buider como argumento
   * private Objeto(Builder builder) {
   * this.descricao = builder.decricao;
   * this.tamanho = builder.tamanho;
   * }
   *
   * // 3. Getters ( sem setters para manter a imutabilidade)
   * public String getDescricao() {
   * return descricao;
   * }
   *
   * public Integer getTamanho() {
   * return tamanho;
   * }
   *
   * // 4. Classe interna estática do Builder
   * public static class Builder {
   * private String decricao;
   * private Integer tamanho;
   *
   * // Métodos de definição que retornam o próprio Builder (Fluent API)
   * public Builder descricao(String descricao) {
   * this.decricao = descricao;
   * return this;
   * }
   *
   * public Builder tamanho(Integer tamanho) {
   * this.tamanho = tamanho;
   * return this;
   * }
   *
   * public Objeto build() {
   * return new Objeto(this);
   * }
   * }
   *
   * //5. Método utilitario para iniciar o builder de forma limpa
   * public static Builder builder() {
   * return new Builder();
   * }
   */

  @Override
  public String quadrado() {
    return Quadrado.nome + "é um Quadrado";
  }

  @Override
  public String redondo() {
    return Cubo.nome + "é redondo";
  }

}
