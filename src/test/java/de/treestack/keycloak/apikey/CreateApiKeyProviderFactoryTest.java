package de.treestack.keycloak.apikey;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class CreateApiKeyProviderFactoryTest {

    @Test
    public void correctProviderId() {
        var factory = new CreateApiKeyProviderFactory();
        assertEquals(CreateApiKeyProviderFactory.PROVIDER_ID, factory.getId());
    }

    @Test
    public void createsEventHandler() {
        var factory = new CreateApiKeyProviderFactory();
        assertNotNull(factory.getUserChangedHandler());
    }
}
