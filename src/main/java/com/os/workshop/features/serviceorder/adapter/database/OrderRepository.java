package com.os.workshop.features.serviceorder.adapter.database;

import com.os.workshop.features.serviceorder.domain.ServiceOrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<ServiceOrderEntity, UUID> {
}
