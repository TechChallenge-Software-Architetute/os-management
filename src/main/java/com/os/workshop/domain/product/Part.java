package com.os.workshop.domain.product;

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
}
