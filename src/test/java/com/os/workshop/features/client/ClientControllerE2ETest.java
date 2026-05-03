package com.os.workshop.features.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.os.workshop.features.client.domain.Client;
import com.os.workshop.features.client.dto.ClientRequest;
import com.os.workshop.features.client.repository.ClientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ClientControllerE2ETest {

    private static final String VALID_CPF = "52998224725";

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private com.os.workshop.features.serviceorder.shared.repository.ServiceOrderJpaRepository serviceOrderJpaRepository;

    @Mock
    private com.os.workshop.features.budget.BudgetService budgetService;

    @BeforeEach
    void setUp() {
        ClientService clientService = new ClientService(clientRepository, serviceOrderJpaRepository, budgetService);
        ClientController clientController = new ClientController(clientService);
        mockMvc = MockMvcBuilders.standaloneSetup(clientController)
                .setCustomArgumentResolvers(new org.springframework.web.method.support.HandlerMethodArgumentResolver() {
                    @Override
                    public boolean supportsParameter(org.springframework.core.MethodParameter parameter) {
                        return parameter.getParameterType().equals(org.springframework.security.core.userdetails.UserDetails.class);
                    }

                    @Override
                    public Object resolveArgument(org.springframework.core.MethodParameter parameter,
                                                  org.springframework.web.method.support.ModelAndViewContainer mavContainer,
                                                  org.springframework.web.context.request.NativeWebRequest webRequest,
                                                  org.springframework.web.bind.support.WebDataBinderFactory binderFactory) {
                        return mockUserDetails();
                    }
                })
                .build();
    }

    private org.springframework.security.core.userdetails.UserDetails mockUserDetails() {
        return org.springframework.security.core.userdetails.User.builder()
                .username("joao@email.com")
                .password("password")
                .roles("USER")
                .build();
    }

    private Client createClient() {
        return Client.reconstitute(
                1L, "JOÃO SILVA", VALID_CPF, "joao@email.com", "11999998888",
                true, LocalDateTime.now(), LocalDateTime.now());
    }

    private ClientRequest createRequest() {
        return new ClientRequest("João Silva", VALID_CPF, "joao@email.com", "11999998888");
    }

    @Test
    void whenCreatingClientWithValidData_thenReturns201() throws Exception {
        when(clientRepository.existsByCpf(VALID_CPF)).thenReturn(false);
        when(clientRepository.save(any(Client.class))).thenAnswer(i -> {
            Client c = i.getArgument(0);
            return Client.reconstitute(
                    1L, c.getName(), c.getCpf().getValue(), c.getEmail(), c.getPhone(),
                    c.isActive(), LocalDateTime.now(), LocalDateTime.now());
        });

        mockMvc.perform(post("/api/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("JOÃO SILVA"))
                .andExpect(jsonPath("$.cpf").value("529.982.247-25"))
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    void whenFindingClientByExistingId_thenReturns200() throws Exception {
        when(clientRepository.findById(1L)).thenReturn(Optional.of(createClient()));

        mockMvc.perform(get("/api/clients/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("JOÃO SILVA"))
                .andExpect(jsonPath("$.cpf").value("529.982.247-25"));
    }

    @Test
    void whenFindingClientByExistingCpf_thenReturns200() throws Exception {
        when(clientRepository.findByCpf(VALID_CPF)).thenReturn(Optional.of(createClient()));

        mockMvc.perform(get("/api/clients/cpf/{cpf}", VALID_CPF))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("JOÃO SILVA"))
                .andExpect(jsonPath("$.cpf").value("529.982.247-25"));
    }

    @Test
    void whenFindingAllClients_thenReturns200WithList() throws Exception {
        when(clientRepository.findAllActive()).thenReturn(List.of(createClient()));

        mockMvc.perform(get("/api/clients"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("JOÃO SILVA"));
    }

    @Test
    void whenUpdatingClientWithValidData_thenReturns200() throws Exception {
        Client client = createClient();
        when(clientRepository.findById(1L)).thenReturn(Optional.of(client));
        when(clientRepository.save(any(Client.class))).thenAnswer(i -> i.getArgument(0));

        ClientRequest updateRequest = new ClientRequest("Maria Souza", VALID_CPF, "maria@email.com", "11888887777");

        mockMvc.perform(put("/api/clients/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Maria Souza"))
                .andExpect(jsonPath("$.email").value("maria@email.com"));
    }

    @Test
    void whenDeactivatingClient_thenReturns204() throws Exception {
        Client client = createClient();
        when(clientRepository.findById(1L)).thenReturn(Optional.of(client));
        when(clientRepository.save(any(Client.class))).thenAnswer(i -> i.getArgument(0));

        mockMvc.perform(delete("/api/clients/{id}", 1L))
                .andExpect(status().isNoContent());
    }

    // ==================== Client Portal E2E Tests ====================

    private com.os.workshop.features.serviceorder.shared.repository.ServiceOrderEntity createOrder(java.util.UUID id) {
        var order = new com.os.workshop.features.serviceorder.shared.repository.ServiceOrderEntity();
        order.setId(id);
        order.setCpfCnpj(VALID_CPF);
        order.setServiceStatus("RECEBIDA");
        order.setListService(List.of("TROCA_OLEO", "ALINHAMENTO"));
        order.setPlacaVeiculo("ABC1234");
        order.setServiceTypeName("[TROCA_OLEO, ALINHAMENTO]");
        return order;
    }

    @Test
    void whenFindingMyOrders_thenReturns200WithOrderList() throws Exception {
        Client client = createClient();
        java.util.UUID orderId = java.util.UUID.randomUUID();

        when(clientRepository.findByEmail("joao@email.com")).thenReturn(Optional.of(client));
        when(serviceOrderJpaRepository.findByCpfCnpj(VALID_CPF)).thenReturn(List.of(createOrder(orderId)));

        mockMvc.perform(get("/api/clients/my-orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].orderId").value(orderId.toString()))
                .andExpect(jsonPath("$[0].placaVeiculo").value("ABC1234"))
                .andExpect(jsonPath("$[0].serviceStatus").value("RECEBIDA"));
    }

    @Test
    void whenFindingMyOrderDetail_thenReturns200WithBudget() throws Exception {
        Client client = createClient();
        java.util.UUID orderId = java.util.UUID.randomUUID();

        when(clientRepository.findByEmail("joao@email.com")).thenReturn(Optional.of(client));
        when(serviceOrderJpaRepository.findById(orderId)).thenReturn(Optional.of(createOrder(orderId)));
        when(budgetService.findByServiceOrderId(orderId)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/clients/my-orders/{orderId}", orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value(orderId.toString()))
                .andExpect(jsonPath("$.placaVeiculo").value("ABC1234"))
                .andExpect(jsonPath("$.budget").isEmpty());
    }

    @Test
    void whenApprovingMyOrder_thenReturns200WithApprovedStatus() throws Exception {
        Client client = createClient();
        java.util.UUID orderId = java.util.UUID.randomUUID();

        var order = createOrder(orderId);
        order.setServiceStatus("AGUARDANDO_APROVACAO");

        when(clientRepository.findByEmail("joao@email.com")).thenReturn(Optional.of(client));
        when(serviceOrderJpaRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(serviceOrderJpaRepository.save(any(com.os.workshop.features.serviceorder.shared.repository.ServiceOrderEntity.class)))
                .thenAnswer(i -> i.getArgument(0));
        when(budgetService.findByServiceOrderId(orderId)).thenReturn(Optional.empty());

        mockMvc.perform(patch("/api/clients/my-orders/{orderId}/approve", orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value(orderId.toString()))
                .andExpect(jsonPath("$.serviceStatus").value("APROVADO"));
    }
}
