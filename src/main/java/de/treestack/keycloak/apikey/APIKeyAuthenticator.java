package de.treestack.keycloak.apikey;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.core.Response;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.authentication.authenticators.directgrant.AbstractDirectGrantAuthenticator;
import org.keycloak.events.Errors;
import org.keycloak.models.AuthenticationExecutionModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.provider.ProviderConfigProperty;

import java.util.Collections;
import java.util.List;

public class APIKeyAuthenticator extends AbstractDirectGrantAuthenticator {

    public static final String PROVIDER_ID = "direct-grant-validate-api-key";
    public static final String API_KEY_ATTRIBUTE = "api-key";
    public static final String API_KEY_FORM_FIELD = "api_key";

    /**
     * Load API key from request form parameters
     *
     * @param context AuthenticationFlowContext
     * @return api key if found, null otherwise
     */
    @Nullable
    protected String retrieveApiKey(@NotNull AuthenticationFlowContext context) {
        final var inputData = context.getHttpRequest().getDecodedFormParameters();
        return inputData.getFirst(API_KEY_FORM_FIELD);
    }

    /**
     * Load user by api key attribute.
     *
     * @param context AuthenticationFlowContext
     * @param apiKey the api key
     * @return user if found, null otherwise
     */
    @Nullable
    protected UserModel findUserByApiKey(@NotNull AuthenticationFlowContext context, @NotNull String apiKey) {
        return context.getSession().users()
                .searchForUserByUserAttributeStream(context.getRealm(), API_KEY_ATTRIBUTE, apiKey)
                .findAny().orElse(null);
    }

    /**
     * Reads api key from request and looks up user by api key.
     * Will set the user and succeed the flow if authentication was successful
     * or fail with 401 UNAUTHORIZED if the api key form field is missing or if
     * no user was found for the given api key.
     *
     * @param context AuthenticationFlowContext
     */
    public void authenticate(AuthenticationFlowContext context) {
        final var apiKey = retrieveApiKey(context);
        if (apiKey == null) {
            context.getEvent().error(Errors.INVALID_USER_CREDENTIALS);
            Response challengeResponse = errorResponse(Response.Status.UNAUTHORIZED.getStatusCode(), "invalid_request", "Missing parameter: " + API_KEY_FORM_FIELD);
            context.failure(AuthenticationFlowError.INVALID_CREDENTIALS, challengeResponse);
            return;
        }

        final var user = findUserByApiKey(context, apiKey);
        if (user == null) {
            context.getEvent().error(Errors.INVALID_USER_CREDENTIALS);
            Response challengeResponse = errorResponse(Response.Status.UNAUTHORIZED.getStatusCode(), "invalid_request", "Invalid api key");
            context.failure(AuthenticationFlowError.INVALID_CREDENTIALS, challengeResponse);
            return;
        }

        context.setUser(user);
        context.success();
    }

    @Override
    public boolean requiresUser() {
        return false;
    }

    @Override
    public boolean configuredFor(KeycloakSession session, RealmModel realm, UserModel user) {
        return true;
    }

    @Override
    public void setRequiredActions(KeycloakSession session, RealmModel realm, UserModel user) {
    }

    @Override
    public boolean isUserSetupAllowed() {
        return false;
    }


    @Override
    public String getDisplayType() {
        return "API-Key";
    }

    @Override
    public String getReferenceCategory() {
        return null;
    }

    @Override
    public boolean isConfigurable() {
        return true;
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return Collections.emptyList();
    }

    @Override
    public AuthenticationExecutionModel.Requirement[] getRequirementChoices() {
        return REQUIREMENT_CHOICES;
    }

    @Override
    public String getHelpText() {
        return "Validates the API-Key supplied as a '" + API_KEY_FORM_FIELD + "' form parameter in direct grant request";
    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }
}