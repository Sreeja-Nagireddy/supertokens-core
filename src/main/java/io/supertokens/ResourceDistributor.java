/*
 *    Copyright (c) 2020, VRAI Labs and/or its affiliates. All rights reserved.
 *
 *    This software is licensed under the Apache License, Version 2.0 (the
 *    "License") as published by the Apache Software Foundation.
 *
 *    You may not use this file except in compliance with the License. You may
 *    obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 *    WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 *    License for the specific language governing permissions and limitations
 *    under the License.
 */

package io.supertokens;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

// the purpose of this class is to tie singleton classes to s specific main instance. So that
// when the main instance dies, those singleton classes die too.

public class ResourceDistributor {

    private final Object lock = new Object();
    private Map<KeyClass, SingletonResource> resources = new HashMap<>();

    public SingletonResource getResource(@Nullable String connectionUriDomain, @Nullable String tenantId,
                                         @Nonnull String key) {
        synchronized (lock) {
            // first we do exact match
            SingletonResource resource = resources.get(new KeyClass(connectionUriDomain, tenantId, key));
            if (resource != null) {
                return resource;
            }

            // then we prioritise based on connectionUriDomain match
            resource = resources.get(new KeyClass(connectionUriDomain, null, key));
            if (resource != null) {
                return resource;
            }

            // then we prioritise based on tenantId match
            resource = resources.get(new KeyClass(null, tenantId, key));
            if (resource != null) {
                return resource;
            }

            // then we return the base case
            resource = resources.get(new KeyClass(null, null, key));
            return resource;
        }
    }

    public SingletonResource getResource(@Nonnull String key) {
        synchronized (lock) {
            return resources.get(new KeyClass(null, null, key));
        }
    }

    public SingletonResource setResource(@Nullable String connectionUriDomain, @Nullable String tenantId,
                                         @Nonnull String key,
                                         SingletonResource resource) {
        synchronized (lock) {
            SingletonResource alreadyExists = resources.get(new KeyClass(connectionUriDomain, tenantId, key));
            if (alreadyExists != null) {
                return alreadyExists;
            }
            resources.put(new KeyClass(connectionUriDomain, tenantId, key), resource);
            return resource;
        }
    }

    public SingletonResource setResource(@Nonnull String key,
                                         SingletonResource resource) {
        return setResource(null, null, key, resource);
    }

    public static class SingletonResource {

    }

    private static class KeyClass {
        @Nonnull
        String key;

        @Nonnull
        String connectionUriDomain;

        @Nonnull
        String tenantId;

        KeyClass(@Nullable String connectionUriDomain, @Nullable String tenantId, @Nonnull String key) {
            this.key = key;
            this.connectionUriDomain = connectionUriDomain == null ? "" : connectionUriDomain;
            this.tenantId = tenantId == null ? "" : tenantId;
        }

        @Override
        public boolean equals(Object other) {
            if (other instanceof KeyClass) {
                KeyClass otherKeyClass = (KeyClass) other;
                return otherKeyClass.tenantId.equals(this.tenantId) &&
                        otherKeyClass.connectionUriDomain.equals(connectionUriDomain) &&
                        otherKeyClass.key.equals(key);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return (this.tenantId + "|" + this.connectionUriDomain + "|" + this.key).hashCode();
        }
    }

}
