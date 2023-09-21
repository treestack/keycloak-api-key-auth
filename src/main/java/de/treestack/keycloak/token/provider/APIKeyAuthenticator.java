package de.treestack.keycloak.token.provider;

import jakarta.ws.rs.core.MultivaluedMap;
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

import java.util.ArrayList;
import java.util.List;

public class APIKeyAuthenticator extends AbstractDirectGrantAuthenticator {

    public static final String PROVIDER_ID = "direct-grant-validate-api-key";
    public static final String ATTRIBUTE_API_KEY_ATTRIBUTE = "attribute.apikey.attribute";
    public static final String API_KEY_FORM_FIELD = "api_key";
    private static final List<ProviderConfigProperty> configProperties = new ArrayList<>();

    protected String retrieveApiKey(AuthenticationFlowContext context) {
        MultivaluedMap<String, String> inputData = context.getHttpRequest().getDecodedFormParameters();
        return inputData.getFirst(API_KEY_FORM_FIELD);
    }

    protected UserModel findUserByApiKey(AuthenticationFlowContext context, String apiKey) {
        String userAttribute = context.getAuthenticatorConfig().getConfig().get(ATTRIBUTE_API_KEY_ATTRIBUTE);
        UserModel user = context.getSession().users()
                .searchForUserByUserAttributeStream(context.getRealm(), userAttribute, apiKey)
                .findAny().orElse(null);
        return user;
    }

    static {
        ProviderConfigProperty property;
        property = new ProviderConfigProperty();
        property.setName(ATTRIBUTE_API_KEY_ATTRIBUTE);
        property.setLabel("User attribute");
        property.setHelpText("User attribute that contains the API key");
        property.setDefaultValue("api-key");
        property.setType(ProviderConfigProperty.STRING_TYPE);
        configProperties.add(property);
    }

    @Override
    public void authenticate(AuthenticationFlowContext context) {
        var apiKey = retrieveApiKey(context);
        if (apiKey == null) {
            context.getEvent().error(Errors.INVALID_USER_CREDENTIALS);
            Response challengeResponse = errorResponse(Response.Status.UNAUTHORIZED.getStatusCode(), "invalid_request", "Missing parameter: " + API_KEY_FORM_FIELD);
            context.failure(AuthenticationFlowError.INVALID_CREDENTIALS, challengeResponse);
            return;
        }

        UserModel user = findUserByApiKey(context, apiKey);
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
        return configProperties;
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