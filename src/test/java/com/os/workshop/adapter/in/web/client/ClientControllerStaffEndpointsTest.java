package com.os.workshop.adapter.in.web.client;

import com.os.workshop.application.client.CreateClientUseCase;
import com.os.workshop.application.client.DeactivateClientUseCase;
import com.os.workshop.application.client.DecideOrderUseCase;
import com.os.workshop.application.client.FindClientByCpfUseCase;
import com.os.workshop.application.client.FindClientByIdUseCase;
import com.os.workshop.application.client.FindMyOrderDetailUseCase;
import com.os.workshop.application.client.FindMyOrdersUseCase;
import com.os.workshop.application.client.ListClientsUseCase;
import com.os.workshop.application.client.UpdateClientUseCase;
import com.os.workshop.domain.client.Client;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Endpoints de cadastro de clientes usados pela oficina (os do cliente ficam no BDD). */
@ExtendWith(MockitoExtension.class)
class ClientControllerStaffEndpointsTest {

    private static final String CPF = "52998224725";

    @Mock private CreateClientUseCase createClientUseCase;
    @Mock private ListClientsUseCase listClientsUseCase;
    @Mock private FindClientByIdUseCase findClientByIdUseCase;
    @Mock private FindClientByCpfUseCase findClientByCpfUseCase;
    @Mock private UpdateClientUseCase updateClientUseCase;
    @Mock private DeactivateClientUseCase deactivateClientUseCase;
    @Mock private FindMyOrdersUseCase findMyOrdersUseCase;
    @Mock private FindMyOrderDetailUseCase findMyOrderDetailUseCase;
    @Mock private DecideOrderUseCase decideOrderUseCase;

    @InjectMocks private ClientController controller;

    @Test
    void listaClientes() {
        when(listClientsUseCase.execute()).thenReturn(List.of(client(), client()));

        var response = controller.findAll();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(2);
    }

    @Test
    void buscaPorIdEPorCpf() {
        when(findClientByIdUseCase.execute(1L)).thenReturn(client());
        when(findClientByCpfUseCase.execute(CPF)).thenReturn(client());

        assertThat(controller.findById(1L).getBody().document()).isEqualTo(CPF);
        assertThat(controller.findByCpf(CPF).getBody().id()).isEqualTo(1L);
    }

    @Test
    void atualizaCliente() {
        when(updateClientUseCase.execute(1L, "JOAO DA SILVA", "joao@test.com", "11999999999")).thenReturn(client());

        var response = controller.update(1L,
                new UpdateClientRequest("JOAO DA SILVA", CPF, "joao@test.com", "11999999999"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().name()).isEqualTo("JOAO DA SILVA");
    }

    @Test
    void desativaCliente() {
        assertThat(controller.deactivate(1L).getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(deactivateClientUseCase).execute(1L);
    }

    private Client client() {
        return Client.reconstitute(1L, "JOAO DA SILVA", CPF, "joao@test.com", "11999999999", true,
                LocalDateTime.now(), LocalDateTime.now());
    }
}
