/*
 * Copyright (c) 2002-2023 Gargoyle Software Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.htmlunit;

import java.io.Serializable;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Holder for different types of storages.
 * <p><span style="color:red">Experimental API: May be changed in next release!</span></p>
 *
 * @author Ahmed Ashour
 * @author Ronald Brill
 * @author Marc Guillemot
 */
public class StorageHolder implements Serializable {

    /**
     * Type for Storage.
     */
    public enum Type {
        /** Old Firefox's global storage. */
        GLOBAL_STORAGE,
        /** The type for window.localStorage. */
        LOCAL_STORAGE,
        /** The type for window.sessionStorage. */
        SESSION_STORAGE
    }

    private final Map<String, Map<String, String>> globalStorage_ = new HashMap<>();

    private final Map<String, Map<String, String>> localStorage_ = new HashMap<>();

    private final transient Map<String, Map<String, String>> sessionStorage_ = new HashMap<>();

    /**
     * Gets the store of the give type for the page.
     * @param storageType the type
     * @param page the page
     * @return the store
     */
    public Map<String, String> getStore(final Type storageType, final Page page) {
        final Map<String, Map<String, String>> storage = getStorage(storageType);
        if (storage == null) {
            return null;
        }

        synchronized (storage) {
            final String key = getKey(storageType, page);
            return storage.computeIfAbsent(key, k -> new LinkedHashMap<>());
        }
    }

    private static String getKey(final Type type, final Page page) {
        switch (type) {
            case GLOBAL_STORAGE:
                return page.getUrl().getHost();

            case LOCAL_STORAGE:
                final URL url = page.getUrl();
                return url.getProtocol() + "://" + url.getHost() + ':'
                        + url.getProtocol();

            case SESSION_STORAGE:
                final WebWindow topWindow = page.getEnclosingWindow()
                        .getTopWindow();
                return Integer.toHexString(topWindow.hashCode());

            default:
                return null;
        }
    }

    private Map<String, Map<String, String>> getStorage(final Type type) {
        switch (type) {
            case GLOBAL_STORAGE:
                return globalStorage_;

            case LOCAL_STORAGE:
                return localStorage_;

            case SESSION_STORAGE:
                return sessionStorage_;

            default:
                return null;
        }
    }
}
