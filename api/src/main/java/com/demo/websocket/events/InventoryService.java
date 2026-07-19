package com.demo.websocket.events;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class InventoryService {

  @EventListener
  public void handleOrderCreated(OrderCreatedEvent event) {
    System.out.println("============================================");
    System.out.println(">>> ESTOQUE REAGINDO ao evento!");
    System.out.println("Pedido ID: " + event.orderId());
    System.out.println("Produto: " + event.productName());
    System.out.println("Hora da Reação: " + event.timestamp());
    System.out.println("============================================");
  }

  public static void soma() {
    var total = new ArrayList<BigDecimal>();
    total.add(BigDecimal.valueOf(11.226));
    total.add(BigDecimal.valueOf(15.5));
    total.add(BigDecimal.valueOf(15));

    var soma = total.stream().reduce(BigDecimal.ZERO, BigDecimal::add);

    // Ajustar para reais usando a API de formatação monetária recomendada
    var formatter = NumberFormat.getCurrencyInstance(Locale.of("pt", "BR"));
    log.info("A soma é {}", formatter.format(soma));
  }
}
