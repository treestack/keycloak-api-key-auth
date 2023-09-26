package de.treestack.keycloak.apikey.events;

import org.keycloak.Config;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.EventListenerProviderFactory;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;

public class UserAddedEventListenerProviderFactory implements EventListenerProviderFactory {

    public static final String PROVIDER_ID = "create-api-key";

    @Override
    public EventListenerProvider create(KeycloakSession session) {
        return new UserAddedEventListenerProvider(session);
    }

    @Override
    public void init(Config.Scope config) {
        // Intentionally left blank
    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {
        // Intentionally left blank
    }

    @Override
    public void close() {
        // Intentionally left blank
    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }
}