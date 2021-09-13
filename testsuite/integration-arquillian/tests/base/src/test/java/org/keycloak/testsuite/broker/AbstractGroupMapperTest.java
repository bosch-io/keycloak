package org.keycloak.testsuite.broker;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.keycloak.testsuite.broker.BrokerTestTools.getConsumerRoot;

import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.models.IdentityProviderMapperSyncMode;
import org.keycloak.representations.idm.GroupRepresentation;
import org.keycloak.representations.idm.IdentityProviderRepresentation;
import org.keycloak.representations.idm.UserRepresentation;

import java.util.List;
import java.util.Map;

public abstract class AbstractGroupMapperTest extends AbstractIdentityProviderMapperTest {

    public static final String GROUP_MAPPER_TEST = "mapper-test";

    protected abstract void createMapperInIdp(
            IdentityProviderRepresentation idp, IdentityProviderMapperSyncMode syncMode);

    protected void updateUser() {
    }

    protected UserRepresentation loginAsUserTwiceWithMapper(
            IdentityProviderMapperSyncMode syncMode, boolean createAfterFirstLogin,
            Map<String, List<String>> userConfig) {
        final IdentityProviderRepresentation idp = setupIdentityProvider();
        if (!createAfterFirstLogin) {
            createMapperInIdp(idp, syncMode);
        }
        createUserInProviderRealm(userConfig);
        createGroupAndAssignUserToItInProviderRealm();

        logInAsUserInIDPForFirstTime();

        UserRepresentation user = findUser(bc.consumerRealmName(), bc.getUserLogin(), bc.getUserEmail());
        if (!createAfterFirstLogin) {
            assertThatUserHasBeenAssignedToGroup(user);
        } else {
            assertThatUserHasNotBeenAssignedToGroup(user);
        }

        if (createAfterFirstLogin) {
            createMapperInIdp(idp, syncMode);
        }
        logoutFromRealm(getConsumerRoot(), bc.consumerRealmName());

        updateUser();

        logInAsUserInIDP();
        user = findUser(bc.consumerRealmName(), bc.getUserLogin(), bc.getUserEmail());
        return user;
    }

    protected void createGroupAndAssignUserToItInProviderRealm() {
        GroupRepresentation group = new GroupRepresentation();
        group.setName(GROUP_MAPPER_TEST);

        adminClient.realm(bc.providerRealmName()).groups().add(group);
        UserResource userResource = adminClient.realm(bc.providerRealmName()).users().get(userId);
        userResource.groups().add(group);
    }

    protected void assertThatUserHasBeenAssignedToGroup(UserRepresentation user) {
        assertTrue(user.getGroups().contains(GROUP_MAPPER_TEST));
    }

    protected void assertThatUserHasNotBeenAssignedToGroup(UserRepresentation user) {
        assertFalse(user.getGroups().contains(GROUP_MAPPER_TEST));
    }
}
