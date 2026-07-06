package com.os.workshop.infrastructure.persistence.budget;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface BudgetJpaRepository extends JpaRepository<BudgetEntity, Long> {
    Optional<BudgetEntity> findByServiceOrderId(UUID serviceOrderId);
}
