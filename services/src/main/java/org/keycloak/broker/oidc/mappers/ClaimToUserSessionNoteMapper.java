package org.keycloak.broker.oidc.mappers;

import org.keycloak.broker.oidc.KeycloakOIDCIdentityProviderFactory;
import org.keycloak.broker.oidc.OIDCIdentityProviderFactory;
import org.keycloak.provider.ProviderConfigProperty;

import java.util.ArrayList;
import java.util.List;

public class ClaimToUserSessionNoteMapper extends AbstractClaimToUserSessionNoteMapper {

    static final String ARE_CLAIM_VALUES_REGEX_PROPERTY_NAME = "are.claim.values.regex";
    static final String[] COMPATIBLE_PROVIDERS = {KeycloakOIDCIdentityProviderFactory.PROVIDER_ID,
            OIDCIdentityProviderFactory.PROVIDER_ID};

    private static final List<ProviderConfigProperty> configProperties = new ArrayList<>();

    static {
        ProviderConfigProperty claimsProperty = new ProviderConfigProperty();
        claimsProperty.setName(CLAIM_PROPERTY_NAME);
        claimsProperty.setLabel("Claims");
        claimsProperty.setHelpText("Name and value of the claims to search for in token. You can reference nested claims using a '.', i.e. 'address.locality'. To use dot (.) literally, escape it with backslash (\\.)");
        claimsProperty.setType(ProviderConfigProperty.MAP_TYPE);
        configProperties.add(claimsProperty);
        ProviderConfigProperty isClaimValueRegexProperty = new ProviderConfigProperty();
        isClaimValueRegexProperty.setName(ARE_CLAIM_VALUES_REGEX_PROPERTY_NAME);
        isClaimValueRegexProperty.setLabel("Regex Claim Values");
        isClaimValueRegexProperty.setHelpText("If enabled, claim values are interpreted as regular expressions.");
        isClaimValueRegexProperty.setType(ProviderConfigProperty.BOOLEAN_TYPE);
        configProperties.add(isClaimValueRegexProperty);
    }

    public static final String PROVIDER_ID = "oidc-user-session-note-idp-mapper";

    @Override public String[] getCompatibleProviders() {
        return COMPATIBLE_PROVIDERS;
    }

    @Override public String getDisplayCategory() {
        return "User Session";
    }

    @Override public String getDisplayType() {
        return "User Session Note Mapper";
    }

    @Override public String getHelpText() {
        return "Add every matching claim to the user session note.";
    }

    @Override public List<ProviderConfigProperty> getConfigProperties() {
        return configProperties;
    }

    @Override public String getId() {
        return PROVIDER_ID;
    }
}
