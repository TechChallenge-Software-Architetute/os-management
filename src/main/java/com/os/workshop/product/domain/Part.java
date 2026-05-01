package com.os.workshop.product.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Part extends Product {

    private String manufacturerCode;
    private int warrantyMonths;

    // TODO: Add compatibility list when Vehicle domain class is created
    // private List<Vehicle> compatibleVehicles = new ArrayList<>();
}
