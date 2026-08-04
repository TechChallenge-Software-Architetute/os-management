package com.os.workshop.infrastructure.persistence.serviceorder;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface ServiceOrderJpaRepository extends JpaRepository<ServiceOrderEntity, UUID> {

    List<ServiceOrderEntity> findByCpfCnpj(String cpfCnpj);

    @Query(value = """
            SELECT * FROM service_order
            WHERE service_status NOT IN ('FINALIZADA', 'ENTREGUE')
            ORDER BY
                CASE service_status
                    WHEN 'EM_EXECUCAO' THEN 1
                    WHEN 'AGUARDANDO_APROVACAO' THEN 2
                    WHEN 'EM_DIAGNOSTICO' THEN 3
                    WHEN 'RECEBIDA' THEN 4
                    WHEN 'APROVADO' THEN 5
                    WHEN 'RECUSADA' THEN 6
                    ELSE 7
                END,
                created_at ASC
            """, nativeQuery = true)
    List<ServiceOrderEntity> findActiveOrdersSorted();
}
