package com.demo.websocket.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InjectionPoint;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
public class LoggerConfig {

  @Bean
  public Logger config(InjectionPoint injectionPoint) {
    return LoggerFactory.getLogger(injectionPoint.getMethodParameter().getContainingClass());
  }
}
