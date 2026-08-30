package com.os.workshop.domain.shared;

import java.util.regex.Pattern;

/**
 * Value object que representa um CPF brasileiro.
 * Compartilhado entre os domínios que precisam de CPF (client, user, ...).
 */
public final class Cpf {

    private static final Pattern NON_DIGIT = Pattern.compile("[^0-9]");

    private final String value;

    public Cpf(String raw) {
        if (raw == null) {
            throw new IllegalArgumentException("CPF cannot be null");
        }
        String normalized = NON_DIGIT.matcher(raw).replaceAll("");
        validate(normalized);
        this.value = normalized;
    }

    public String getValue() {
        return value;
    }

    public String formatted() {
        return value.substring(0, 3) + "."
             + value.substring(3, 6) + "."
             + value.substring(6, 9) + "-"
             + value.substring(9);
    }

    private static void validate(String cpf) {
        if (cpf.length() != 11) {
            throw new IllegalArgumentException("CPF must have 11 digits, got: " + cpf.length());
        }
        if (cpf.chars().distinct().count() == 1) {
            throw new IllegalArgumentException("CPF cannot have all identical digits");
        }

        int sum = 0;
        for (int i = 0; i < 9; i++) {
            sum += (cpf.charAt(i) - '0') * (10 - i);
        }
        int firstDigit = 11 - (sum % 11);
        if (firstDigit >= 10) firstDigit = 0;
        if (firstDigit != (cpf.charAt(9) - '0')) {
            throw new IllegalArgumentException("Invalid CPF: wrong first check digit");
        }

        sum = 0;
        for (int i = 0; i < 10; i++) {
            sum += (cpf.charAt(i) - '0') * (11 - i);
        }
        int secondDigit = 11 - (sum % 11);
        if (secondDigit >= 10) secondDigit = 0;
        if (secondDigit != (cpf.charAt(10) - '0')) {
            throw new IllegalArgumentException("Invalid CPF: wrong second check digit");
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Cpf other)) return false;
        return value.equals(other.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public String toString() {
        return formatted();
    }
}
