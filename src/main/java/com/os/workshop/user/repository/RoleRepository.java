package com.os.workshop.user.repository;

import com.os.workshop.user.domain.Role;

public interface RoleRepository {
    Role findByRoleName(String roleName);
}
