package com.os.workshop.infrastructure.persistence.user;

import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

@Entity @Table(name = "roles")
@Getter @Setter @AllArgsConstructor @NoArgsConstructor
public class RoleEntity {
    @Id @GeneratedValue private UUID id;
    private String name;
}
