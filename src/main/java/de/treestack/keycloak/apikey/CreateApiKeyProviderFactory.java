package de.treestack.keycloak.apikey;

import io.phasetwo.keycloak.events.UserEventListenerProviderFactory;

public class CreateApiKeyProviderFactory extends UserEventListenerProviderFactory {

    public static final String PROVIDER_ID = "create-api-key";

    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    @Override
    protected UserChangedHandler getUserChangedHandler() {
        return new GenerateApiKeyUserChangedHandler();
    }
}
