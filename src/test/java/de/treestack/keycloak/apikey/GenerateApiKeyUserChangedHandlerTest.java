package de.treestack.keycloak.apikey;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.keycloak.common.util.SecretGenerator;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class GenerateApiKeyUserChangedHandlerTest {

    @Test
    public void generatesApiKeyOnUserAdded() {
        var handler = new GenerateApiKeyUserChangedHandler();
        var mockUser = mock(UserModel.class);
        var mockRealm = mock(RealmModel.class);
        var keyCaptor = ArgumentCaptor.forClass(String.class);

        handler.onUserAdded(
                mock(KeycloakSession.class),
                mockRealm,
                mockUser
        );

        verify(mockUser, atLeastOnce()).setSingleAttribute(eq(APIKeyAuthenticator.API_KEY_ATTRIBUTE), keyCaptor.capture());

        var value = keyCaptor.getValue();
        assertEquals(GenerateApiKeyUserChangedHandler.API_KEY_LENGTH, value.length());
    }

    @Test
    public void doesNothingOnUserRemoved() {
        var handler = new GenerateApiKeyUserChangedHandler();
        var mockSession = mock(KeycloakSession.class);
        var mockUser = mock(UserModel.class);
        var mockRealm = mock(RealmModel.class);

        handler.onUserRemoved(
                mockSession,
                mockRealm,
                mockUser
        );

        verifyNoInteractions(mockSession);
        verifyNoInteractions(mockRealm);
        verifyNoInteractions(mockUser);
    }

}
