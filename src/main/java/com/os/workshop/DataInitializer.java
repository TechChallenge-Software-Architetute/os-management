package com.os.workshop;

import com.os.features.client.domain.Client;
import com.os.features.client.repository.ClientRepository;
import com.os.features.vehicle.domain.Vehicle;
import com.os.features.vehicle.domain.VehicleType;
import com.os.features.vehicle.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Inicializador de dados de teste executado automaticamente na subida da aplicação.
 *
 * <p>Estratégia de execução:
 * <ul>
 *   <li>Verifica se já existem clientes cadastrados — se sim, <b>não executa</b> (idempotente).</li>
 *   <li>Se o banco estiver vazio, insere 3 clientes e 3 veículos conforme o cenário abaixo.</li>
 * </ul>
 *
 * <p>Cenário de dados:
 * <ul>
 *   <li><b>João da Silva</b> — 2 veículos (Toyota Corolla + Honda CG 160)</li>
 *   <li><b>Maria Souza</b>   — 1 veículo (Volkswagen Gol)</li>
 *   <li><b>Carlos Oliveira</b> — sem veículo</li>
 * </ul>
 *
 * <p>Os objetos de domínio são criados via factory methods ({@link Client#create} e {@link Vehicle#create}),
 * garantindo que todas as regras de negócio e validações (CPF, placa) sejam respeitadas mesmo na carga inicial.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final ClientRepository clientRepository;
    private final VehicleRepository vehicleRepository;

    /**
     * Ponto de entrada chamado pelo Spring após o contexto estar completamente carregado.
     * A anotação {@link Transactional} garante que toda a carga inicial seja atômica —
     * em caso de erro, nenhum dado parcial é persistido.
     *
     * @param args argumentos de linha de comando (não utilizados)
     */
    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (!clientRepository.findAllActive().isEmpty()) {
            log.info("[DataInitializer] Banco de dados já possui dados. Carga inicial ignorada.");
            return;
        }

        log.info("[DataInitializer] Iniciando carga de dados de teste...");

        seedJoao();
        seedMaria();
        seedCarlos();

        log.info("[DataInitializer] Carga concluída: 3 clientes e 3 veículos inseridos com sucesso.");
    }

    /**
     * João da Silva — cliente com <b>2 veículos</b>: Toyota Corolla e Honda Civic.
     * CPF: 529.982.247-25
     */
    private void seedJoao() {
        Client joao = Client.create(
                "João da Silva",
                "52998224725",
                "joao.silva@email.com",
                "(11) 99999-1234"
        );
        joao = clientRepository.save(joao);
        log.info("[DataInitializer] Cliente criado: {} (id={})", joao.getName(), joao.getId());

        // Veículo 1 — Toyota Corolla (carro, placa formato antigo)
        Vehicle corolla = Vehicle.create(
                joao.getId(),
                "ABC1234",
                "Toyota",
                "Corolla",
                2020,
                "Prata",
                VehicleType.CAR
        );
        vehicleRepository.save(corolla);
        log.info("[DataInitializer]   -> Veículo: Toyota Corolla 2020 | Placa: ABC-1234");

        // Veículo 2 — Honda Civic (carro, placa formato Mercosul)
        Vehicle civic = Vehicle.create(
                joao.getId(),
                "XYZ1A23",
                "Honda",
                "Civic",
                2021,
                "Preto",
                VehicleType.CAR
        );
        vehicleRepository.save(civic);
        log.info("[DataInitializer]   -> Veículo: Honda Civic 2021   | Placa: XYZ-1A23");
    }

    /**
     * Maria Souza — cliente com <b>1 veículo</b>: um carro popular.
     * CPF: 071.246.320-80
     */
    private void seedMaria() {
        Client maria = Client.create(
                "Maria Souza",
                "07124632080",
                "maria.souza@email.com",
                "(21) 98888-5678"
        );
        maria = clientRepository.save(maria);
        log.info("[DataInitializer] Cliente criado: {} (id={})", maria.getName(), maria.getId());

        // Veículo — Volkswagen Gol (carro, placa formato antigo)
        Vehicle gol = Vehicle.create(
                maria.getId(),
                "DEF5678",
                "Volkswagen",
                "Gol",
                2019,
                "Branco",
                VehicleType.CAR
        );
        vehicleRepository.save(gol);
        log.info("[DataInitializer]   -> Veículo: Volkswagen Gol 2019 | Placa: DEF-5678");
    }

    /**
     * Carlos Oliveira — cliente <b>sem veículo</b> cadastrado.
     * Representa o caso de um cliente recém-cadastrado que ainda não trouxe o veículo.
     * CPF: 187.468.800-11
     */
    private void seedCarlos() {
        Client carlos = Client.create(
                "Carlos Oliveira",
                "18746880011",
                "carlos.oliveira@email.com",
                "(31) 97777-9012"
        );
        clientRepository.save(carlos);
        log.info("[DataInitializer] Cliente criado: {} (id={}) — sem veículo", carlos.getName(), carlos.getId());
    }
}
