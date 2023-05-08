package org.keycloak.authentication.authenticators.conditional;

import org.keycloak.Config.Scope;
import org.keycloak.models.AuthenticationExecutionModel.Requirement;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.provider.ProviderConfigProperty;

import java.util.Arrays;
import java.util.List;

public class ConditionalRequestParamAuthenticatorFactory implements ConditionalAuthenticatorFactory {
    public static final String PROVIDER_ID = "conditional-req-param";

    public static final String CONDITIONAL_REQUEST_PARAM_NAME = "condReqParam";
    public static final String CONDITIONAL_REQUEST_PARAM_VALUE = "condReqValue";
    public static final String CONF_NEGATE = "negate";

    @Override
    public void init(Scope config) {
        // no-op
    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {
        // no-op
    }

    @Override
    public void close() {
        // no-op
    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    @Override
    public String getDisplayType() {
        return "Condition - request parameter";
    }

    @Override
    public boolean isConfigurable() {
        return true;
    }

    private static final Requirement[] REQUIREMENT_CHOICES = {
            Requirement.REQUIRED,
            Requirement.DISABLED
    };

    @Override
    public Requirement[] getRequirementChoices() {
        return REQUIREMENT_CHOICES;
    }

    @Override
    public boolean isUserSetupAllowed() {
        return false;
    }

    @Override
    public String getHelpText() {
        return "Flow is executed only if the login has been initiated with a given request parameter.";
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        ProviderConfigProperty name = new ProviderConfigProperty();
        name.setType(ProviderConfigProperty.STRING_TYPE);
        name.setName(CONDITIONAL_REQUEST_PARAM_NAME);
        name.setLabel("Request parameter name");
        name.setHelpText("The name of the parameter to be checked.");

        ProviderConfigProperty value = new ProviderConfigProperty();
        value.setType(ProviderConfigProperty.STRING_TYPE);
        value.setName(CONDITIONAL_REQUEST_PARAM_VALUE);
        value.setLabel("Request parameter value");
        value.setHelpText("The expected value of the parameter to be checked.");

        ProviderConfigProperty negateOutput = new ProviderConfigProperty();
        negateOutput.setType(ProviderConfigProperty.BOOLEAN_TYPE);
        negateOutput.setName(CONF_NEGATE);
        negateOutput.setLabel("Negate output");
        negateOutput.setHelpText("Apply a NOT to the check result.");

        return Arrays.asList(name, value, negateOutput);
    }

    @Override
    public ConditionalAuthenticator getSingleton() {
        return ConditionalRequestParamAuthenticator.SINGLETON;
    }
}
