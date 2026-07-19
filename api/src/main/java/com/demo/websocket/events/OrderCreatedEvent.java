package com.demo.websocket.events;

import java.time.LocalDateTime;
import lombok.Builder;

public record OrderCreatedEvent(
  long orderId,
  String productName,
  LocalDateTime timestamp
) {

  @Builder
  public OrderCreatedEvent(long orderId, String productName) {
    this(orderId, productName, LocalDateTime.now());
  }
}
