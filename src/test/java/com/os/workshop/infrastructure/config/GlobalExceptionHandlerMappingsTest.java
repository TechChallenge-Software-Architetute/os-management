package com.os.workshop.infrastructure.config;

import com.os.workshop.domain.client.ClientNotFoundException;
import com.os.workshop.domain.serviceorder.InvalidStatusTransitionException;
import com.os.workshop.domain.vehicle.VehicleNotFoundException;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/** Mapeamento exceção -> status HTTP dos handlers que o GlobalExceptionHandlerTest não cobre. */
class GlobalExceptionHandlerMappingsTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void clienteEVeiculoNaoEncontradosRetornam404() {
        assertThat(handler.handleClientNotFound(new ClientNotFoundException("cpf: 1")).getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(handler.handleVehicleNotFound(new VehicleNotFoundException("placa: X")).getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(handler.handleNotFound(new NoSuchElementException("nao existe")).getBody())
                .containsEntry("status", 404)
                .containsEntry("error", "nao existe");
    }

    @Test
    void chaveDuplicadaVira409ComMensagemAmigavel() {
        var ex = new DataIntegrityViolationException("falhou",
                new RuntimeException("ERROR: duplicate key value violates unique constraint"));

        var response = handler.handleDataIntegrity(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat((String) response.getBody().get("error")).startsWith("Registro duplicado");
    }

    @Test
    void outraViolacaoDeIntegridadeVira409Generico() {
        var response = handler.handleDataIntegrity(new DataIntegrityViolationException("fk violada"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).containsEntry("error", "Violação de integridade dos dados.");
    }

    @Test
    void transicaoInvalidaVira422() {
        var response = handler.handleInvalidStatusTransition(new InvalidStatusTransitionException("RECEBIDA -> ENTREGUE"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
        assertThat(response.getBody()).containsEntry("status", 422);
    }

    @Test
    void acessoNegadoVira403() {
        var response = handler.handleSecurityException(new SecurityException("OS de outro cliente"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void violacaoDeConstraintVira400() {
        var response = handler.handleConstraintViolation(new ConstraintViolationException("invalido", Set.of()));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).containsEntry("errors", List.of());
    }

    @Test
    void erroDeNegocioGenericoVira422() {
        var response = handler.handleRuntime(new RuntimeException("regra violada"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
        assertThat(response.getBody()).containsEntry("error", "regra violada");
    }
}
