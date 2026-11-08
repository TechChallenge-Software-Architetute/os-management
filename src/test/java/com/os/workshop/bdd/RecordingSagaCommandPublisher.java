package com.os.workshop.bdd;

import com.os.workshop.application.saga.OrderSagaCommand;
import com.os.workshop.application.saga.SagaAction;
import com.os.workshop.application.saga.port.out.SagaCommandPublisherPort;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Duble da porta de saida da saga: guarda as mensagens na ordem em que sairiam
 * para a fila do os-orchestrator.
 */
public class RecordingSagaCommandPublisher implements SagaCommandPublisherPort {

    private final List<OrderSagaCommand> commands = new CopyOnWriteArrayList<>();

    @Override
    public void publish(OrderSagaCommand command) {
        commands.add(command);
    }

    public List<SagaAction> actionsFor(UUID orderId) {
        return commands.stream()
                .filter(command -> command.orderId().equals(orderId))
                .map(OrderSagaCommand::action)
                .toList();
    }
}
