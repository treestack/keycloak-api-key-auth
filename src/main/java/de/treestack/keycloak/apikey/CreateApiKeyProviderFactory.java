package de.treestack.keycloak.apikey;

import io.phasetwo.keycloak.events.UserEventListenerProviderFactory;
import org.keycloak.common.util.SecretGenerator;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;

import java.util.Collections;

public class CreateApiKeyProviderFactory extends UserEventListenerProviderFactory {

    private static final int API_KEY_LENGTH = 16;

    @Override
    public String getId() {
        return "create-api-key";
    }

    @Override
    protected UserChangedHandler getUserChangedHandler() {
        final var secret = SecretGenerator.getInstance();
        return new UserChangedHandler() {
            @Override
            protected void onUserAdded(KeycloakSession session, RealmModel realm, UserModel user) {
                log.info("Generating API key for new user {} in realm {}", user.getUsername(), realm.getName());
                final var apiKey = secret.randomString(API_KEY_LENGTH);
                user.setAttribute(APIKeyAuthenticator.API_KEY_ATTRIBUTE, Collections.singletonList(apiKey));
            }

            @Override
            protected void onUserRemoved(KeycloakSession session, RealmModel realm, UserModel user) {
            }
        };
    }
}
