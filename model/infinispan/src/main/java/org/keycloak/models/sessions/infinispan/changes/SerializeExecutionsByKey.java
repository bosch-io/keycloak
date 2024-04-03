/*
 * Copyright 2024 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.models.sessions.infinispan.changes;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Adding an in-JVM lock to prevent a best-effort concurrent executions for the same ID.
 * This should prevent a burst of requests by letting only the first request pass, and then the others will follow one-by-one.
 * Use this when the code wrapped by runSerialized is known to produce conflicts when run concurrently with the same ID.
 *
 * @author Alexander Schwartz
 */
public class SerializeExecutionsByKey<K> {
    private final ConcurrentHashMap<K, K> cacheInteractions = new ConcurrentHashMap<>();

    public void runSerialized(K key, Runnable task) {
        // this locking is only to ensure that if there is a computation for the same id in the "synchronized" block below,
        // it will have the same object instance to lock the current execution until the other is finished.
        K lock = cacheInteractions.computeIfAbsent(key, s -> key);
        try {
            synchronized (lock) {
                // in case the previous thread has removed the entry in the finally block
                cacheInteractions.putIfAbsent(key, lock);
                task.run();
            }
        } finally {
            cacheInteractions.remove(lock);
        }
    }
}
