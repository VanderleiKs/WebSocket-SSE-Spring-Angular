package com.demo.websocket.ia;

import com.demo.websocket.entities.Objeto;
import com.demo.websocket.repositories.ObjetoRepository;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaChatOptions;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;

import tools.jackson.databind.json.JsonMapper;

@Slf4j
@RestController
@RequestMapping("/ollama")
//@RequiredArgsConstructor
public class OllamaController {

  private final OllamaChatModel ollamaChatModel;
   private final ObjetoRepository objetoRepository;
   private final JsonMapper jsonMapper;
   private final ChatClient chatClient;
   private final ObterDadosWebTool dadosWebTool;

   public OllamaController(
       OllamaChatModel ollamaChatModel,
       ObjetoRepository objetoRepository,
       JsonMapper jsonMapper,
       ObterDadosWebTool dadosWebTool
   ) {
     this.ollamaChatModel = ollamaChatModel;
     this.objetoRepository = objetoRepository;
     this.jsonMapper = jsonMapper;
     this.chatClient = ChatClient.builder(ollamaChatModel).build();
     this.dadosWebTool = dadosWebTool;
   }

   @GetMapping("/dados-spring")
  public ResponseEntity<?> dadosSpring() {
    var dados = dadosWebTool.obterDependenciesSpring();

    var instrucoes = """
         Aqui estão os dados da API do Spring:
         %s

         Com base nestes dados, identifique a versão do Spring Boot, e a quantidade de extensões.
         Retorne APENAS um JSON estruturado:
         {
           "bootVersion": "bootVersion",
           "quantidadeExtensoes": numero
         }
         """.formatted(dados);

    return ResponseEntity.ok(
      chatClient
        .prompt(instrucoes)
        .call()
        .content()
    );
    //return ResponseEntity.ok(dados);
  }


  @GetMapping("/perguntar")
  public ResponseEntity<?> perguntar(@RequestParam() String message, @RequestParam() ResponseType type) {
    var intent = interpretarPedido(message, type);
    log.info("{}", jsonMapper.writeValueAsString(intent));

    if (type == ResponseType.CSV) {
        var objetos = objetoRepository.findAll();
        return gerarRelatorioCsv(message, objetos);
    }

    var dataAtual = LocalDateTime.now();
    var objs = objetoRepository.findAll();
    var promptOptions = OllamaChatOptions.builder()
      .model("gemma4:e2b")
      .temperature(0.8)
      .format("json")
      .build();

    var payload = jsonMapper.writeValueAsString(
      new Payload(message, dataAtual, objs)
    );
    var prompt = new Prompt(payload, promptOptions);

    return ResponseEntity.ok(
      ollamaChatModel.call(prompt).getResult().getOutput().getText()
    );
  }

  /**
   * A IA irá interpretar o pedido e montar um objeto estruturado.
   * @param message
   * @return
   */
  private ReportIntent interpretarPedido(String message, ResponseType type) {
    return chatClient
      .prompt()
      .system(
        """
          Você interpreta pedidos de relatórios.

          Determine se o usuário deseja uma resposta textual ou um arquivo CSV.

          Regras:
          - Use type CSV quando o usuário pedir CSV, planilha ou relatório em arquivo.
          - Deve estar explicito no pedido a palavra CSV, senão adote type TEXT.
          - Para respostas textuais, use type TEXT.
          - O nome do arquivo deve terminar com .csv.
          - Retorne apenas dados compatíveis com o schema solicitado.
        """
      )
      .user(message)
      .call()
      .entity(ReportIntent.class);
  }

  private ResponseEntity<byte[]> gerarRelatorioCsv(
    String solicitacao,
    List<Objeto> objetos
  ) {
    var dadosJson = jsonMapper.writeValueAsString(objetos);

    var instrucoes = """
    Você é um gerador de arquivos CSV.

    Analise a solicitação do usuário e gere um CSV utilizando exclusivamente
    os dados fornecidos.

    Regras obrigatórias:
    - Retorne somente o conteúdo CSV.
    - ALtere o delimiter SEMPRE para PONTO E VIRGULA(";").
    - Não use blocos Markdown.
    - Não use ```csv.
    - Não escreva explicações antes ou depois do CSV.
    - Preserve exatamente os valores recebidos.
    - Não invente, remova ou altere registros.
    - Inclua um cabeçalho.
    - Utilize o delimitador solicitado pelo usuário.
    - Coloque entre aspas valores que contenham o delimitador,
      aspas ou quebras de linha.
    - Duplique aspas existentes dentro dos valores.

    Solicitação do usuário:
    %s

    Dados disponíveis, em JSON:
    %s
    """.formatted(solicitacao, dadosJson);

    var options = OllamaChatOptions.builder()
      .model("gemma4:e2b")
      .temperature(0.0)
      .build();

    var prompt = new Prompt(instrucoes, options);
    var csv = ollamaChatModel.call(prompt).getResult().getOutput().getText();

    csv = removerMarkdown(csv);

    // BOM para o Excel reconhecer UTF-8 e acentos
    var bytes = ("\uFEFF" + csv).getBytes(StandardCharsets.UTF_8);

    return ResponseEntity.ok()
      .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"objetos.csv\"")
      .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
      .contentLength(bytes.length)
      .body(bytes);
  }

  private String removerMarkdown(String content) {
    if (Objects.isNull(content)) return "";

    return content
      .replaceFirst("(?i)^\\s*```csv\\s*", "")
      .replaceFirst("(?i)^\\s*```\\s*", "")
      .replaceFirst("\\s*```\\s*$", "")
      .strip();
  }

  record Payload(
    String pergunta,
    LocalDateTime dataAtual,
    List<Objeto> objetos
  ) {}

  record ReportIntent(
    ResponseType type,
    String delimiter,
    String fileName,
    List<String> colunas
  ) {
  }

  enum ResponseType {
    TEXT,
    CSV,
  }
}
