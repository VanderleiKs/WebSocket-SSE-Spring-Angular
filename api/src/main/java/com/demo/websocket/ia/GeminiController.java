package com.demo.websocket.ia;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaChatOptions;
//import org.springframework.ai.google.genai.GoogleGenAiChatModel;
//import org.springframework.ai.google.genai.GoogleGenAiChatOptions;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.demo.websocket.entities.Objeto;
import com.demo.websocket.repositories.ObjetoRepository;

import lombok.RequiredArgsConstructor;
import tools.jackson.databind.json.JsonMapper;

@RestController
@RequestMapping("/ia")
@RequiredArgsConstructor
public class GeminiController {
  //private final GoogleGenAiChatModel genAiChatModel;
  private final ObjetoRepository objetoRepository;
  private final JsonMapper jsonMapper;

  @GetMapping("/perguntar")
  public ResponseEntity<?> perguntar(@RequestParam() String message) {
    var dataAtual = LocalDateTime.now();

    var objs = objetoRepository.findAll();

    // ------- GOOGLE GEMINI --------
   /* var prompOptions = GoogleGenAiChatOptions.builder()
                        .model("gemini-3.1-flash-lite") // Modelo
                        .temperature(0.3) // Resposta mais diretas e curtas
                        .maxOutputTokens(150) // Tamanho máximo da resposta
                        .build();

    var payload = jsonMapper.writeValueAsString(new Payload(message, dataAtual, objs));
    var prompt = new Prompt(payload, prompOptions);

    return ResponseEntity.ok(genAiChatModel.call(prompt).getResult().getOutput().getText()); */
    return ResponseEntity.ok("");

  }

  private record Payload(
    String pergunta,
    LocalDateTime dataAtual,
    List<Objeto> objetos
  ) {
  }
}
