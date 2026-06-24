package com.os.workshop.features.user.shared.repository;

import com.os.workshop.features.user.shared.domain.Role;

public interface RoleRepository {
    Role findByRoleName(String roleName);
}
