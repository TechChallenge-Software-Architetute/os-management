package com.os.features.product.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Part extends Product {

    private String manufacturerCode; // código do fabricante
    private int warrantyMonths;

    // TODO: Add compatibility list of vehicles
    //private List<Vehicle> vehiclesCompatibilityList;


}