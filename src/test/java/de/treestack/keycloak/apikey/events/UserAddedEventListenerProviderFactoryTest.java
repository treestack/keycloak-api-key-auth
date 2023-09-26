package de.treestack.keycloak.apikey.events;

import org.junit.jupiter.api.Test;
import org.keycloak.models.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

class UserAddedEventListenerProviderFactoryTest {

    @Test
    void correctProviderId() {
        var factory = new UserAddedEventListenerProviderFactory();
        assertEquals(UserAddedEventListenerProviderFactory.PROVIDER_ID, factory.getId());
    }

    @Test
    void createsEventListenerProvider() {
        var factory = new UserAddedEventListenerProviderFactory();
        assertNotNull(factory.create(mock(KeycloakSession.class)));
    }

}
