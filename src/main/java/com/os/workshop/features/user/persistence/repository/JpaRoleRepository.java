package com.os.workshop.features.user.persistence.repository;

import com.os.workshop.features.user.persistence.entity.RoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface JpaRoleRepository extends JpaRepository<RoleEntity, UUID> {
    RoleEntity findByName(String role);
}