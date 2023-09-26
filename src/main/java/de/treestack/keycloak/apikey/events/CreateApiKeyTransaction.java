package de.treestack.keycloak.apikey.events;

import de.treestack.keycloak.apikey.auth.APIKeyAuthenticator;
import org.keycloak.common.util.SecretGenerator;
import org.keycloak.models.*;
import org.keycloak.models.utils.KeycloakModelUtils;

public class CreateApiKeyTransaction extends AbstractKeycloakTransaction {
    private static final int API_KEY_LENGTH = 15;
    private final RealmModel realm;
    private final String userId;
    private final KeycloakSessionFactory factory;

    public CreateApiKeyTransaction(KeycloakSessionFactory factory, RealmModel realm, String userId) {
        this.factory = factory;
        this.realm = realm;
        this.userId = userId;
    }

    @Override
    protected void commitImpl() {
        KeycloakModelUtils.runJobInTransaction(factory, generateApiKeyTask(realm, userId));
    }

    protected KeycloakSessionTask generateApiKeyTask(RealmModel realm, String userId) {
        final var apiKey = SecretGenerator.getInstance().randomString(API_KEY_LENGTH);
        return session -> {
            UserModel user = session.users().getUserById(realm, userId);
            user.setSingleAttribute(APIKeyAuthenticator.API_KEY_ATTRIBUTE, apiKey);
        };
    }

    @Override
    protected void rollbackImpl() {
        // Intentionally left blank
    }
}
