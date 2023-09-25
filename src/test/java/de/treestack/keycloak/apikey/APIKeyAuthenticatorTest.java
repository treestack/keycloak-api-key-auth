package de.treestack.keycloak.apikey;

import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.events.EventBuilder;
import org.keycloak.models.AuthenticationExecutionModel;
import org.keycloak.models.UserModel;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Stream;

import static de.treestack.keycloak.apikey.APIKeyAuthenticator.API_KEY_ATTRIBUTE;
import static de.treestack.keycloak.apikey.APIKeyAuthenticator.API_KEY_FORM_FIELD;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

final public class APIKeyAuthenticatorTest {

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private AuthenticationFlowContext ctx;

    @BeforeEach
    public void init() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void apiKeyNotGivenReturnsUnauthorized() {
        var auth = new APIKeyAuthenticator();
        var responseArgument = ArgumentCaptor.forClass(Response.class);
        when(ctx.getEvent()).thenReturn(mock(EventBuilder.class));
        when(ctx.getHttpRequest().getDecodedFormParameters()).thenReturn(createFormData(null));

        auth.authenticate(ctx);

        verify(ctx).failure(eq(AuthenticationFlowError.INVALID_CREDENTIALS), responseArgument.capture());
        assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), responseArgument.getValue().getStatus());
    }

    @Test
    void userNotFoundReturnsUnauthorized() {
        var auth = new APIKeyAuthenticator();
        var responseArgument = ArgumentCaptor.forClass(Response.class);
        when(ctx.getEvent()).thenReturn(mock(EventBuilder.class));
        when(ctx.getHttpRequest().getDecodedFormParameters()).thenReturn(createFormData("myapikey"));
        when(ctx.getSession().users().searchForUserByUserAttributeStream(any(), eq(API_KEY_ATTRIBUTE), eq("myapikey"))).thenReturn(Stream.empty());

        auth.authenticate(ctx);

        verify(ctx).failure(eq(AuthenticationFlowError.INVALID_CREDENTIALS), responseArgument.capture());
        assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), responseArgument.getValue().getStatus());
    }

    @Test
    void successfulAuthSetsUser() {
        var auth = new APIKeyAuthenticator();
        var user = mock(UserModel.class);

        when(ctx.getHttpRequest().getDecodedFormParameters()).thenReturn(createFormData("myapikey"));
        when(ctx.getSession().users().searchForUserByUserAttributeStream(any(), eq(API_KEY_ATTRIBUTE), eq("myapikey"))).thenReturn(Stream.of(user));

        auth.authenticate(ctx);

        verify(ctx).setUser(eq(user));
        verify(ctx).success();
    }

    @Test
    void noUserRequired() {
        var auth = new APIKeyAuthenticator();
        assertFalse(auth.requiresUser(), "no user required");
    }

    @Test
    void noUserConfiguration() {
        var auth = new APIKeyAuthenticator();
        assertFalse(auth.isConfigurable(), "not configurable");
        assertFalse(auth.isUserSetupAllowed(), "user setup not allowed");
        assertTrue(auth.getConfigProperties().isEmpty(), "no configuration properties");
    }

    @Test
    void properDisplayAndHelpText() {
        var auth = new APIKeyAuthenticator();
        assertNotNull(auth.getDisplayType(), "display type");
        assertNotNull(auth.getHelpText(), "help text");
        assertNull(auth.getReferenceCategory(), "no reference category");
    }

    @Test
    void correctProviderId() {
        var auth = new APIKeyAuthenticator();
        assertEquals(APIKeyAuthenticator.PROVIDER_ID, auth.getId());
    }

    @Test
    void correctRequirementChoices() {
        var auth = new APIKeyAuthenticator();
        var requirementList = Arrays.asList(auth.getRequirementChoices());
        assertTrue(requirementList.contains(AuthenticationExecutionModel.Requirement.REQUIRED));
        assertTrue(requirementList.contains(AuthenticationExecutionModel.Requirement.ALTERNATIVE));
        assertTrue(requirementList.contains(AuthenticationExecutionModel.Requirement.DISABLED));
    }

    private static MultivaluedMap<String, String> createFormData(String apiKey) {
        var formData = new MultivaluedHashMap<String, String>();
        formData.put(API_KEY_FORM_FIELD, Collections.singletonList(apiKey));
        return formData;
    }


}

