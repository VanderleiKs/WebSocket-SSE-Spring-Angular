package com.demo.websocket.events;

import org.slf4j.Logger;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import com.demo.websocket.events.OrderCreatedEvent.OrderCreatedEventBuilder;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderService {
  private final ApplicationEventPublisher eventPublisher;
  private final Logger logger;

  public void createOrder(long orderId, String productName) {
    logger.info("--- ORDDEM CRIADA: Publicando evento OrderCreateEvent com ID: " + orderId);

    var event = new OrderCreatedEventBuilder()
                    .orderId(orderId)
                    .productName(productName)
                    .build();


    eventPublisher.publishEvent(event);
    InventoryService.soma();
  }

}
