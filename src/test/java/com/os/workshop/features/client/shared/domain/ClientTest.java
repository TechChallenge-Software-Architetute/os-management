package com.os.workshop.features.client.shared.domain;

import com.os.workshop.domain.client.Client;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ClientTest {

    private static final String VALID_CPF = "52998224725";
    private static final String VALID_NAME = "João Silva";
    private static final String VALID_EMAIL = "joao@email.com";
    private static final String VALID_PHONE = "11999998888";

    @Test
    void whenCreatingClientWithValidData_thenClientIsActiveAndNameIsUpperCase() {
        Client client = Client.create(VALID_NAME, VALID_CPF, VALID_EMAIL, VALID_PHONE);

        assertEquals("JOÃO SILVA", client.getName());
        assertEquals(VALID_CPF, client.getCpf().getValue());
        assertEquals(VALID_EMAIL, client.getEmail());
        assertEquals(VALID_PHONE, client.getPhone());
        assertTrue(client.isActive());
        assertNull(client.getId());
    }

    @Test
    void whenCreatingClientWithNullName_thenThrowsNullPointerException() {
        assertThrows(NullPointerException.class,
                () -> Client.create(null, VALID_CPF, VALID_EMAIL, VALID_PHONE));
    }

    @Test
    void whenCreatingClientWithBlankName_thenThrowsIllegalArgument() {
        assertThrows(IllegalArgumentException.class,
                () -> Client.create("   ", VALID_CPF, VALID_EMAIL, VALID_PHONE));
    }

    @Test
    void whenCreatingClientWithInvalidCpf_thenThrowsIllegalArgument() {
        assertThrows(IllegalArgumentException.class,
                () -> Client.create(VALID_NAME, "00000000000", VALID_EMAIL, VALID_PHONE));
    }

    @Test
    void whenCreatingClientWithNullEmailAndPhone_thenClientIsCreatedSuccessfully() {
        Client client = Client.create(VALID_NAME, VALID_CPF, null, null);

        assertNull(client.getEmail());
        assertNull(client.getPhone());
        assertTrue(client.isActive());
    }

    @Test
    void whenUpdatingClient_thenContactDataIsChanged() {
        Client client = Client.create(VALID_NAME, VALID_CPF, VALID_EMAIL, VALID_PHONE);

        client.update("Maria Souza", "maria@email.com", "11888887777");

        assertEquals("Maria Souza", client.getName());
        assertEquals("maria@email.com", client.getEmail());
        assertEquals("11888887777", client.getPhone());
    }

    @Test
    void whenUpdatingClientWithNullName_thenThrowsNullPointerException() {
        Client client = Client.create(VALID_NAME, VALID_CPF, VALID_EMAIL, VALID_PHONE);

        assertThrows(NullPointerException.class,
                () -> client.update(null, VALID_EMAIL, VALID_PHONE));
    }

    @Test
    void whenUpdatingClientWithBlankName_thenThrowsIllegalArgument() {
        Client client = Client.create(VALID_NAME, VALID_CPF, VALID_EMAIL, VALID_PHONE);

        assertThrows(IllegalArgumentException.class,
                () -> client.update("  ", VALID_EMAIL, VALID_PHONE));
    }

    @Test
    void whenDeactivatingClient_thenClientIsNoLongerActive() {
        Client client = Client.create(VALID_NAME, VALID_CPF, VALID_EMAIL, VALID_PHONE);

        client.deactivate();

        assertFalse(client.isActive());
    }

    @Test
    void whenReconstitutingClient_thenAllFieldsAreRestored() {
        LocalDateTime now = LocalDateTime.now();

        Client client = Client.reconstitute(
                1L, "JOÃO SILVA", VALID_CPF, VALID_EMAIL, VALID_PHONE,
                true, now, now);

        assertEquals(1L, client.getId());
        assertEquals("JOÃO SILVA", client.getName());
        assertEquals(VALID_CPF, client.getCpf().getValue());
        assertEquals(VALID_EMAIL, client.getEmail());
        assertEquals(VALID_PHONE, client.getPhone());
        assertTrue(client.isActive());
        assertEquals(now, client.getCreatedAt());
        assertEquals(now, client.getUpdatedAt());
    }

    @Test
    void whenReconstitutingInactiveClient_thenActiveIsFalse() {
        Client client = Client.reconstitute(
                2L, "MARIA SOUZA", "39053344705", "maria@email.com", "11888887777",
                false, LocalDateTime.now(), LocalDateTime.now());

        assertFalse(client.isActive());
    }
}
