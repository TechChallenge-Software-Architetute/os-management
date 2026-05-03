package com.os.workshop.features.client.domain.valueobject;

import java.util.regex.Pattern;

/**
 * Value object que representa um CPF brasileiro (Cadastro de Pessoas Físicas).
 *
 * <p>Responsabilidades deste value object:
 * <ul>
 *   <li>Normalizar a entrada removendo pontuação (aceita "123.456.789-09" ou "12345678909")</li>
 *   <li>Validar o comprimento (11 dígitos)</li>
 *   <li>Rejeitar CPFs com todos os dígitos iguais (ex: "111.111.111-11")</li>
 *   <li>Verificar os dois dígitos verificadores pelo algoritmo oficial da Receita Federal</li>
 * </ul>
 *
 * <p>Imutável por design: uma vez criado com um CPF válido, o valor nunca muda.
 */
public final class Cpf {

    private static final Pattern NON_DIGIT = Pattern.compile("[^0-9]");

    private final String value;

    /**
     * Cria um CPF a partir de uma string bruta, com ou sem formatação.
     *
     * @param raw CPF no formato "XXX.XXX.XXX-XX" ou "XXXXXXXXXXX"
     * @throws IllegalArgumentException se o CPF for nulo, tiver comprimento inválido ou
     *                                  não passar na validação dos dígitos verificadores
     */
    public Cpf(String raw) {
        if (raw == null) {
            throw new IllegalArgumentException("CPF cannot be null");
        }
        String normalized = NON_DIGIT.matcher(raw).replaceAll("");
        validate(normalized);
        this.value = normalized;
    }

    /**
     * Retorna o CPF sem formatação (apenas 11 dígitos).
     * Este é o valor armazenado em banco de dados.
     */
    public String getValue() {
        return value;
    }

    /**
     * Retorna o CPF no formato oficial brasileiro: XXX.XXX.XXX-XX.
     * Útil para exibição em respostas da API.
     */
    public String formatted() {
        return value.substring(0, 3) + "."
             + value.substring(3, 6) + "."
             + value.substring(6, 9) + "-"
             + value.substring(9);
    }

    /**
     * Algoritmo de validação da Receita Federal para os dois dígitos verificadores.
     * Rejeita sequências uniformes (ex: "000.000.000-00") mesmo que matematicamente válidas.
     */
    private static void validate(String cpf) {
        if (cpf.length() != 11) {
            throw new IllegalArgumentException("CPF must have 11 digits, got: " + cpf.length());
        }
        if (cpf.chars().distinct().count() == 1) {
            throw new IllegalArgumentException("CPF cannot have all identical digits");
        }

        // Primeiro dígito verificador
        int sum = 0;
        for (int i = 0; i < 9; i++) {
            sum += (cpf.charAt(i) - '0') * (10 - i);
        }
        int firstDigit = 11 - (sum % 11);
        if (firstDigit >= 10) firstDigit = 0;
        if (firstDigit != (cpf.charAt(9) - '0')) {
            throw new IllegalArgumentException("Invalid CPF: wrong first check digit");
        }

        // Segundo dígito verificador
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
