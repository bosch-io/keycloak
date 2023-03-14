package org.keycloak.testsuite.broker;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.keycloak.testsuite.broker.OidcClaimToUserSessionNoteMapperTest.CLAIM;
import static org.keycloak.testsuite.broker.OidcClaimToUserSessionNoteMapperTest.CLAIM_VALUE;

import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Test;
import org.keycloak.admin.client.CreatedResponseUtil;
import org.keycloak.admin.client.resource.IdentityProviderResource;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.jose.jws.JWSInput;
import org.keycloak.jose.jws.JWSInputException;
import org.keycloak.models.IdentityProviderMapperSyncMode;
import org.keycloak.protocol.oidc.OIDCLoginProtocol;
import org.keycloak.representations.AccessToken;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.IdentityProviderMapperRepresentation;
import org.keycloak.representations.idm.ProtocolMapperRepresentation;
import org.keycloak.testsuite.util.OAuthClient;

import java.util.Collections;
import java.util.Map;

public abstract class AbstractUserSessionNoteMapperTest extends AbstractIdentityProviderMapperTest {

    protected abstract void createMapperInIdp(IdentityProviderMapperSyncMode syncMode);

    ClientRepresentation consumerClientRep;

    @Before
    public void setup() {
        RealmResource providerRealm = adminClient.realm(bc.providerRealmName());
        RealmResource consumerRealm = adminClient.realm(bc.consumerRealmName());

        ProtocolMapperRepresentation providerHardcodedClaimMapper = new ProtocolMapperRepresentation();
        providerHardcodedClaimMapper.setName("Hardcoded Claim");
        providerHardcodedClaimMapper.setProtocol(OIDCLoginProtocol.LOGIN_PROTOCOL);
        providerHardcodedClaimMapper.setProtocolMapper("oidc-hardcoded-claim-mapper");
        providerHardcodedClaimMapper.setConfig(Map.of("claim.name", CLAIM,
                "claim.value", CLAIM_VALUE, "access.token.claim", "true"));

        ProtocolMapperRepresentation consumerSessionNoteToClaimMapper = new ProtocolMapperRepresentation();
        consumerSessionNoteToClaimMapper.setName("Session Note To Claim");
        consumerSessionNoteToClaimMapper.setProtocol(OIDCLoginProtocol.LOGIN_PROTOCOL);
        consumerSessionNoteToClaimMapper.setProtocolMapper("oidc-usersessionmodel-note-mapper");
        consumerSessionNoteToClaimMapper.setConfig(Map.of("user.session.note", CLAIM, "claim.name", CLAIM,
                "access.token.claim", "true"));

        ClientRepresentation providerClientRep = providerRealm.clients().findByClientId("brokerapp").get(0);
        consumerClientRep = consumerRealm.clients().findByClientId("broker-app").get(0);

        setupIdentityProvider();
        createMapperInIdp(IdentityProviderMapperSyncMode.FORCE);
        createUserInProviderRealm(Map.of(
                "firstName", Collections.singletonList("FIRST NAME"),
                "lastName", Collections.singletonList("LAST NAME"),
                "email", Collections.singletonList("EMAIL")));

        providerRealm.clients().get(providerClientRep.getId()).getProtocolMappers()
                .createMapper(providerHardcodedClaimMapper);
        consumerRealm.clients().get(consumerClientRep.getId()).getProtocolMappers()
                .createMapper(consumerSessionNoteToClaimMapper);
    }

    @Test
    public void userSessionNoteContainsClaim() {
        OAuthClient.AuthorizationEndpointResponse authzResponse = oauth.realm(bc.consumerRealmName())
                .clientId("broker-app")
                .redirectUri(getAuthServerRoot() + "realms/" + bc.consumerRealmName() + "/account")
                .doLoginSocial(bc.getIDPAlias(), bc.getUserLogin(), bc.getUserPassword());

        String code = authzResponse.getCode();
        OAuthClient.AccessTokenResponse response = oauth.doAccessTokenRequest(code, consumerClientRep.getSecret());
        AccessToken accessToken = toAccessToken(response.getAccessToken());

        assertThat(accessToken.getOtherClaims().get(CLAIM), CoreMatchers.equalTo(CLAIM_VALUE));
    }

    protected final void persistMapper(IdentityProviderMapperRepresentation idpMapper) {
        String idpAlias = bc.getIDPAlias();
        IdentityProviderResource idpResource = realm.identityProviders().get(idpAlias);
        idpMapper.setIdentityProviderAlias(idpAlias);

        CreatedResponseUtil.getCreatedId(idpResource.addMapper(idpMapper));
    }

    private AccessToken toAccessToken(String encoded) {
        AccessToken accessToken;

        try {
            accessToken = new JWSInput(encoded).readJsonContent(AccessToken.class);
        } catch (JWSInputException cause) {
            throw new RuntimeException("Failed to deserialize token", cause);
        }
        return accessToken;
    }
}
