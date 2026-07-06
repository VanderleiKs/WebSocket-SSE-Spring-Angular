import { ChangeDetectorRef, Component } from "@angular/core";
import { Subscription } from "rxjs";
import { Sse } from "../app/services/sse";
import { CommonModule, NgClass } from "@angular/common";
import { RestService } from "../app/rest.service";

@Component({
  selector: 'page-relatorio',
  imports: [NgClass, CommonModule],
  templateUrl: './page.html'
})
export class PageRelatorio {

  statusProcesso: string = 'Iniciando...';
  porcentagemProgresso: number = 0;
  linkDownload: string | null = null;
  private sseSub!: Subscription;

  constructor(private sseService: Sse, private restService: RestService, private cdr: ChangeDetectorRef) { }

  ngOnInit(): void {
    //this.restService.processarRelatorio().subscribe(() => console.info("processando..."))
    const url = 'http://localhost:8080/api/relatorios/processar-relatorio';

    this.sseSub = this.sseService.ouvirAcompanhamentoRelatorio(url).subscribe({
      next: (evento) => {
        console.log(evento)
        // Se o evento contiver dados de progresso (enquanto o loop roda no Java)
        if (evento.status === 'PROCESSANDO') {
          this.statusProcesso = 'Processando dados...';
          this.porcentagemProgresso = evento.porcentagem;
          this.cdr.detectChanges();
        }
      },
      error: (err) => {
        this.statusProcesso = 'Erro ao processar relatório.';
        console.error(err);
      },
      // O RxJS ativa o método 'complete' quando o serviço executa o 'observer.complete()'
      complete: () => {
        // Esse bloco roda no exato segundo em que o Java enviou o evento 'fim'
        this.statusProcesso = 'Concluído com sucesso!';
        this.porcentagemProgresso = 100;
        this.linkDownload = '/arquivos/relatorio-vendas.pdf'; // Libera o botão na tela
        console.log('O Frontend parou de escutar de forma limpa e definitiva.');
      }
    });
  }

  ngOnDestroy(): void {
    if (this.sseSub) this.sseSub.unsubscribe();
  }
}