package com.os.workshop.domain.serviceorder;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ServiceOrderTest {

    @Nested
    class CreateTests {

        @Test
        void createsOrderWithValidData() {
            ServiceOrder order = ServiceOrder.create("12345678900", "ABC1D23", List.of("TROCA_OLEO"));

            assertNotNull(order.getId());
            assertEquals("12345678900", order.getCpfCnpj());
            assertEquals("ABC1D23", order.getPlacaVeiculo());
            assertEquals(List.of("TROCA_OLEO"), order.getListService());
            assertEquals(OrderServiceStatusEnum.RECEBIDA.getStatus(), order.getServiceStatus());
            assertNull(order.getRejectionReason());
        }

        @Test
        void throwsWhenCpfCnpjNull() {
            assertThrows(NullPointerException.class,
                    () -> ServiceOrder.create(null, "ABC1D23", List.of("TROCA_OLEO")));
        }

        @Test
        void throwsWhenPlacaNull() {
            assertThrows(NullPointerException.class,
                    () -> ServiceOrder.create("12345678900", null, List.of("TROCA_OLEO")));
        }

        @Test
        void throwsWhenServiceTypesEmpty() {
            assertThrows(IllegalArgumentException.class,
                    () -> ServiceOrder.create("12345678900", "ABC1D23", List.of()));
        }

        @Test
        void throwsWhenServiceTypesNull() {
            assertThrows(IllegalArgumentException.class,
                    () -> ServiceOrder.create("12345678900", "ABC1D23", null));
        }

        @Test
        void normalizesPlacaToUpperCase() {
            ServiceOrder order = ServiceOrder.create("12345678900", "abc1d23", List.of("TROCA_OLEO"));
            assertEquals("ABC1D23", order.getPlacaVeiculo());
        }
    }

    @Nested
    class ReconstituteTests {

        @Test
        void reconstitutesWithAllFields() {
            UUID id = UUID.randomUUID();
            LocalDateTime now = LocalDateTime.now();

            ServiceOrder order = ServiceOrder.reconstitute(
                    id, "12345678900", "ABC1D23",
                    List.of("TROCA_OLEO"), "[TROCA_OLEO]",
                    OrderServiceStatusEnum.AGUARDANDO_APROVACAO, null,
                    now, now
            );

            assertEquals(id, order.getId());
            assertEquals("12345678900", order.getCpfCnpj());
            assertEquals(OrderServiceStatusEnum.AGUARDANDO_APROVACAO.getStatus(), order.getServiceStatus());
            assertEquals(now, order.getCreatedAt());
        }
    }

    @Nested
    class StateMachineTests {

        private ServiceOrder orderWithStatus(OrderServiceStatusEnum status) {
            return ServiceOrder.reconstitute(
                    UUID.randomUUID(), "12345678900", "ABC1D23",
                    List.of("TROCA_OLEO"), "[TROCA_OLEO]",
                    status, null,
                    LocalDateTime.now(), LocalDateTime.now()
            );
        }

        @Test
        void recebidaToEmDiagnostico() {
            ServiceOrder order = orderWithStatus(OrderServiceStatusEnum.RECEBIDA);
            order.advanceTo(OrderServiceStatusEnum.EM_DIAGNOSTICO);
            assertEquals(OrderServiceStatusEnum.EM_DIAGNOSTICO.getStatus(), order.getServiceStatus());
        }

        @Test
        void emDiagnosticoToAguardandoAprovacao() {
            ServiceOrder order = orderWithStatus(OrderServiceStatusEnum.EM_DIAGNOSTICO);
            order.advanceTo(OrderServiceStatusEnum.AGUARDANDO_APROVACAO);
            assertEquals(OrderServiceStatusEnum.AGUARDANDO_APROVACAO.getStatus(), order.getServiceStatus());
        }

        @Test
        void aguardandoAprovacaoToAprovado() {
            ServiceOrder order = orderWithStatus(OrderServiceStatusEnum.AGUARDANDO_APROVACAO);
            order.advanceTo(OrderServiceStatusEnum.APROVADO);
            assertEquals(OrderServiceStatusEnum.APROVADO.getStatus(), order.getServiceStatus());
        }

        @Test
        void aguardandoAprovacaoToRecusada() {
            ServiceOrder order = orderWithStatus(OrderServiceStatusEnum.AGUARDANDO_APROVACAO);
            order.advanceTo(OrderServiceStatusEnum.RECUSADA);
            assertEquals(OrderServiceStatusEnum.RECUSADA.getStatus(), order.getServiceStatus());
        }

        @Test
        void aprovadoToEmExecucao() {
            ServiceOrder order = orderWithStatus(OrderServiceStatusEnum.APROVADO);
            order.advanceTo(OrderServiceStatusEnum.EM_EXECUCAO);
            assertEquals(OrderServiceStatusEnum.EM_EXECUCAO.getStatus(), order.getServiceStatus());
        }

        @Test
        void emExecucaoToFinalizada() {
            ServiceOrder order = orderWithStatus(OrderServiceStatusEnum.EM_EXECUCAO);
            order.advanceTo(OrderServiceStatusEnum.FINALIZADA);
            assertEquals(OrderServiceStatusEnum.FINALIZADA.getStatus(), order.getServiceStatus());
        }

        @Test
        void finalizadaToEntregue() {
            ServiceOrder order = orderWithStatus(OrderServiceStatusEnum.FINALIZADA);
            order.advanceTo(OrderServiceStatusEnum.ENTREGUE);
            assertEquals(OrderServiceStatusEnum.ENTREGUE.getStatus(), order.getServiceStatus());
        }

        @Test
        void recusadaToEmDiagnostico_reopen() {
            ServiceOrder order = orderWithStatus(OrderServiceStatusEnum.RECUSADA);
            order.reopen();
            assertEquals(OrderServiceStatusEnum.EM_DIAGNOSTICO.getStatus(), order.getServiceStatus());
        }

        @Test
        void recebidaCannotGoDirectlyToAprovado() {
            ServiceOrder order = orderWithStatus(OrderServiceStatusEnum.RECEBIDA);
            assertThrows(InvalidStatusTransitionException.class,
                    () -> order.advanceTo(OrderServiceStatusEnum.APROVADO));
        }

        @Test
        void recebidaCannotGoToFinalizada() {
            ServiceOrder order = orderWithStatus(OrderServiceStatusEnum.RECEBIDA);
            assertThrows(InvalidStatusTransitionException.class,
                    () -> order.advanceTo(OrderServiceStatusEnum.FINALIZADA));
        }

        @Test
        void entregueCannotTransition() {
            ServiceOrder order = orderWithStatus(OrderServiceStatusEnum.ENTREGUE);
            assertThrows(InvalidStatusTransitionException.class,
                    () -> order.advanceTo(OrderServiceStatusEnum.RECEBIDA));
        }

        @Test
        void aprovadoCannotGoBackToRecebida() {
            ServiceOrder order = orderWithStatus(OrderServiceStatusEnum.APROVADO);
            assertThrows(InvalidStatusTransitionException.class,
                    () -> order.advanceTo(OrderServiceStatusEnum.RECEBIDA));
        }

        @Test
        void emDiagnosticoCannotGoToRecusada() {
            ServiceOrder order = orderWithStatus(OrderServiceStatusEnum.EM_DIAGNOSTICO);
            assertThrows(InvalidStatusTransitionException.class,
                    () -> order.advanceTo(OrderServiceStatusEnum.RECUSADA));
        }

        @Test
        void throwsWhenTargetStatusNull() {
            ServiceOrder order = orderWithStatus(OrderServiceStatusEnum.RECEBIDA);
            assertThrows(NullPointerException.class,
                    () -> order.advanceTo(null));
        }
    }

    @Nested
    class RejectTests {

        @Test
        void rejectFromAguardandoAprovacao() {
            ServiceOrder order = ServiceOrder.reconstitute(
                    UUID.randomUUID(), "12345678900", "ABC1D23",
                    List.of("TROCA_OLEO"), "[TROCA_OLEO]",
                    OrderServiceStatusEnum.AGUARDANDO_APROVACAO, null,
                    LocalDateTime.now(), LocalDateTime.now()
            );

            order.reject("Preco muito alto");

            assertEquals(OrderServiceStatusEnum.RECUSADA.getStatus(), order.getServiceStatus());
            assertEquals("Preco muito alto", order.getRejectionReason());
        }

        @Test
        void rejectWithNullReason() {
            ServiceOrder order = ServiceOrder.reconstitute(
                    UUID.randomUUID(), "12345678900", "ABC1D23",
                    List.of("TROCA_OLEO"), "[TROCA_OLEO]",
                    OrderServiceStatusEnum.AGUARDANDO_APROVACAO, null,
                    LocalDateTime.now(), LocalDateTime.now()
            );

            order.reject(null);

            assertEquals(OrderServiceStatusEnum.RECUSADA.getStatus(), order.getServiceStatus());
            assertNull(order.getRejectionReason());
        }

        @Test
        void cannotRejectFromRecebida() {
            ServiceOrder order = ServiceOrder.reconstitute(
                    UUID.randomUUID(), "12345678900", "ABC1D23",
                    List.of("TROCA_OLEO"), "[TROCA_OLEO]",
                    OrderServiceStatusEnum.RECEBIDA, null,
                    LocalDateTime.now(), LocalDateTime.now()
            );

            assertThrows(InvalidStatusTransitionException.class,
                    () -> order.reject("Motivo qualquer"));
        }
    }

    @Nested
    class ReopenTests {

        @Test
        void reopenFromRecusada() {
            ServiceOrder order = ServiceOrder.reconstitute(
                    UUID.randomUUID(), "12345678900", "ABC1D23",
                    List.of("TROCA_OLEO"), "[TROCA_OLEO]",
                    OrderServiceStatusEnum.RECUSADA, "Preco alto",
                    LocalDateTime.now(), LocalDateTime.now()
            );

            order.reopen();

            assertEquals(OrderServiceStatusEnum.EM_DIAGNOSTICO.getStatus(), order.getServiceStatus());
            assertEquals("Preco alto", order.getRejectionReason()); // mantido como historico
        }

        @Test
        void cannotReopenFromAprovado() {
            ServiceOrder order = ServiceOrder.reconstitute(
                    UUID.randomUUID(), "12345678900", "ABC1D23",
                    List.of("TROCA_OLEO"), "[TROCA_OLEO]",
                    OrderServiceStatusEnum.APROVADO, null,
                    LocalDateTime.now(), LocalDateTime.now()
            );

            assertThrows(InvalidStatusTransitionException.class, order::reopen);
        }
    }
}
