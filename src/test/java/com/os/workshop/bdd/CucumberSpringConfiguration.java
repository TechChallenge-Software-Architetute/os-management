package com.os.workshop.bdd;

import com.os.workshop.integration.config.TestcontainersConfig;
import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

/**
 * Contexto Spring dos cenarios BDD: aplicacao inteira numa porta aleatoria,
 * Postgres real (o mesmo container singleton dos testes de integracao) e os
 * dubles de {@link BddTestConfig} no lugar do SQS e do SNS.
 */
@CucumberContextConfiguration
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("integration")
@Import({TestcontainersConfig.class, BddTestConfig.class})
public class CucumberSpringConfiguration {
}
