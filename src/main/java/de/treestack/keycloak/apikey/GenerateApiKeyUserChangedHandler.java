package de.treestack.keycloak.apikey;

import io.phasetwo.keycloak.events.UserEventListenerProviderFactory;
import org.keycloak.common.util.SecretGenerator;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;

public class GenerateApiKeyUserChangedHandler implements UserEventListenerProviderFactory.UserChangedHandler {

    protected static final int API_KEY_LENGTH = 16;

    protected static final Logger log = LoggerFactory.getLogger(GenerateApiKeyUserChangedHandler.class);

    @Override
    public void onUserAdded(KeycloakSession session, RealmModel realm, UserModel user) {
        log.info("Generating API key for new user {} in realm {}", user.getUsername(), realm.getName());
        final var apiKey = SecretGenerator.getInstance().randomString(API_KEY_LENGTH);
        user.setSingleAttribute(APIKeyAuthenticator.API_KEY_ATTRIBUTE, apiKey);
    }

    @Override
    public void onUserRemoved(KeycloakSession session, RealmModel realm, UserModel user) {
        // Intentionally left blank
    }
}
