package org.keycloak.testsuite.broker;

import org.keycloak.models.IdentityProviderMapperModel;
import org.keycloak.models.IdentityProviderMapperSyncMode;
import org.keycloak.representations.idm.IdentityProviderMapperRepresentation;

import com.google.common.collect.ImmutableMap;

public class OidcClaimToUserSessionNoteMapperTest extends AbstractUserSessionNoteMapperTest {

    protected static final String CLAIM = "sessionNoteTest";
    protected static final String CLAIM_VALUE = "foo";

    @Override
    protected BrokerConfiguration getBrokerConfiguration() {
        return new KcOidcBrokerConfiguration();
    }

    @Override
    protected void createMapperInIdp(IdentityProviderMapperSyncMode syncMode) {
        IdentityProviderMapperRepresentation claimToRoleMapper = new IdentityProviderMapperRepresentation();
        claimToRoleMapper.setName("User Session Note Idp Mapper");
        claimToRoleMapper.setIdentityProviderMapper("oidc-user-session-note-idp-mapper");
        claimToRoleMapper.setConfig(ImmutableMap.<String, String> builder()
                .put(IdentityProviderMapperModel.SYNC_MODE, syncMode.toString())
                .put("claims", "[{\"key\":\"sessionNoteTest\",\"value\":\"foo\"}]")
                .build());

        persistMapper(claimToRoleMapper);
    }
}
