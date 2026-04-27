package com.os.features.user.repository;

import com.os.features.user.domain.Role;

public interface RoleRepository {
    Role findByRoleName(String roleName);
}
