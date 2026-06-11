package com.fisioclinic;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * ─────────────────────────────────────────────────────────────────────────────
 * FisioclinicApplication — Ponto de entrada da aplicação Spring Boot
 * ─────────────────────────────────────────────────────────────────────────────
 * Responsabilidades desta classe:
 *  1. Inicializa o contexto Spring Boot via SpringApplication.run()
 *  2. @SpringBootApplication: habilita auto-configuração, component scan e
 *     configuração de beans para todo o pacote com.fisioclinic
 *  3. @EnableJpaAuditing: ativa o suporte a campos @CreatedDate e
 *     @LastModifiedDate nos modelos JPA (preenchidos automaticamente)
 *
 * Fluxo de inicialização:
 *  main() → Spring Boot carrega application.properties → inicializa JPA/Hibernate
 *  → conecta ao PostgreSQL → executa DataInitializer (cria admin padrão se
 *  não existir) → sobe servidor HTTP na porta 8080
 * ─────────────────────────────────────────────────────────────────────────────
 */
@SpringBootApplication
@EnableJpaAuditing
@EnableScheduling
public class FisioclinicApplication {

    public static void main(String[] args) {
        SpringApplication.run(FisioclinicApplication.class, args);
    }
}
