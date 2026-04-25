package com.os.features.product.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@DiscriminatorValue("PART")
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
