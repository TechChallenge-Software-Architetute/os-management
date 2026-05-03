package com.os.workshop.features.vehicle;

import com.os.workshop.features.client.shared.exception.ClientNotFoundException;
import com.os.workshop.features.client.shared.repository.ClientRepository;
import com.os.workshop.features.vehicle.domain.Vehicle;
import com.os.workshop.features.vehicle.dto.VehicleRequest;
import com.os.workshop.features.vehicle.exception.VehicleNotFoundException;
import com.os.workshop.features.vehicle.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Serviço de aplicação responsável pelos casos de uso de veículo.
 *
 * <p>Orquestra as validações e delegações entre as features {@code client} e {@code vehicle}:
 * <ul>
 *   <li>Valida a existência do cliente proprietário antes de criar ou listar veículos.</li>
 *   <li>Garante unicidade da placa no cadastro e na atualização.</li>
 *   <li>Invoca as regras de negócio encapsuladas no aggregate {@link Vehicle}.</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
public class VehicleService {

    private final VehicleRepository vehicleRepository;
    private final ClientRepository clientRepository;

    /**
     * Cadastra um novo veículo para um cliente existente.
     *
     * @param request dados do veículo a cadastrar
     * @return veículo persistido com ID e timestamps preenchidos
     * @throws ClientNotFoundException  se o cliente informado não existir
     * @throws IllegalStateException    se a placa já estiver cadastrada
     * @throws IllegalArgumentException se a placa tiver formato inválido (lançado pelo value object)
     */
    @Transactional
    public Vehicle create(VehicleRequest request) {
        clientRepository.findById(request.clientId())
                .orElseThrow(() -> new ClientNotFoundException("id: " + request.clientId()));

        String normalizedPlate = normalizePlate(request.plate());
        if (vehicleRepository.existsByPlate(normalizedPlate)) {
            throw new IllegalStateException("A vehicle with plate '" + request.plate() + "' already exists");
        }

        Vehicle vehicle = Vehicle.create(
                request.clientId(), request.plate(),
                request.brand(), request.model(),
                request.year(), request.color(), request.type()
        );
        return vehicleRepository.save(vehicle);
    }

    /**
     * Busca um veículo pelo seu ID interno.
     *
     * @param id UUID do veículo
     * @return veículo encontrado
     * @throws VehicleNotFoundException se nenhum veículo existir com o ID informado
     */
    @Transactional(readOnly = true)
    public Vehicle findById(Long id) {
        return vehicleRepository.findById(id)
                .orElseThrow(() -> new VehicleNotFoundException("id: " + id));
    }

    /**
     * Busca um veículo pela placa.
     * Normaliza a entrada antes da consulta (aceita com ou sem hífen, qualquer capitalização).
     *
     * @param plate placa no formato antigo ou Mercosul
     * @return veículo encontrado
     * @throws VehicleNotFoundException se nenhum veículo existir com a placa informada
     */
    @Transactional(readOnly = true)
    public Vehicle findByPlate(String plate) {
        return vehicleRepository.findByPlate(normalizePlate(plate))
                .orElseThrow(() -> new VehicleNotFoundException("plate: " + plate));
    }

    /**
     * Retorna todos os veículos ativos de um cliente.
     *
     * @param clientId UUID do cliente
     * @return lista de veículos ativos do cliente
     * @throws ClientNotFoundException se o cliente não existir
     */
    @Transactional(readOnly = true)
    public List<Vehicle> findAllByClient(Long clientId) {
        clientRepository.findById(clientId)
                .orElseThrow(() -> new ClientNotFoundException("id: " + clientId));
        return vehicleRepository.findAllByClientId(clientId);
    }

    /**
     * Atualiza os dados de um veículo existente.
     * Valida unicidade da nova placa caso seja diferente da atual.
     * O cliente proprietário não pode ser alterado nesta operação.
     *
     * @param id      UUID do veículo a atualizar
     * @param request novos dados do veículo
     * @return veículo atualizado
     * @throws VehicleNotFoundException se o veículo não for encontrado
     * @throws IllegalStateException    se a nova placa já pertencer a outro veículo
     */
    @Transactional
    public Vehicle update(Long id, VehicleRequest request) {
        Vehicle vehicle = findById(id);

        String normalizedPlate = normalizePlate(request.plate());
        if (!vehicle.getPlate().getValue().equals(normalizedPlate)
                && vehicleRepository.existsByPlate(normalizedPlate)) {
            throw new IllegalStateException("A vehicle with plate '" + request.plate() + "' already exists");
        }

        vehicle.update(request.plate(), request.brand(), request.model(),
                request.year(), request.color(), request.type());
        return vehicleRepository.save(vehicle);
    }

    /**
     * Desativa um veículo (soft delete).
     * O registro permanece no banco mas é excluído das listagens ativas.
     *
     * @param id UUID do veículo a desativar
     * @throws VehicleNotFoundException se o veículo não for encontrado
     */
    @Transactional
    public void deactivate(Long id) {
        Vehicle vehicle = findById(id);
        vehicle.deactivate();
        vehicleRepository.save(vehicle);
    }

    /** Remove caracteres não alfanuméricos e converte para maiúsculas para consultas e unicidade. */
    private static String normalizePlate(String plate) {
        return plate == null ? "" : plate.replaceAll("[^A-Za-z0-9]", "").toUpperCase();
    }
}
