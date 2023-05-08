/*
 * Copyright 2022 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.keycloak.authentication.authenticators.conditional;

import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.authentication.AuthenticationFlowException;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;

import java.util.Map;

public class ConditionalCredentialsAuthenticator implements ConditionalAuthenticator {

    static final ConditionalCredentialsAuthenticator SINGLETON = new ConditionalCredentialsAuthenticator();

    @Override
    public boolean matchCondition(AuthenticationFlowContext context) {
        Map<String, String> config = context.getAuthenticatorConfig().getConfig();
        String credentialType = config.get(ConditionalCredentialsAuthenticatorFactory.CONF_CREDENTIAL_TYPE);
        boolean negateOutput = Boolean.parseBoolean(config.get(ConditionalCredentialsAuthenticatorFactory.CONF_NOT));

        UserModel user = context.getUser();
        if (user == null) {
            throw new AuthenticationFlowException("Cannot find user for checking credential types. Authenticator: " + ConditionalCredentialsAuthenticatorFactory.PROVIDER_ID, AuthenticationFlowError.UNKNOWN_USER);
        }

        boolean result = user.credentialManager().isConfiguredFor(credentialType);
        return negateOutput != result;
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
