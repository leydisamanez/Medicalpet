package com.medicalpet.medicalpet;

import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.testcontainers.containers.MySQLContainer;

@Configuration
@Profile("!test") // No se carga en el perfil "test"
public class TestcontainersConfiguration {

    @Bean
    @ServiceConnection
    public MySQLContainer<?> mysqlContainer() {
        return new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("medicalpetdb")
            .withUsername("root")
            .withPassword("password");
    }
}