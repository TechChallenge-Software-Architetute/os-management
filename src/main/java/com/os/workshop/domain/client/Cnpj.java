package com.os.workshop.domain.client;

import java.util.regex.Pattern;

public final class Cnpj {

    private static final Pattern NON_DIGIT = Pattern.compile("[^0-9]");
    private static final int[] WEIGHTS_FIRST = {5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
    private static final int[] WEIGHTS_SECOND = {6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};

    private final String value;

    public Cnpj(String raw) {
        if (raw == null) {
            throw new IllegalArgumentException("CNPJ cannot be null");
        }
        String normalized = NON_DIGIT.matcher(raw).replaceAll("");
        validate(normalized);
        this.value = normalized;
    }

    public String getValue() {
        return value;
    }

    public String formatted() {
        return value.substring(0, 2) + "."
                + value.substring(2, 5) + "."
                + value.substring(5, 8) + "/"
                + value.substring(8, 12) + "-"
                + value.substring(12);
    }

    private static void validate(String cnpj) {
        if (cnpj.length() != 14) {
            throw new IllegalArgumentException("CNPJ must have 14 digits, got: " + cnpj.length());
        }
        if (cnpj.chars().distinct().count() == 1) {
            throw new IllegalArgumentException("CNPJ cannot have all identical digits");
        }

        int firstDigit = calculateDigit(cnpj, WEIGHTS_FIRST, 12);
        if (firstDigit != (cnpj.charAt(12) - '0')) {
            throw new IllegalArgumentException("Invalid CNPJ: wrong first check digit");
        }

        int secondDigit = calculateDigit(cnpj, WEIGHTS_SECOND, 13);
        if (secondDigit != (cnpj.charAt(13) - '0')) {
            throw new IllegalArgumentException("Invalid CNPJ: wrong second check digit");
        }
    }

    private static int calculateDigit(String cnpj, int[] weights, int length) {
        int sum = 0;
        for (int i = 0; i < length; i++) {
            sum += (cnpj.charAt(i) - '0') * weights[i];
        }
        int remainder = sum % 11;
        return remainder < 2 ? 0 : 11 - remainder;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Cnpj other)) return false;
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
