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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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

    @BeforeEach
    void setUp() {
        ClientService clientService = new ClientService(clientRepository);
        ClientController clientController = new ClientController(clientService);
        mockMvc = MockMvcBuilders.standaloneSetup(clientController).build();
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
}
