package com.os.workshop.infrastructure.persistence.user;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface JpaRoleRepository extends JpaRepository<RoleEntity, UUID> {
    RoleEntity findByName(String role);
}
