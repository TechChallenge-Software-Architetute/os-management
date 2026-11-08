package com.os.workshop.bdd;

import io.cucumber.junit.platform.engine.Constants;
import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;

/**
 * Runner dos cenarios BDD (src/test/resources/features).
 *
 * <p>O sufixo {@code IntegrationTest} e proposital: o failsafe ja inclui as
 * classes com esse sufixo, entao os cenarios rodam no {@code mvn verify}
 * (precisam de Docker para o Postgres do Testcontainers) e ficam fora do
 * {@code mvn test}. Para rodar so o BDD:
 * {@code mvn verify -Dit.test=CucumberIntegrationTest}.
 */
@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features")
@ConfigurationParameter(key = Constants.GLUE_PROPERTY_NAME, value = "com.os.workshop.bdd")
@ConfigurationParameter(key = Constants.PLUGIN_PROPERTY_NAME,
        value = "pretty, html:target/cucumber-reports/cucumber.html, junit:target/cucumber-reports/cucumber.xml")
public class CucumberIntegrationTest {
}
