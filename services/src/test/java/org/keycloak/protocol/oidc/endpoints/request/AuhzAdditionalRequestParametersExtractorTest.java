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

import org.hamcrest.core.AllOf;
import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.collection.IsMapContaining.hasEntry;
import static org.hamcrest.collection.IsMapContaining.hasKey;
import static org.hamcrest.collection.IsMapWithSize.aMapWithSize;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertThrows;

public class AuhzAdditionalRequestParametersExtractorTest {

    @Test
    public void shouldAddAdditionalParams() {
        Map<String, String> paramsMap = new HashMap<>();
        paramsMap.put("foo", "bar");

        AuthzAdditionalRequestParamsExtractor extractor = new AuthzAdditionalRequestParamsExtractor(paramsMap::get);
        Map<String, String> targetMap = new HashMap<>();
        extractor.addAdditionalParams(paramsMap.keySet().stream(), targetMap);

        assertThat(targetMap, hasEntry("foo", "bar"));
    }

    @Test
    public void shouldAddMultipleParams() {
        Map<String, String> paramsMap = new HashMap<>();
        paramsMap.put("foo", "bar");
        paramsMap.put("a", "b");
        paramsMap.put("some-more-lengthy-key", "some-value");

        AuthzAdditionalRequestParamsExtractor extractor = new AuthzAdditionalRequestParamsExtractor(paramsMap::get);
        Map<String, String> targetMap = new HashMap<>();
        extractor.addAdditionalParams(paramsMap.keySet().stream(), targetMap);

        assertThat(targetMap, AllOf.allOf(
                hasEntry("foo", "bar"),
                hasEntry("a", "b"),
                hasEntry("some-more-lengthy-key", "some-value")
        ));
    }

    @Test
    public void shouldNotAddEmptyParams() {
        Map<String, String> paramsMap = new HashMap<>();
        paramsMap.put("foo", "bar");
        paramsMap.put("empty", "");
        paramsMap.put("whitespaces", "   ");

        AuthzAdditionalRequestParamsExtractor extractor = new AuthzAdditionalRequestParamsExtractor(paramsMap::get);
        Map<String, String> targetMap = new HashMap<>();
        extractor.addAdditionalParams(paramsMap.keySet().stream(), targetMap);

        assertThat(targetMap, AllOf.allOf(
            hasEntry("foo", "bar"),
            not(hasKey("empty")),
            not(hasKey("whitespaces"))
        ));
    }

    @Test
    public void shouldDiscardNullValue() {
        Map<String, String> paramsMap = new HashMap<>();
        paramsMap.put("nullvalue", null);
        paramsMap.put("foo", "bar");

        AuthzAdditionalRequestParamsExtractor extractor = new AuthzAdditionalRequestParamsExtractor(paramsMap::get);
        Map<String, String> targetMap = new HashMap<>();
        extractor.addAdditionalParams(paramsMap.keySet().stream(), targetMap);

        assertThat(targetMap, AllOf.allOf(
                hasEntry("foo", "bar"),
                not(hasKey("nullvalue"))
        ));
    }

    @Test
    public void shouldAdd100SmallParams() {
        final int numberOfParams = 100;
        Map<String, String> paramsMap = new HashMap<>();
        for (int i = 0; i < numberOfParams; i++) {
            paramsMap.put(Integer.toString(i), "test_value_with_average_size");
        }

        AuthzAdditionalRequestParamsExtractor extractor = new AuthzAdditionalRequestParamsExtractor(paramsMap::get);
        Map<String, String> targetMap = new HashMap<>();
        extractor.addAdditionalParams(paramsMap.keySet().stream(), targetMap);

        assertThat(targetMap, aMapWithSize(numberOfParams));
    }

    @Test
    public void shouldThrowWhenLimitExceeded() {
        // WHEN: Multiple parameters with 1000 characters plus param name size exceed the limit
        Map<String, String> paramsMap = new HashMap<>();
        String thousandX = String.join("", Collections.nCopies(1000, "x"));
        int numberOfParams = AuthzAdditionalRequestParamsExtractor.ADDITIONAL_REQ_PARAMS_MAX_CHARS / 1000;
        for (int i = 0; i < numberOfParams; i++) {
            paramsMap.put(Integer.toString(i), thousandX);
        }

        AuthzAdditionalRequestParamsExtractor extractor = new AuthzAdditionalRequestParamsExtractor(paramsMap::get);
        Map<String, String> targetMap = new HashMap<>();
        assertThrows(IllegalArgumentException.class,
                () -> extractor.addAdditionalParams(paramsMap.keySet().stream(), targetMap));
    }

}
