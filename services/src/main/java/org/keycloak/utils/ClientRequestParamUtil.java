package org.keycloak.utils;

import org.keycloak.models.KeycloakSession;
import org.keycloak.protocol.oidc.endpoints.AuthorizationEndpoint;
import org.keycloak.sessions.AuthenticationSessionModel;

import jakarta.ws.rs.core.MultivaluedMap;

public final class ClientRequestParamUtil {

    private ClientRequestParamUtil() {
        // noop
    }

    /**
     * Gets a client request param, either stored in the AuthenticationSession (on the initial login request) or, when
     * none is stored there, from the current request.
     *
     * @param name the name of the param
     * @param keycloakSession the keycloak session
     * @param authenticationSession the authentication session, may be {@code null}
     * @return the value of the param or {@code null}
     */
    public static String getClientRequestParam(String name, KeycloakSession keycloakSession,
            AuthenticationSessionModel authenticationSession) {
        /*
         * in some cases (for example post-login flow), the requested params are just provided as a client note. Because
         * this is the originally requested value on authorization, we use this value even when there is a param in the
         * current URL
         */
        if (authenticationSession != null) {
            String reqParamFromClientNote = authenticationSession
                    .getClientNote(AuthorizationEndpoint.LOGIN_SESSION_NOTE_ADDITIONAL_REQ_PARAMS_PREFIX + name);
            if (reqParamFromClientNote != null) {
                return reqParamFromClientNote;
            }
        }

        // otherwise get the param from the current URL
        MultivaluedMap<String, String> queryParams = keycloakSession.getContext().getUri().getQueryParameters();
        return queryParams.getFirst(name);
    }
}
