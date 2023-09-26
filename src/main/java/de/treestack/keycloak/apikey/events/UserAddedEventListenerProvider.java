package de.treestack.keycloak.apikey.events;

import org.keycloak.events.Event;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.EventType;
import org.keycloak.events.admin.AdminEvent;
import org.keycloak.events.admin.OperationType;
import org.keycloak.events.admin.ResourceType;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;

public class UserAddedEventListenerProvider implements EventListenerProvider {
    private final KeycloakSession session;

    public UserAddedEventListenerProvider(KeycloakSession session) {
        this.session = session;
    }

    /**
     * Handle user self-registration
     * {@inheritDoc}
     */
    @Override
    public void onEvent(Event event) {
        if (EventType.REGISTER.equals(event.getType())) {
            userAdded(event.getRealmId(), event.getUserId());
        }
    }

    /**
     * Handle user creation by admin
     * {@inheritDoc}
     */
    @Override
    public void onEvent(AdminEvent adminEvent, boolean b) {
        if (ResourceType.USER.equals(adminEvent.getResourceType())
                && OperationType.CREATE.equals(adminEvent.getOperationType())) {
            String resourcePath = adminEvent.getResourcePath();
            if (resourcePath.startsWith("users/")) {
                userAdded(adminEvent.getRealmId(), resourcePath.substring("users/".length()));
            }
        }
    }

    protected void userAdded(String realmId, String userId) {
        RealmModel realm = session.realms().getRealm(realmId);
        session.getTransactionManager().enlistAfterCompletion(
                new CreateApiKeyTransaction(session.getKeycloakSessionFactory(), realm, userId)
        );
    }

    @Override
    public void close() {
        // Intentionally left blank
    }
}

