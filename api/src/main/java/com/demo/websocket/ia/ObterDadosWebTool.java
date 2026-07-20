package com.demo.websocket.ia;

import java.util.List;
import java.util.Map;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.ObjectMapper;

@Slf4j
@Component
@RequiredArgsConstructor
public class ObterDadosWebTool {
  private final RestClient.Builder restClientBuilder;
  private final ObjectMapper mapper;

  @Tool(description = "Consulta a API do Spring Initializr para obter informações sobre dependências e versões disponíveis do Spring Boot.")
  SpringData obterDependenciesSpring() {

    var result = restClientBuilder.build()
      .get()
      .uri("https://start.spring.io/dependencies")
      .retrieve()
      .body(SpringData.class);

      log.info("Obtido dados spring, Boot Version: {}, quantidade: {}", result.bootVersion(), result.dependencies().size());


      //limitar quantidade
      var limitDep = result.dependencies().entrySet().stream()
        .limit(10)
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

    var response = new SpringData(result.bootVersion(), limitDep);

      log.info("dados: {}", mapper.writeValueAsString(response));

      return response;
  }

  /**
   *
   "bootVersion": "4.1.0",
   "dependencies": {
     "amqp-streams": {
       "groupId": "org.springframework.amqp",
       "artifactId": "spring-rabbit-stream",
       "scope": "compile"
     },
     "spring-ai-chat-memory-repository-in-memory": {
       "groupId": "org.springframework.ai",
       "artifactId": "spring-ai-starter-model-chat-memory",
       "scope": "compile",
       "bom": "spring-ai"
   */
   record SpringData(
     String bootVersion,
     Map<String,SpringDependency> dependencies
   ){
   }

   record SpringDependency(
     String groupId,
     String artifactId,
     String scope,
     String bom
   ){
   }
}
