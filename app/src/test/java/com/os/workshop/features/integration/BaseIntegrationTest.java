package com.os.workshop.features.integration;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;

/**
 * Base class for integration tests that need a real PostgreSQL database.
 *
 * <p>The container is started manually in a static initializer block (not via @Container)
 * to guarantee it is fully ready before Spring context initialization begins.
 * The wait strategy ensures PostgreSQL is accepting connections before proceeding.
 *
 * <p>Uses {@code create-drop} DDL strategy so Hibernate creates all tables from
 * entity mappings at startup and drops them at shutdown — validating that every
 * {@code @Entity}, {@code @Column}, {@code @Convert}, and relationship annotation
 * produces valid PostgreSQL DDL.
 */
@SpringBootTest
@ActiveProfiles("integration")
public abstract class BaseIntegrationTest {

    static PostgreSQLContainer<?> postgres;

    static {
        postgres = new PostgreSQLContainer<>("postgres:16-alpine")
                .withDatabaseName("workshop_test")
                .withUsername("test")
                .withPassword("test")
                .waitingFor(Wait.forListeningPort());
        postgres.start();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }
}
