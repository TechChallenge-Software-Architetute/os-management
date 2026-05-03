package com.os.workshop.features.vehicle.domain.valueobject;

import java.util.regex.Pattern;

/**
 * Value object que representa uma placa veicular brasileira.
 *
 * <p>Suporta dois formatos oficiais:
 * <ul>
 *   <li><b>Padrão antigo</b>: {@code ABC1234} — 3 letras seguidas de 4 dígitos</li>
 *   <li><b>Padrão Mercosul</b>: {@code ABC1D23} — 3 letras, 1 dígito, 1 letra, 2 dígitos</li>
 * </ul>
 *
 * <p>A normalização remove separadores (hífen) e converte para maiúsculas,
 * permitindo entradas como {@code ABC-1234} ou {@code abc1234}.
 *
 * <p>Imutável por design: uma vez criado com uma placa válida, o valor nunca muda.
 */
public final class LicensePlate {

    private static final Pattern OLD_FORMAT = Pattern.compile("[A-Z]{3}[0-9]{4}");
    private static final Pattern MERCOSUL_FORMAT = Pattern.compile("[A-Z]{3}[0-9][A-Z][0-9]{2}");
    private static final Pattern NON_ALPHANUMERIC = Pattern.compile("[^A-Za-z0-9]");

    private final String value;

    /**
     * Cria uma placa a partir de uma string bruta.
     *
     * @param raw placa no formato antigo ({@code ABC-1234}) ou Mercosul ({@code ABC1D23}),
     *            com ou sem hífen, em qualquer capitalização
     * @throws IllegalArgumentException se a placa for nula, vazia ou não corresponder a nenhum
     *                                  formato válido brasileiro
     */
    public LicensePlate(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new IllegalArgumentException("License plate cannot be null or blank");
        }
        String normalized = NON_ALPHANUMERIC.matcher(raw).replaceAll("").toUpperCase();
        validate(normalized);
        this.value = normalized;
    }

    /**
     * Retorna a placa sem separadores (7 caracteres), conforme armazenado no banco.
     * Exemplo: {@code "ABC1234"} ou {@code "ABC1D23"}.
     */
    public String getValue() {
        return value;
    }

    /**
     * Retorna a placa no formato com hífen para exibição.
     * Exemplo: {@code "ABC-1234"} ou {@code "ABC-1D23"}.
     */
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
