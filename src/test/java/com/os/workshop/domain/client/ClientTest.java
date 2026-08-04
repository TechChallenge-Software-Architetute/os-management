package com.os.workshop.domain.client;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ClientTest {

    // Valid CPF for test purposes: 529.982.247-25
    private static final String VALID_CPF = "52998224725";

    // ==================== create ====================

    @Test
    void create_withValidData_returnsActiveClient() {
        Client client = Client.create("João Silva", VALID_CPF, "joao@email.com", "11999999999");

        assertEquals("JOÃO SILVA", client.getName());
        assertEquals("joao@email.com", client.getEmail());
        assertEquals("11999999999", client.getPhone());
        assertTrue(client.isActive());
    }

    @Test
    void create_withNullName_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () ->
                Client.create(null, VALID_CPF, "joao@email.com", "11999999999"));
    }

    @Test
    void create_withBlankName_throwsIllegalArgument() {
        assertThrows(IllegalArgumentException.class, () ->
                Client.create("   ", VALID_CPF, "joao@email.com", "11999999999"));
    }

    @Test
    void create_stripsAndUppercasesName() {
        Client client = Client.create("  maria souza  ", VALID_CPF, "maria@email.com", "11888888888");

        assertEquals("MARIA SOUZA", client.getName());
    }

    @Test
    void create_withInvalidCpf_throwsIllegalArgument() {
        assertThrows(IllegalArgumentException.class, () ->
                Client.create("João Silva", "12345678901", "joao@email.com", "11999999999"));
    }

    // ==================== update ====================

    @Test
    void update_withValidData_updatesFields() {
        Client client = Client.create("João Silva", VALID_CPF, "joao@email.com", "11999999999");

        client.update("Maria Souza", "maria@email.com", "11888888888");

        assertEquals("Maria Souza", client.getName());
        assertEquals("maria@email.com", client.getEmail());
        assertEquals("11888888888", client.getPhone());
    }

    @Test
    void update_withNullName_throwsNullPointerException() {
        Client client = Client.create("João Silva", VALID_CPF, "joao@email.com", "11999999999");

        assertThrows(NullPointerException.class, () ->
                client.update(null, "maria@email.com", "11888888888"));
    }

    @Test
    void update_withBlankName_throwsIllegalArgument() {
        Client client = Client.create("João Silva", VALID_CPF, "joao@email.com", "11999999999");

        assertThrows(IllegalArgumentException.class, () ->
                client.update("  ", "maria@email.com", "11888888888"));
    }

    @Test
    void update_stripsName() {
        Client client = Client.create("João Silva", VALID_CPF, "joao@email.com", "11999999999");

        client.update("  Pedro Santos  ", "pedro@email.com", "11777777777");

        assertEquals("Pedro Santos", client.getName());
    }

    // ==================== deactivate ====================

    @Test
    void deactivate_setsActiveToFalse() {
        Client client = Client.create("João Silva", VALID_CPF, "joao@email.com", "11999999999");
        assertTrue(client.isActive());

        client.deactivate();

        assertFalse(client.isActive());
    }

    @Test
    void deactivate_canBeCalledMultipleTimes() {
        Client client = Client.create("João Silva", VALID_CPF, "joao@email.com", "11999999999");

        client.deactivate();
        client.deactivate();

        assertFalse(client.isActive());
    }
}
