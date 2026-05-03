package com.os.workshop.features.product.shared.repository;

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
}
