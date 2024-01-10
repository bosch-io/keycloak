/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates
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
package org.keycloak.services.resources;

import org.jboss.logging.Logger;
import org.keycloak.common.Profile;
import org.keycloak.common.Version;
import org.keycloak.common.util.Base64Url;
import org.keycloak.common.util.MimeTypeUtil;
import org.keycloak.common.util.SecretGenerator;
import org.keycloak.http.HttpRequest;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.utils.KeycloakModelUtils;
import org.keycloak.services.ForbiddenException;
import org.keycloak.services.ServicesLogger;
import org.keycloak.services.managers.ApplianceBootstrap;
import org.keycloak.services.util.CacheControlUtil;
import org.keycloak.services.util.CookieHelper;
import org.keycloak.theme.Theme;
import org.keycloak.theme.freemarker.FreeMarkerProvider;
import org.keycloak.urls.UrlType;
import org.keycloak.utils.MediaType;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Cookie;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.ResponseBuilder;
import jakarta.ws.rs.core.Response.Status;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
@Path("/")
public class WelcomeResource {

    protected static final Logger logger = Logger.getLogger(WelcomeResource.class);

    private static final String KEYCLOAK_STATE_CHECKER = "WELCOME_STATE_CHECKER";

    private AtomicBoolean shouldBootstrap;

    @Context
    KeycloakSession session;

    /**
     * Welcome page of Keycloak
     *
     * @return
     * @throws URISyntaxException
     */
    @GET
    @Produces(MediaType.TEXT_HTML_UTF_8)
    public Response getWelcomePage() throws URISyntaxException {
        String requestUri = session.getContext().getUri().getRequestUri().toString();
        if (!requestUri.endsWith("/")) {
            return Response.seeOther(new URI(requestUri + "/")).build();
        } else {
            return createWelcomePage(null, null);
        }
    }

    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.TEXT_HTML_UTF_8)
    public Response createUser() {
        HttpRequest request = session.getContext().getHttpRequest();
        MultivaluedMap<String, String> formData = request.getDecodedFormParameters();

        if (!shouldBootstrap()) {
            return createWelcomePage(null, null);
        } else {
            if (!isLocal()) {
                ServicesLogger.LOGGER.rejectedNonLocalAttemptToCreateInitialUser(session.getContext().getConnection().getRemoteAddr());
                throw new WebApplicationException(Response.Status.BAD_REQUEST);
            }

            csrfCheck(formData);

            String username = formData.getFirst("username");
            String password = formData.getFirst("password");
            String passwordConfirmation = formData.getFirst("passwordConfirmation");

            if (username != null) {
                username = username.trim();
            }

            if (username == null || username.length() == 0) {
                return createWelcomePage(null, "Username is missing");
            }

            if (password == null || password.length() == 0) {
                return createWelcomePage(null, "Password is missing");
            }

            if (!password.equals(passwordConfirmation)) {
                return createWelcomePage(null, "Password and confirmation doesn't match");
            }

            expireCsrfCookie();

            ApplianceBootstrap applianceBootstrap = new ApplianceBootstrap(session);
            applianceBootstrap.createMasterRealmUser(username, password);

            shouldBootstrap.set(false);
            ServicesLogger.LOGGER.createdInitialAdminUser(username);
            return createWelcomePage("User created", null);
        }
    }

    /**
     * Resources for welcome page
     *
     * @param path
     * @return
     */
    @GET
    @Path("/welcome-content/{path}")
    @Produces(MediaType.TEXT_HTML_UTF_8)
    public Response getResource(@PathParam("path") String path) {
        try {
            InputStream resource = getTheme().getResourceAsStream(path);
            if (resource != null) {
                String contentType = MimeTypeUtil.getContentType(path);
                Response.ResponseBuilder builder = Response.ok(resource).type(contentType).cacheControl(CacheControlUtil.getDefaultCacheControl());
                return builder.build();
            } else {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
        } catch (IOException e) {
            throw new WebApplicationException(e, Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    private Response createWelcomePage(String successMessage, String errorMessage) {
        try {
            Theme theme = getTheme();

            if(Objects.isNull(theme)) {
                logger.error("Theme is null please check the \"--spi-theme-default\" parameter");
                errorMessage = "The theme is null";
                ResponseBuilder rb = Response.status(Status.BAD_REQUEST)
                        .entity(errorMessage)
                        .cacheControl(CacheControlUtil.noCache());
                return rb.build();
            }

            Map<String, Object> map = new HashMap<>();

            map.put("adminConsoleEnabled", isAdminConsoleEnabled());
            map.put("productName", Version.NAME);

            map.put("properties", theme.getProperties());
            map.put("adminUrl", session.getContext().getUri(UrlType.ADMIN).getBaseUriBuilder().path("/admin/").build());

            map.put("resourcesPath", "resources/" + Version.RESOURCES_VERSION + "/" + theme.getType().toString().toLowerCase() +"/" + theme.getName());
            map.put("resourcesCommonPath", "resources/" + Version.RESOURCES_VERSION + "/common/keycloak");

            boolean bootstrap = shouldBootstrap();
            map.put("bootstrap", bootstrap);
            if (bootstrap) {
                boolean isLocal = isLocal();
                map.put("localUser", isLocal);

                String localAdminUrl = session.getContext().getUri(UrlType.LOCAL_ADMIN).getBaseUri().toString();
                String adminCreationMessage = getAdminCreationMessage();
                map.put("localAdminUrl", localAdminUrl);
                map.put("adminUserCreationMessage", adminCreationMessage);

                if (isLocal) {
                    String stateChecker = setCsrfCookie();
                    map.put("stateChecker", stateChecker);
                }
            }
            if (successMessage != null) {
                map.put("successMessage", successMessage);
            }
            if (errorMessage != null) {
                map.put("errorMessage", errorMessage);
            }
            FreeMarkerProvider freeMarkerUtil = session.getProvider(FreeMarkerProvider.class);
            String result = freeMarkerUtil.processTemplate(map, "index.ftl", theme);

            ResponseBuilder rb = Response.status(errorMessage == null ? Status.OK : Status.BAD_REQUEST)
                    .entity(result)
                    .cacheControl(CacheControlUtil.noCache());
            return rb.build();
        } catch (Exception e) {
            throw new WebApplicationException(e, Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    private static boolean isAdminConsoleEnabled() {
        return Profile.isFeatureEnabled(Profile.Feature.ADMIN2);
    }

    private Theme getTheme() {
        try {
            return session.theme().getTheme(Theme.Type.WELCOME);
        } catch (IOException e) {
            throw new WebApplicationException(e, Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    protected String getAdminCreationMessage() {
        return "or set the environment variables KEYCLOAK_ADMIN and KEYCLOAK_ADMIN_PASSWORD before starting the server";
    }

    private boolean shouldBootstrap() {
        if (shouldBootstrap == null) {
            synchronized (this) {
                if (shouldBootstrap == null) {
                    shouldBootstrap = new AtomicBoolean(new ApplianceBootstrap(session).isNoMasterUser());
                }
            }
        }
        return shouldBootstrap.get();
    }

    private boolean isLocal() {
        return KeycloakModelUtils.isLocalRequest(session.getContext());
    }

    private String setCsrfCookie() {
        String stateChecker = Base64Url.encode(SecretGenerator.getInstance().randomBytes());
        String cookiePath = session.getContext().getUri().getPath();
        boolean secureOnly = session.getContext().getUri().getRequestUri().getScheme().equalsIgnoreCase("https");
        CookieHelper.addCookie(KEYCLOAK_STATE_CHECKER, stateChecker, cookiePath, null, null, 300, secureOnly, true, session);
        return stateChecker;
    }

    private void expireCsrfCookie() {
        String cookiePath = session.getContext().getUri().getPath();
        boolean secureOnly = session.getContext().getUri().getRequestUri().getScheme().equalsIgnoreCase("https");
        CookieHelper.addCookie(KEYCLOAK_STATE_CHECKER, "", cookiePath, null, null, 0, secureOnly, true, session);
    }

    private void csrfCheck(final MultivaluedMap<String, String> formData) {
        String formStateChecker = formData.getFirst("stateChecker");
        HttpRequest request = session.getContext().getHttpRequest();
        HttpHeaders headers = request.getHttpHeaders();
        Cookie cookie = headers.getCookies().get(KEYCLOAK_STATE_CHECKER);
        if (cookie == null) {
            throw new ForbiddenException();
        }

        String cookieStateChecker = cookie.getValue();

        if (cookieStateChecker == null || !cookieStateChecker.equals(formStateChecker)) {
            throw new ForbiddenException();
        }
    }

}
