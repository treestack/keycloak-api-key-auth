package de.treestack.keycloak.apikey.events;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.keycloak.events.EventBuilder;
import org.keycloak.events.EventType;
import org.keycloak.events.admin.AdminEvent;
import org.keycloak.events.admin.OperationType;
import org.keycloak.events.admin.ResourceType;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakTransactionManager;
import org.keycloak.models.RealmModel;
import org.keycloak.models.RealmProvider;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UserAddedEventListenerProviderTest {

    @Mock
    RealmModel mockRealm;

    @Mock
    KeycloakTransactionManager mockTransactionManager;

    @Mock
    RealmProvider mockRealmProvider;

    @Mock
    KeycloakSession mockSession;

    @BeforeEach
    void init() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void enlistsTransactionOnUserRegistration() {
        when(mockSession.getTransactionManager()).thenReturn(mockTransactionManager);
        when(mockSession.realms()).thenReturn(mockRealmProvider);
        when(mockRealmProvider.getRealm(any())).thenReturn(mockRealm);

        var handler = new UserAddedEventListenerProvider(mockSession);

        var event = new EventBuilder(mockRealm, mockSession)
                .event(EventType.REGISTER)
                .getEvent();

        handler.onEvent(event);

        verify(mockTransactionManager).enlistAfterCompletion(any());
    }

    @Test
    void enlistsTransactionOnAdminCreation() {
        var mockRealm = mock(RealmModel.class);
        var mockTransactionManager = mock(KeycloakTransactionManager.class);
        var mockRealmProvider = mock(RealmProvider.class);
        var mockSession = mock(KeycloakSession.class);

        when(mockSession.getTransactionManager()).thenReturn(mockTransactionManager);
        when(mockSession.realms()).thenReturn(mockRealmProvider);
        when(mockRealmProvider.getRealm(any())).thenReturn(mockRealm);

        var handler = new UserAddedEventListenerProvider(mockSession);

        var event = new AdminEvent();
        event.setResourceType(ResourceType.USER);
        event.setOperationType(OperationType.CREATE);
        event.setResourcePath("users/4711");

        handler.onEvent(event, true);

        verify(mockTransactionManager).enlistAfterCompletion(any());
    }

}
