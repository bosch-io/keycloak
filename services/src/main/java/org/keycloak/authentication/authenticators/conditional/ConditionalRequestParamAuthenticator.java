package org.keycloak.authentication.authenticators.conditional;

import org.jboss.logging.Logger;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.models.AuthenticatorConfigModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.utils.ClientRequestParamUtil;
import org.keycloak.utils.StringUtil;

import java.util.Map;
import java.util.Objects;

public class ConditionalRequestParamAuthenticator implements ConditionalAuthenticator {

    public static final ConditionalRequestParamAuthenticator SINGLETON = new ConditionalRequestParamAuthenticator();

    private static final Logger logger = Logger.getLogger(ConditionalRequestParamAuthenticator.class);

    @Override
    public boolean matchCondition(AuthenticationFlowContext context) {
        AuthenticatorConfigModel authenticatorConfig = context.getAuthenticatorConfig();
        if (authenticatorConfig == null) {
            return false;
        }

        Map<String, String> config = authenticatorConfig.getConfig();
        if (config == null) {
            return false;
        }

        String configuredName = config.get(ConditionalRequestParamAuthenticatorFactory.CONDITIONAL_REQUEST_PARAM_NAME);
        if (StringUtil.isBlank(configuredName)) {
            logger.errorv("Configured value is blank for property: {0}",
                    ConditionalRequestParamAuthenticatorFactory.CONDITIONAL_REQUEST_PARAM_NAME);
            return false;
        }
        String configuredValue =
                config.get(ConditionalRequestParamAuthenticatorFactory.CONDITIONAL_REQUEST_PARAM_VALUE);
        if (StringUtil.isBlank(configuredValue)) {
            logger.errorv("Configured value is blank for property: {0}",
                    ConditionalRequestParamAuthenticatorFactory.CONDITIONAL_REQUEST_PARAM_VALUE);
            return false;
        }

        String requestedValue = ClientRequestParamUtil.getClientRequestParam(configuredName, context.getSession(),
                context.getAuthenticationSession());
        boolean paramMatches = Objects.equals(configuredValue, requestedValue);

        boolean negateOutput =
                Boolean.parseBoolean(config.get(ConditionalRequestParamAuthenticatorFactory.CONF_NEGATE));

        return negateOutput != paramMatches;
    }

    @Override
    public void action(AuthenticationFlowContext context) {
        // Not used
    }

    @Override
    public boolean requiresUser() {
        return true;
    }

    @Override
    public void setRequiredActions(KeycloakSession session, RealmModel realm, UserModel user) {
        // Not used
    }

    @Override
    public void close() {
        // Does nothing
    }
}
