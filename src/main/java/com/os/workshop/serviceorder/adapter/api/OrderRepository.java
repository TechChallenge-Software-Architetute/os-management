package com.os.workshop.serviceorder.adapter.api;

import com.os.workshop.serviceorder.domain.ServiceOrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<ServiceOrderEntity, UUID> {
}
