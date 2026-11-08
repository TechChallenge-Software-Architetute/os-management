package com.os.workshop.bdd;

import com.os.workshop.application.saga.SagaAction;
import com.os.workshop.application.saga.SagaMessage;
import com.os.workshop.application.saga.port.out.SagaCommandPublisherPort;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Duble da porta de saida da saga: guarda as mensagens na ordem em que sairiam
 * para a fila do os-orchestrator.
 */
public class RecordingSagaCommandPublisher implements SagaCommandPublisherPort {

    private final List<SagaMessage> messages = new CopyOnWriteArrayList<>();

    @Override
    public void publish(SagaMessage message) {
        messages.add(message);
    }

    public List<SagaMessage> messagesFor(UUID orderId) {
        return messages.stream()
                .filter(message -> message.orderId().equals(orderId))
                .toList();
    }

    public List<SagaAction> actionsFor(UUID orderId) {
        return messagesFor(orderId).stream().map(SagaMessage::action).toList();
    }
}
