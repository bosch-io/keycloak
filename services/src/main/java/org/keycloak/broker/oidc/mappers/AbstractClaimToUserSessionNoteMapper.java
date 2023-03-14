package org.keycloak.broker.oidc.mappers;

import org.keycloak.broker.provider.BrokeredIdentityContext;
import org.keycloak.models.IdentityProviderMapperModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;

import java.util.Map;

import static org.keycloak.broker.oidc.mappers.ClaimToUserSessionNoteMapper.ARE_CLAIM_VALUES_REGEX_PROPERTY_NAME;
import static org.keycloak.utils.RegexUtils.valueMatchesRegex;

public abstract class AbstractClaimToUserSessionNoteMapper extends AbstractClaimMapper {

    public static final String CLAIM_PROPERTY_NAME = "claims";

    @Override
    public void importNewUser(KeycloakSession session, RealmModel realm, UserModel user,
            IdentityProviderMapperModel mapperModel, BrokeredIdentityContext context) {
        addClaimToSessionNote(mapperModel, context);
    }

    @Override
    public void updateBrokeredUser(KeycloakSession session, RealmModel realm, UserModel user,
            IdentityProviderMapperModel mapperModel, BrokeredIdentityContext context) {
        addClaimToSessionNote(mapperModel, context);
    }

    private void addClaimToSessionNote(IdentityProviderMapperModel mapperModel, BrokeredIdentityContext context) {
        Map<String, String> claims = mapperModel.getConfigMap(CLAIM_PROPERTY_NAME);
        boolean areClaimValuesRegex = Boolean.parseBoolean(mapperModel.getConfig().get(ARE_CLAIM_VALUES_REGEX_PROPERTY_NAME));

        for (Map.Entry<String, String> claim : claims.entrySet()) {
            Object value = getClaimValue(context, claim.getKey());

            boolean claimValuesMatch = areClaimValuesRegex ?
                    valueMatchesRegex(claim.getValue(), value) : valueEquals(claim.getValue(), value);
            if (claimValuesMatch) {
                context.getAuthenticationSession().setUserSessionNote(claim.getKey(), claim.getValue());
            }
        }
    }
}
