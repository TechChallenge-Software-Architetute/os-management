package com.os.workshop.domain.vehicle;

import java.util.regex.Pattern;

public final class LicensePlate {

    private static final Pattern OLD_FORMAT = Pattern.compile("[A-Z]{3}[0-9]{4}");
    private static final Pattern MERCOSUL_FORMAT = Pattern.compile("[A-Z]{3}[0-9][A-Z][0-9]{2}");
    private static final Pattern NON_ALPHANUMERIC = Pattern.compile("[^A-Za-z0-9]");

    private final String value;

    public LicensePlate(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new IllegalArgumentException("License plate cannot be null or blank");
        }
        String normalized = NON_ALPHANUMERIC.matcher(raw).replaceAll("").toUpperCase();
        validate(normalized);
        this.value = normalized;
    }

    public String getValue() {
        return value;
    }

    public String formatted() {
        return value.substring(0, 3) + "-" + value.substring(3);
    }

    private static void validate(String plate) {
        if (!OLD_FORMAT.matcher(plate).matches() && !MERCOSUL_FORMAT.matcher(plate).matches()) {
            throw new IllegalArgumentException(
                    "Invalid Brazilian license plate: '" + plate + "'. "
                    + "Expected formats: ABC1234 (old) or ABC1D23 (Mercosul)");
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LicensePlate other)) return false;
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
