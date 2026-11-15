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

/**
 * Passos da saga da OS, em estilo declarativo: os "Dado" descrevem o estado da OS
 * e escondem os passos tecnicos para chegar nele; cada "Quando" e uma acao so.
 *
 * <p>As acoes da oficina e do cliente passam pela API HTTP real (seguranca,
 * validacao, transacoes, eventos internos). Os comandos do os-orchestrator entram
 * pelo {@link OrderEventsSqsListener}, com o mesmo formato da mensagem SQS (corpo =
 * id da OS, atributo {@code action}); so a fila em si fica de fora. O que o
 * os-management envia ao orquestrador e capturado pelo
 * {@link RecordingSagaCommandPublisher}, e os "Então" olham apenas o que saiu
 * depois do "Quando".
 *
 * <p>O Cucumber cria uma instancia desta classe por cenario. Cada cenario usa CPF,
 * placa e SKU proprios, porque o contexto Spring (e o banco) e compartilhado.
 */
public class OrdemDeServicoSteps {

    private static final ParameterizedTypeReference<Map<String, Object>> JSON_OBJECT =
            new ParameterizedTypeReference<>() { };
    private static final ParameterizedTypeReference<List<Map<String, Object>>> JSON_LIST =
            new ParameterizedTypeReference<>() { };

    private static final String SERVICE_TYPE = "TROCA_OLEO";
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

    /** X-Request-Id da requisicao que abre a OS: vira o correlationId do CRIAR_OS. */
    private final String requestId = "bdd-req-" + UUID.randomUUID();
    /** correlationId que o "orquestrador" do cenario manda nos comandos. */
    private final String sagaCorrelationId = "bdd-saga-" + UUID.randomUUID();
    /** Quantas mensagens a OS ja tinha mandado ao orquestrador antes do "Quando". */
    private int messagesBeforeAction;
    private RuntimeException commandError;
    private ResponseEntity<Map<String, Object>> lastResponse;
    private List<Map<String, Object>> clientOrders;
    private Map<String, Object> clientOrderDetail;

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

        // O banco de teste nasce vazio (create-drop) e o /signup exige ADMIN, entao
        // papeis, usuario da oficina e tipo de servico sao criados direto.
        ensureRole("ROLE_ADMIN");
        ensureRole("ROLE_USER");
        ensureUser(ADMIN_EMAIL, ADMIN_PASSWORD, "ADMIN");
        ensureServiceType(SERVICE_TYPE);
        adminToken = login(ADMIN_EMAIL, ADMIN_PASSWORD);
    }

    // ==================== Dado ====================

    @Given("um cliente com veículo cadastrado")
    public void umClienteComVeiculoCadastrado() {
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
        // que o /my-orders encontra o cliente dono da OS.
        ensureUser(clientEmail, CLIENT_PASSWORD, "USER");
    }

    @Given("uma peça com preço de venda {string} e {int} unidades em estoque")
    public void umaPecaComEstoque(String salePrice, int quantity) {
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

    @Given("uma OS recém-aberta")
    public void umaOsRecemAberta() {
        openOrder();
    }

    @Given("uma OS aguardando aprovação com {int} unidades da peça reservadas")
    public void umaOsAguardandoAprovacao(int quantity) {
        openOrder();
        moveOrderTo("EM_DIAGNOSTICO");
        reserveParts(quantity);
        moveOrderTo("AGUARDANDO_APROVACAO");
    }

    @Given("uma OS aprovada pelo cliente com {int} unidades da peça reservadas")
    public void umaOsAprovada(int quantity) {
        umaOsAguardandoAprovacao(quantity);
        decide(Map.of("decision", "APPROVED"));
    }

    @Given("uma OS em execução com {int} unidades da peça reservadas")
    public void umaOsEmExecucao(int quantity) {
        umaOsAprovada(quantity);
        // Pelo contrato, so o comando do orquestrador inicia a execucao.
        orchestratorQueue.onMessage(orchestratorBody(SagaAction.EXECUTAR_SERVICO.name()), null);
    }

    // ==================== Quando ====================

    @When("a oficina abre uma OS")
    public void aOficinaAbreUmaOs() {
        markMessages();
        openOrder();
    }

    @When("o orquestrador manda {string}")
    public void oOrquestradorManda(String action) {
        markMessages();
        try {
            orchestratorQueue.onMessage(orchestratorBody(action), null);
        } catch (RuntimeException e) {
            // Em producao a excecao deixa a mensagem sem confirmacao: ela volta para
            // a fila e, depois do maxReceiveCount, vai para a DLQ. Quem decide se
            // era esperado e o "Então".
            commandError = e;
        }
    }

    @When("a oficina tenta mover a OS para {string}")
    public void aOficinaTentaMoverAOsPara(String status) {
        markMessages();
        lastResponse = call(HttpMethod.PATCH, "/order/" + orderId, adminToken, Map.of("status", status));
    }

    @When("o cliente recusa o orçamento")
    public void oClienteRecusaOOrcamento() {
        markMessages();
        decide(Map.of("decision", "REJECTED", "reason", "Muito caro"));
    }

    @When("o cliente consulta suas ordens de serviço")
    public void oClienteConsultaSuasOrdensDeServico() {
        var clientToken = login(clientEmail, CLIENT_PASSWORD);

        var list = http.get().uri("/api/clients/my-orders")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + clientToken)
                .retrieve()
                .toEntity(JSON_LIST);
        assertThat(list.getStatusCode()).isEqualTo(HttpStatus.OK);
        clientOrders = list.getBody();

        var detail = call(HttpMethod.GET, "/api/clients/my-orders/" + orderId, clientToken, null);
        assertStatus(detail, HttpStatus.OK);
        clientOrderDetail = detail.getBody();
    }

    // ==================== Então ====================

    @Then("a OS fica/continua com status {string}")
    public void aOsFicaComStatus(String expectedStatus) {
        assertNoUnexpectedCommandError();
        var response = call(HttpMethod.GET, "/order/id/" + orderId, adminToken, null);
        assertStatus(response, HttpStatus.OK);
        assertThat(response.getBody().get("serviceStatus")).isEqualTo(expectedStatus);
    }

    @Then("o orquestrador recebe {string}")
    public void oOrquestradorRecebe(String action) {
        assertNoUnexpectedCommandError();
        assertThat(messagesSinceAction()).containsExactly(SagaAction.valueOf(action));
    }

    @Then("a mensagem leva o valor {string} e o id da requisição como correlationId")
    public void aMensagemLevaOValorEOIdDaRequisicao(String expectedAmount) {
        assertLastMessage(expectedAmount, requestId);
    }

    @Then("a mensagem leva o valor {string} e o correlationId do comando")
    public void aMensagemLevaOValorEOCorrelationIdDoComando(String expectedAmount) {
        assertLastMessage(expectedAmount, sagaCorrelationId);
    }

    @Then("o orquestrador não recebe nada")
    public void oOrquestradorNaoRecebeNada() {
        assertThat(messagesSinceAction()).isEmpty();
    }

    @Then("o comando é recusado")
    public void oComandoERecusado() {
        assertThat(commandError)
                .as("o comando deveria ter sido recusado")
                .isInstanceOf(SagaCommandRejectedException.class);
        // Recusa esperada e conferida: os proximos "Então" do cenario seguem normalmente.
        commandError = null;
    }

    @Then("o estoque disponível da peça fica em {int}")
    public void oEstoqueDisponivelFicaEm(int expected) {
        assertNoUnexpectedCommandError();
        var response = call(HttpMethod.GET, "/api/stocks/product/" + partId, adminToken, null);
        assertStatus(response, HttpStatus.OK);
        assertThat(decimal(response.getBody().get("availableQuantity")))
                .isEqualByComparingTo(BigDecimal.valueOf(expected));
    }

    @Then("o estoque disponível da peça volta para {int}")
    public void oEstoqueDisponivelVoltaPara(int expected) {
        oEstoqueDisponivelFicaEm(expected);
    }

    @Then("o orçamento da OS continua em {string}")
    public void oOrcamentoDaOsContinuaEm(String expectedTotal) {
        var response = call(HttpMethod.GET, "/api/budgets/service-order/" + orderId, adminToken, null);
        assertStatus(response, HttpStatus.OK);
        assertThat(decimal(response.getBody().get("totalPrice")))
                .isEqualByComparingTo(new BigDecimal(expectedTotal));
    }

    @Then("ele vê a OS com status {string} e orçamento de {string}")
    public void eleVeAOs(String expectedStatus, String expectedTotal) {
        assertThat(clientOrders).anySatisfy(order -> {
            assertThat(order.get("orderId")).isEqualTo(orderId.toString());
            assertThat(order.get("serviceStatus")).isEqualTo(expectedStatus);
        });

        assertThat(clientOrderDetail.get("serviceStatus")).isEqualTo(expectedStatus);
        @SuppressWarnings("unchecked")
        var budget = (Map<String, Object>) clientOrderDetail.get("budget");
        assertThat(budget).as("orcamento no detalhe da OS").isNotNull();
        assertThat(decimal(budget.get("totalPrice"))).isEqualByComparingTo(new BigDecimal(expectedTotal));
    }

    @Then("a operação é recusada com status HTTP {int}")
    public void aOperacaoERecusadaComStatusHttp(int status) {
        assertThat(lastResponse).as("nenhuma tentativa registrada no cenario").isNotNull();
        assertThat(lastResponse.getStatusCode().value()).isEqualTo(status);
    }

    // ==================== Apoio ====================

    private void openOrder() {
        var response = http.post().uri("/order")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                // O RequestCorrelationFilter usa este header como request_id, e o
                // CRIAR_OS sai com ele como correlationId.
                .header("X-Request-Id", requestId)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of(
                        "cpfCnpj", cpf,
                        "placaVeiculo", plate,
                        "serviceTypes", List.of(SERVICE_TYPE)))
                .retrieve()
                .toEntity(JSON_OBJECT);
        assertStatus(response, HttpStatus.CREATED);
        orderId = UUID.fromString((String) response.getBody().get("id"));
    }

    /**
     * Corpo no formato do contrato (ADR-0006), como o orquestrador manda: os 4
     * campos no JSON e nenhum atributo de mensagem.
     */
    private String orchestratorBody(String action) {
        return "{\"order\":\"" + orderId + "\",\"action\":\"" + action
                + "\",\"amount\":0.00,\"correlationId\":\"" + sagaCorrelationId + "\"}";
    }

    private void assertLastMessage(String expectedAmount, String expectedCorrelationId) {
        var messages = sagaPublisher.messagesFor(orderId);
        assertThat(messages).as("nenhuma mensagem enviada ao orquestrador").isNotEmpty();
        var last = messages.get(messages.size() - 1);
        assertThat(last.amount()).isEqualByComparingTo(new BigDecimal(expectedAmount));
        assertThat(last.correlationId()).isEqualTo(expectedCorrelationId);
    }

    private void moveOrderTo(String status) {
        var response = call(HttpMethod.PATCH, "/order/" + orderId, adminToken, Map.of("status", status));
        assertStatus(response, HttpStatus.NO_CONTENT);
    }

    private void reserveParts(int quantity) {
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

    private void decide(Map<String, Object> body) {
        var clientToken = login(clientEmail, CLIENT_PASSWORD);
        var response = call(HttpMethod.POST, "/api/clients/my-orders/" + orderId + "/decision", clientToken, body);
        assertStatus(response, HttpStatus.NO_CONTENT);
    }

    private void markMessages() {
        messagesBeforeAction = orderId == null ? 0 : sagaPublisher.actionsFor(orderId).size();
    }

    private List<SagaAction> messagesSinceAction() {
        var all = sagaPublisher.actionsFor(orderId);
        return all.subList(messagesBeforeAction, all.size());
    }

    private void assertNoUnexpectedCommandError() {
        assertThat(commandError).as("o comando do orquestrador falhou: %s", commandError).isNull();
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

    private void ensureServiceType(String serviceType) {
        if (serviceTypeRepository.findByName(serviceType).isEmpty()) {
            var entity = new ServiceTypeEntity();
            entity.setName(serviceType);
            entity.setDescription("Criado pelo cenario BDD");
            serviceTypeRepository.save(entity);
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
