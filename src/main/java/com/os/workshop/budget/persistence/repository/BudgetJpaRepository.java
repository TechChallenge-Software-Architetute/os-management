package com.os.workshop.budget.persistence.repository;

import com.os.workshop.budget.persistence.entity.BudgetEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface BudgetJpaRepository extends JpaRepository<BudgetEntity, Long> {

    Optional<BudgetEntity> findByServiceOrderId(UUID serviceOrderId);
}
