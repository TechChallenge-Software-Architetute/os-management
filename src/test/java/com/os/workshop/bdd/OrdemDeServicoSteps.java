package com.os.workshop.bdd;

import com.os.workshop.adapter.in.messaging.OrderEventsSqsListener;
import com.os.workshop.application.saga.HandleSagaCommandUseCase;
import com.os.workshop.application.saga.SagaAction;
import com.os.workshop.application.saga.SagaCommandRejectedException;
import com.os.workshop.application.user.port.out.UserRepository;
import com.os.workshop.infrastructure.persistence.service.ServiceTypeEntity;
import com.os.workshop.infrastructure.persistence.service.ServiceTypeJpaRepository;
import com.os.workshop.infrastructure.persistence.user.JpaRoleRepository;
import com.os.workshop.infrastructure.persistence.user.RoleEntity;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Passos do ciclo de vida da OS na saga.
 *
 * <p>As acoes da oficina e do cliente passam pela API HTTP real (seguranca,
 * validacao, transacoes, eventos internos). Os comandos do os-orchestrator entram
 * pelo {@link OrderEventsSqsListener}, com o mesmo formato da mensagem SQS (corpo =
 * id da OS, atributo {@code action}); so a fila em si fica de fora. As mensagens
 * que o os-management envia sao capturadas pelo {@link RecordingSagaCommandPublisher}.
 *
 * <p>O Cucumber cria uma instancia desta classe por cenario, entao os campos
 * guardam o estado do cenario corrente. Cada cenario usa CPF, placa e SKU
 * proprios, porque o contexto Spring (e o banco) e compartilhado entre cenarios.
 */
public class OrdemDeServicoSteps {

    private static final ParameterizedTypeReference<Map<String, Object>> JSON_OBJECT =
            new ParameterizedTypeReference<>() { };
    private static final ParameterizedTypeReference<List<Map<String, Object>>> JSON_LIST =
            new ParameterizedTypeReference<>() { };

    private static final String ADMIN_EMAIL = "admin.bdd@workshop.test";
    private static final String ADMIN_PASSWORD = "Admin@123";
    private static final String CLIENT_PASSWORD = "Cliente@123";
    private static final SecureRandom RANDOM = new SecureRandom();

    @Autowired private Environment environment;
    @Autowired private JpaRoleRepository roleRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private ServiceTypeJpaRepository serviceTypeRepository;
    @Autowired private HandleSagaCommandUseCase handleSagaCommandUseCase;
    @Autowired private RecordingSagaCommandPublisher sagaPublisher;

    private RestClient http;
    private OrderEventsSqsListener orchestratorQueue;
    private String adminToken;
    private String cpf;
    private String plate;
    private String clientEmail;
    private Long partId;
    private UUID orderId;
    private ResponseEntity<Map<String, Object>> lastResponse;

    @Before
    public void prepararAmbiente() {
        http = RestClient.builder()
                .baseUrl("http://localhost:" + environment.getRequiredProperty("local.server.port"))
                // Os passos verificam o status HTTP; erro 4xx/5xx nao deve virar excecao.
                .defaultStatusHandler(HttpStatusCode::isError, (request, response) -> { })
                .build();

        // Mesmo adapter que consome a fila em producao, sem a fila: no teste o
        // listener SQS fica desligado (aws.sqs.order-events.enabled=false).
        orchestratorQueue = new OrderEventsSqsListener(handleSagaCommandUseCase);

        // O banco de teste nasce vazio (create-drop) e o /signup exige ADMIN,
        // entao os papeis e o usuario da oficina sao criados direto pelas portas.
        ensureRole("ROLE_ADMIN");
        ensureRole("ROLE_USER");
        ensureUser(ADMIN_EMAIL, ADMIN_PASSWORD, "ADMIN");
        adminToken = login(ADMIN_EMAIL, ADMIN_PASSWORD);
    }

    // ==================== Dado ====================

    @Given("que existe o tipo de serviço {string}")
    public void queExisteOTipoDeServico(String serviceType) {
        if (serviceTypeRepository.findByName(serviceType).isEmpty()) {
            var entity = new ServiceTypeEntity();
            entity.setName(serviceType);
            entity.setDescription("Criado pelo cenario BDD");
            serviceTypeRepository.save(entity);
        }
    }

    @Given("que existe um cliente com veículo cadastrado")
    public void queExisteUmClienteComVeiculoCadastrado() {
        cpf = randomCpf();
        plate = randomMercosulPlate();
        clientEmail = "cliente." + cpf + "@bdd.test";

        var client = call(HttpMethod.POST, "/api/clients", adminToken, Map.of(
                "name", "CLIENTE BDD",
                "document", cpf,
                "email", clientEmail,
                "phone", "11999999999"));
        assertStatus(client, HttpStatus.CREATED);
        Number clientId = (Number) client.getBody().get("id");

        var vehicle = call(HttpMethod.POST, "/api/vehicles", adminToken, Map.of(
                "clientId", clientId.longValue(),
                "plate", plate,
                "brand", "Toyota",
                "model", "Corolla",
                "year", 2023,
                "color", "Preto",
                "type", "CAR"));
        assertStatus(vehicle, HttpStatus.CREATED);

        // Usuario do cliente (papel USER) com o mesmo e-mail do cadastro: e assim
        // que o /my-orders/{id}/decision encontra o cliente dono da OS.
        ensureUser(clientEmail, CLIENT_PASSWORD, "USER");
    }

    @Given("que existe uma peça com preço de venda {string} e {int} unidades em estoque")
    public void queExisteUmaPecaComEstoque(String salePrice, int quantity) {
        var part = call(HttpMethod.POST, "/api/parts", adminToken, Map.of(
                "name", "Pastilha de Freio BDD",
                "sku", "BDD-" + (100000 + RANDOM.nextInt(900000)),
                "unit", "UNIT",
                "category", "Freios",
                "brand", "Bosch",
                "costPrice", new BigDecimal("45.00"),
                "salePrice", new BigDecimal(salePrice),
                "manufacturerCode", "BOH-BP-2025",
                "warrantyMonths", 12));
        assertStatus(part, HttpStatus.CREATED);
        partId = ((Number) part.getBody().get("id")).longValue();

        var stock = call(HttpMethod.POST, "/api/stocks", adminToken, Map.of(
                "productId", partId,
                "quantity", BigDecimal.valueOf(quantity),
                "minimumQuantity", BigDecimal.ONE));
        assertStatus(stock, HttpStatus.CREATED);
    }

    // ==================== Quando ====================

    @When("a oficina abre uma OS com o serviço {string}")
    public void aOficinaAbreUmaOs(String serviceType) {
        var response = call(HttpMethod.POST, "/order", adminToken, Map.of(
                "cpfCnpj", cpf,
                "placaVeiculo", plate,
                "serviceTypes", List.of(serviceType)));
        assertStatus(response, HttpStatus.CREATED);
        orderId = UUID.fromString((String) response.getBody().get("id"));
    }

    @When("a oficina move a OS para {string}")
    public void aOficinaMoveAOsPara(String status) {
        assertStatus(moveOrderTo(status), HttpStatus.NO_CONTENT);
    }

    @When("a oficina tenta mover a OS para {string}")
    public void aOficinaTentaMoverAOsPara(String status) {
        lastResponse = moveOrderTo(status);
    }

    @When("a oficina reserva {int} unidades da peça para a OS")
    public void aOficinaReservaUnidadesDaPeca(int quantity) {
        var response = http.post().uri("/api/stocks/reservations")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of(
                        "serviceOrderId", orderId,
                        "items", List.of(Map.of("productId", partId, "quantity", BigDecimal.valueOf(quantity)))))
                .retrieve()
                .toEntity(JSON_LIST);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }

    @When("o cliente aprova o orçamento")
    public void oClienteAprovaOOrcamento() {
        decide(Map.of("decision", "APPROVED"));
    }

    @When("o cliente recusa o orçamento com o motivo {string}")
    public void oClienteRecusaOOrcamento(String reason) {
        decide(Map.of("decision", "REJECTED", "reason", reason));
    }

    @When("o orquestrador manda {string} para a OS")
    public void oOrquestradorMandaParaAOs(String action) {
        orchestratorQueue.onMessage(orderId.toString(), action);
    }

    @When("o orquestrador manda {string} para a OS, que é recusado")
    public void oOrquestradorMandaParaAOsQueERecusado(String action) {
        // Em producao a excecao deixa a mensagem sem confirmacao: ela volta para a
        // fila e, depois do maxReceiveCount, vai para a DLQ.
        assertThatThrownBy(() -> orchestratorQueue.onMessage(orderId.toString(), action))
                .isInstanceOf(SagaCommandRejectedException.class);
    }

    // ==================== Então ====================

    @Then("a OS fica com status {string}")
    public void aOsFicaComStatus(String expectedStatus) {
        var response = call(HttpMethod.GET, "/order/id/" + orderId, adminToken, null);
        assertStatus(response, HttpStatus.OK);
        assertThat(response.getBody().get("serviceStatus")).isEqualTo(expectedStatus);
    }

    @Then("o orquestrador recebeu {string} nessa ordem")
    public void oOrquestradorRecebeuNessaOrdem(String actions) {
        var expected = Arrays.stream(actions.split(","))
                .map(String::trim)
                .map(SagaAction::valueOf)
                .toList();
        assertThat(sagaPublisher.actionsFor(orderId)).containsExactlyElementsOf(expected);
    }

    @Then("o orçamento da OS soma {string}")
    public void oOrcamentoDaOsSoma(String expectedTotal) {
        var response = call(HttpMethod.GET, "/api/budgets/service-order/" + orderId, adminToken, null);
        assertStatus(response, HttpStatus.OK);
        assertThat(decimal(response.getBody().get("totalPrice")))
                .isEqualByComparingTo(new BigDecimal(expectedTotal));
    }

    @Then("as reservas de estoque da OS ficam {string}")
    public void asReservasDeEstoqueDaOsFicam(String expectedStatus) {
        var response = http.get().uri("/api/stocks/reservations/service-order/" + orderId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                .retrieve()
                .toEntity(JSON_LIST);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody())
                .isNotEmpty()
                .allSatisfy(reservation -> assertThat(reservation.get("status")).isEqualTo(expectedStatus));
    }

    @Then("o estoque disponível da peça fica em {int}")
    public void oEstoqueDisponivelDaPecaFicaEm(int expected) {
        var response = call(HttpMethod.GET, "/api/stocks/product/" + partId, adminToken, null);
        assertStatus(response, HttpStatus.OK);
        assertThat(decimal(response.getBody().get("availableQuantity")))
                .isEqualByComparingTo(BigDecimal.valueOf(expected));
    }

    @Then("a operação é recusada com status HTTP {int}")
    public void aOperacaoERecusadaComStatusHttp(int status) {
        assertThat(lastResponse).as("nenhuma tentativa registrada no cenario").isNotNull();
        assertThat(lastResponse.getStatusCode().value()).isEqualTo(status);
    }

    // ==================== Apoio ====================

    private ResponseEntity<Map<String, Object>> moveOrderTo(String status) {
        return call(HttpMethod.PATCH, "/order/" + orderId, adminToken, Map.of("status", status));
    }

    private void decide(Map<String, Object> body) {
        var clientToken = login(clientEmail, CLIENT_PASSWORD);
        var response = call(HttpMethod.POST, "/api/clients/my-orders/" + orderId + "/decision", clientToken, body);
        assertStatus(response, HttpStatus.NO_CONTENT);
    }

    private ResponseEntity<Map<String, Object>> call(HttpMethod method, String uri, String token, Object body) {
        var request = http.method(method).uri(uri).contentType(MediaType.APPLICATION_JSON);
        if (token != null) {
            request = request.header(HttpHeaders.AUTHORIZATION, "Bearer " + token);
        }
        if (body != null) {
            request = request.body(body);
        }
        return request.retrieve().toEntity(JSON_OBJECT);
    }

    private String login(String email, String password) {
        var response = call(HttpMethod.POST, "/auth/login", null, Map.of("email", email, "password", password));
        assertStatus(response, HttpStatus.OK);
        return (String) response.getBody().get("token");
    }

    private void ensureRole(String name) {
        if (roleRepository.findByName(name) == null) {
            roleRepository.save(new RoleEntity(null, name));
        }
    }

    private void ensureUser(String email, String password, String role) {
        if (userRepository.findByEmail(email).isEmpty()) {
            userRepository.save(email, passwordEncoder.encode(password), Set.of(role));
        }
    }

    private static void assertStatus(ResponseEntity<?> response, HttpStatus expected) {
        assertThat(response.getStatusCode())
                .as("resposta inesperada: %s", response.getBody())
                .isEqualTo(expected);
    }

    private static BigDecimal decimal(Object value) {
        return new BigDecimal(String.valueOf(value));
    }

    /** CPF valido (digitos verificadores corretos) e diferente a cada chamada. */
    private static String randomCpf() {
        int[] digits = new int[11];
        do {
            for (int i = 0; i < 9; i++) {
                digits[i] = RANDOM.nextInt(10);
            }
        } while (Arrays.stream(digits, 0, 9).distinct().count() == 1);
        digits[9] = checkDigit(digits, 9);
        digits[10] = checkDigit(digits, 10);

        var cpf = new StringBuilder();
        for (int digit : digits) {
            cpf.append(digit);
        }
        return cpf.toString();
    }

    private static int checkDigit(int[] digits, int length) {
        int sum = 0;
        for (int i = 0; i < length; i++) {
            sum += digits[i] * (length + 1 - i);
        }
        int digit = 11 - (sum % 11);
        return digit >= 10 ? 0 : digit;
    }

    /** Placa no padrao Mercosul (AAA9A99), diferente a cada chamada. */
    private static String randomMercosulPlate() {
        return "" + letter() + letter() + letter() + RANDOM.nextInt(10) + letter()
                + RANDOM.nextInt(10) + RANDOM.nextInt(10);
    }

    private static char letter() {
        return (char) ('A' + RANDOM.nextInt(26));
    }
}
