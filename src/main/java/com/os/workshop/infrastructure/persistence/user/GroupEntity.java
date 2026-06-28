package com.os.workshop.infrastructure.persistence.user;

import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

@Entity @Table(name = "groups")
@Getter @Setter @AllArgsConstructor @NoArgsConstructor
public class GroupEntity {
    @Id @GeneratedValue private UUID id;
    private String name;
}
