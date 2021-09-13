package org.keycloak.broker.oidc.mappers;

import org.keycloak.broker.provider.BrokeredIdentityContext;
import org.keycloak.broker.provider.ConfigConstants;
import org.keycloak.broker.provider.IdentityBrokerException;
import org.keycloak.models.GroupModel;
import org.keycloak.models.IdentityProviderMapperModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;

public abstract class AbstractClaimToGroupMapper extends AbstractClaimMapper {

    @Override
    public void importNewUser(KeycloakSession session, RealmModel realm, UserModel user,
            IdentityProviderMapperModel mapperModel, BrokeredIdentityContext context) {

        GroupModel group = this.getGroup(realm, mapperModel);
        if (applies(mapperModel, context)) {
            user.joinGroup(group);
        }
    }

    @Override
    public void updateBrokeredUserLegacy(KeycloakSession session, RealmModel realm, UserModel user,
            IdentityProviderMapperModel mapperModel, BrokeredIdentityContext context) {

        GroupModel group = this.getGroup(realm, mapperModel);
        if (!applies(mapperModel, context)) {
            user.leaveGroup(group);
        }
    }

    @Override
    public void updateBrokeredUser(KeycloakSession session, RealmModel realm, UserModel user,
            IdentityProviderMapperModel mapperModel, BrokeredIdentityContext context) {

        GroupModel group = this.getGroup(realm, mapperModel);
        String groupId = mapperModel.getConfig().get(ConfigConstants.GROUP);

        if (!context.hasMapperAssignedGroup(groupId)) {
            if (applies(mapperModel, context)) {
                context.addMapperAssignedGroup(groupId);
                user.joinGroup(group);
            } else {
                user.leaveGroup(group);
            }
        }
    }

    /**
     * This method must be implemented by subclasses and they must return {@code true} if their mapping can be applied
     * (i.e. user has the OIDC claim that should be mapped) or {@code false} otherwise.
     *
     * @param mapperModel a reference to the {@link IdentityProviderMapperModel}.
     * @param context a reference to the {@link BrokeredIdentityContext}.
     * @return {@code true} if the mapping can be applied or {@code false} otherwise.*
     */
    protected abstract boolean applies(final IdentityProviderMapperModel mapperModel,
            final BrokeredIdentityContext context);

    /**
     * Obtains the {@link GroupModel} corresponding the group configured in the specified
     * {@link IdentityProviderMapperModel}.
     * If the group doesn't exist, this method throws an {@link IdentityBrokerException}.
     *
     * @param realm a reference to the realm.
     * @param mapperModel a reference to the {@link IdentityProviderMapperModel} containing the configured group.
     * @return the {@link GroupModel}
     * @throws IdentityBrokerException if the role group doesn't exist.
     */
    private GroupModel getGroup(final RealmModel realm, final IdentityProviderMapperModel mapperModel) {
        String groupId = mapperModel.getConfig().get(ConfigConstants.GROUP);
        GroupModel group = realm.getGroupById(groupId);

        if (group == null) {
            throw new IdentityBrokerException("Unable to find group: " + groupId);
        }
        return group;
    }
}
