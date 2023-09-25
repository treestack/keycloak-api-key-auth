package io.phasetwo.keycloak.events;

import org.keycloak.events.Event;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.EventType;
import org.keycloak.events.admin.AdminEvent;
import org.keycloak.events.admin.OperationType;
import org.keycloak.events.admin.ResourceType;
import org.keycloak.models.*;
import org.keycloak.models.utils.KeycloakModelUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * User added/removed listener base class. Just provide a user add/remove handler. Inspired by
 * zonaut's work:
 * https://keycloak.discourse.group/t/created-user-not-immediately-available-on-event/1476
 */
public abstract class UserEventListenerProviderFactory
        extends AbstractEventListenerProviderFactory {

    protected static final Logger log = LoggerFactory.getLogger(UserEventListenerProviderFactory.class);

    private KeycloakSessionFactory factory;

    @Override
    public EventListenerProvider create(KeycloakSession session) {
        return new AbstractEventListenerProvider() {
            @Override
            public void onEvent(Event event) {
                if (EventType.REGISTER.equals(event.getType())) {
                    userAdded(event.getRealmId(), event.getUserId());
                }
            }

            @Override
            public void onEvent(AdminEvent adminEvent, boolean b) {
                if (ResourceType.USER.equals(adminEvent.getResourceType())
                        && OperationType.CREATE.equals(adminEvent.getOperationType())) {
                    String resourcePath = adminEvent.getResourcePath();
                    if (resourcePath.startsWith("users/")) {
                        userAdded(adminEvent.getRealmId(), resourcePath.substring("users/".length()));
                    } else {
                        log.warn("AdminEvent was CREATE:USER without appropriate resourcePath={}", resourcePath);
                    }
                }
            }

            void userAdded(String realmId, String userId) {
                session
                        .getTransactionManager()
                        .enlistAfterCompletion(
                                new AbstractKeycloakTransaction() {
                                    @Override
                                    protected void commitImpl() {
                                        KeycloakModelUtils.runJobInTransaction(
                                                factory,
                                                (s) -> {
                                                    RealmModel realm = s.realms().getRealm(realmId);
                                                    UserModel user = s.users().getUserById(realm, userId);
                                                    getUserChangedHandler().onUserAdded(s, realm, user);
                                                });
                                    }

                                    @Override
                                    protected void rollbackImpl() {
                                    }
                                });
            }
        };
    }

    abstract protected UserChangedHandler getUserChangedHandler();

    @Override
    public void postInit(KeycloakSessionFactory factory) {
        this.factory = factory;
        factory.register(
                (event) -> {
                    if (event instanceof UserModel.UserRemovedEvent removal) {
                        getUserChangedHandler()
                                .onUserRemoved(removal.getKeycloakSession(), removal.getRealm(), removal.getUser());
                    }
                });
    }

    public interface UserChangedHandler {
        void onUserAdded(KeycloakSession session, RealmModel realm, UserModel user);

        void onUserRemoved(KeycloakSession session, RealmModel realm, UserModel user);
    }
}