package com.os.workshop.validators.validator;

import com.os.workshop.validators.annotation.Document;
import com.os.workshop.validators.enums.DocumentType;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class DocumentValidator implements ConstraintValidator<Document, String> {

    private DocumentType type;

    @Override
    public void initialize(Document annotation) {
        this.type = annotation.type();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) return false;

        value = value.replaceAll("\\D", "");

        return switch (type) {
            case CPF -> validateCpf(value);
            case CNPJ -> validateCnpj(value);
        };
    }

    private boolean validateCpf(String cpf) {
        if (cpf.length() != 11 || cpf.matches("(\\d)\\1{10}")) return false;

        int sum = 0;
        for (int i = 0; i < 9; i++) {
            sum += (cpf.charAt(i) - '0') * (10 - i);
        }

        int dig1 = 11 - (sum % 11);
        if (dig1 >= 10) dig1 = 0;

        sum = 0;
        for (int i = 0; i < 10; i++) {
            sum += (cpf.charAt(i) - '0') * (11 - i);
        }

        int dig2 = 11 - (sum % 11);
        if (dig2 >= 10) dig2 = 0;

        return dig1 == (cpf.charAt(9) - '0') &&
                dig2 == (cpf.charAt(10) - '0');
    }

    private boolean validateCnpj(String cnpj) {
        if (cnpj.length() != 14 || cnpj.matches("(\\d)\\1{13}")) return false;

        int[] pesos1 = {5,4,3,2,9,8,7,6,5,4,3,2};
        int[] pesos2 = {6,5,4,3,2,9,8,7,6,5,4,3,2};

        int sum = 0;
        for (int i = 0; i < 12; i++) {
            sum += (cnpj.charAt(i) - '0') * pesos1[i];
        }

        int dig1 = sum % 11;
        dig1 = (dig1 < 2) ? 0 : 11 - dig1;

        sum = 0;
        for (int i = 0; i < 13; i++) {
            sum += (cnpj.charAt(i) - '0') * pesos2[i];
        }

        int dig2 = sum % 11;
        dig2 = (dig2 < 2) ? 0 : 11 - dig2;

        return dig1 == (cnpj.charAt(12) - '0') &&
                dig2 == (cnpj.charAt(13) - '0');
    }
}