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

import org.keycloak.Config;
import org.keycloak.models.AuthenticationExecutionModel;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.provider.ProviderConfigProperty;

import java.util.Arrays;
import java.util.List;

public class ConditionalCredentialsAuthenticatorFactory implements ConditionalAuthenticatorFactory {

    public static final String PROVIDER_ID = "conditional-credentials";

    public static final String CONF_CREDENTIAL_TYPE = "credential-type";
    public static final String CONF_NOT = "not";

    private static final List<ProviderConfigProperty> CONFIG_PROPERTIES = createConfigProperties();

    private static final AuthenticationExecutionModel.Requirement[] REQUIREMENT_CHOICES = {
            AuthenticationExecutionModel.Requirement.REQUIRED, AuthenticationExecutionModel.Requirement.DISABLED
    };

    @Override
    public void init(Config.Scope config) {
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
        return "Condition - credentials configured";
    }

    @Override
    public boolean isConfigurable() {
        return true;
    }

    @Override
    public AuthenticationExecutionModel.Requirement[] getRequirementChoices() {
        return REQUIREMENT_CHOICES;
    }

    @Override
    public boolean isUserSetupAllowed() {
        return false;
    }

    @Override
    public String getHelpText() {
        return "Flow is executed only if the user has credentials of expected type";
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return CONFIG_PROPERTIES;
    }

    private static List<ProviderConfigProperty> createConfigProperties() {
        ProviderConfigProperty credentialType = new ProviderConfigProperty();
        credentialType.setType(ProviderConfigProperty.STRING_TYPE);
        credentialType.setName(CONF_CREDENTIAL_TYPE);
        credentialType.setLabel("Credential type");
        credentialType.setHelpText("The credential type to check");

        ProviderConfigProperty negateOutput = new ProviderConfigProperty();
        negateOutput.setType(ProviderConfigProperty.BOOLEAN_TYPE);
        negateOutput.setName(CONF_NOT);
        negateOutput.setLabel("Negate output");
        negateOutput.setHelpText("Apply a not to the check result");

        return Arrays.asList(credentialType, negateOutput);
    }

    @Override
    public ConditionalAuthenticator getSingleton() {
        return ConditionalCredentialsAuthenticator.SINGLETON;
    }
}
