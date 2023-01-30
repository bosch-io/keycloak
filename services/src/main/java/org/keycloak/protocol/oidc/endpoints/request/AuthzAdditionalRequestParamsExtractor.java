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

package org.keycloak.protocol.oidc.endpoints.request;

import org.jboss.logging.Logger;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Fills additional request parameters into a Map. The overall size of parameter names and values is allowed to be up to
 * {@value ADDITIONAL_REQ_PARAMS_MAX_CHARS} characters. An exception is thrown if this limit is exceeded.
 */
public class AuthzAdditionalRequestParamsExtractor {

    private static final Logger logger = Logger.getLogger(AuthzEndpointRequestParser.class);

    static int ADDITIONAL_REQ_PARAMS_MAX_CHARS = 12000;

    private final Function<String, String> valueFunction;

    public AuthzAdditionalRequestParamsExtractor(Function<String, String> valueFunction) {
        this.valueFunction = valueFunction;
    }

    public void addAdditionalParams(Stream<String> paramNames, Map<String, String> targetReqParams) {
        final int[] currentLength = {0};
        paramNames.forEach(name -> {
            String value = valueFunction.apply(name);
            if (value == null)
            {
                return;
            }
            if (value.trim().isEmpty()) {
                logger.debug("OIDC Additional param " + name + " ignored because value is empty.");
                return;
            }
            final int paramLength = name.length() + value.length();
            if (currentLength[0] + paramLength >= ADDITIONAL_REQ_PARAMS_MAX_CHARS) {
                throw new IllegalArgumentException("The overall length of additional request parameter names and values exceeds the limit of" + ADDITIONAL_REQ_PARAMS_MAX_CHARS);
            }
            targetReqParams.put(name, value);
            currentLength[0] += paramLength;
        });
    }
}
