/*
 * Copyright (c) 2005-2021 Xceptance Software Technologies GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.xceptance.common.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * Utility class that provides convenient methods regarding ordinary java objects.
 * 
 * @author Jörg Werner (Xceptance Software Technologies GmbH)
 */
public final class ObjectUtils
{
    /**
     * Default constructor. Declared private to prevent external instantiation.
     */
    private ObjectUtils()
    {
    }

    /**
     * Creates a deep copy of the passed object. For this method to work, the object to be cloned and all contained
     * objects must be serializable.
     * 
     * @param o
     *            the object to clone
     * @return the clone
     * @throws Exception
     *             if an error occurred, especially if the object is not serializable
     */
    public static Object cloneObject(final Object o) throws Exception
    {
        // stream the object to a byte buffer
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final ObjectOutputStream out = new ObjectOutputStream(baos);

        out.writeObject(o);
        out.flush();

        // reconstruct the object from the byte buffer
        final ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        final ObjectInputStream in = new ObjectInputStream(bais);

        return in.readObject();
    }
}
