package com.os.workshop.product.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "parts")
public class PartEntity extends ProductEntity {

    private String manufacturerCode;

    @Column(nullable = false)
    private int warrantyMonths;

    // TODO: Add compatibility list when Vehicle entity is created
    // @ManyToMany
    // @JoinTable(
    //     name = "part_vehicle_compatibility",
    //     joinColumns = @JoinColumn(name = "part_id"),
    //     inverseJoinColumns = @JoinColumn(name = "vehicle_id")
    // )
    // private List<VehicleEntity> compatibleVehicles = new ArrayList<>();
}
