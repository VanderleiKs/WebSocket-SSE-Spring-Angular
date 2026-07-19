package com.demo.websocket;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

import com.demo.websocket.events.OrderService;

import lombok.RequiredArgsConstructor;

@SpringBootApplication
@EnableAsync
@RequiredArgsConstructor
public class WebsocketApplication implements CommandLineRunner {
  private final OrderService orderService;

	public static void main(String[] args) {
		SpringApplication.run(WebsocketApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
    System.out.println("Iniciando o sistema de eventos...");

    orderService.createOrder(1001L, "Laptop x200");
    orderService.createOrder(1002L, "Mouse pro");
	}

}
