package com.medicalpet.medicalpet.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * Clase de configuración para la aplicación MedicalPet.
 */
@Configuration
public class ApplicationConfig {

  /**
     * Crea y registra un bean de RestTemplate.
     *
     * @return una nueva instancia de RestTemplate
     */
  @Bean
    public RestTemplate restTemplate() {
    return new RestTemplate();
  }
}