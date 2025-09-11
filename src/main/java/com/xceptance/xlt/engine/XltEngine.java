/*
 * Copyright (c) 2005-2025 Xceptance Software Technologies GmbH
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
package com.xceptance.xlt.engine;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;

import com.xceptance.xlt.engine.resultbrowser.ErrorCounter;
import com.xceptance.xlt.util.XltPropertiesImpl;

/**
 * This class tries to make singleton handling easier and especially targets testing purposes.
 * Under production conditions, this hopefully is all happy and fast. The beauty of this approach is
 * also that we can keep all dependencies at the end aka most depend on the properties.
 *
 * Main entry point into the XLT framework.
 *
 * @author Rene Schwietzke
 * @since 7.0.0
 */
public class XltEngine
{
    private static XltEngine instance = new XltEngine(true, true);
    private static final VarHandle instanceHandle;

    public final XltPropertiesImpl xltProperties;
    public final ErrorCounter errorCounter;

    static
    {
        try
        {
            instanceHandle = MethodHandles.lookup().findStaticVarHandle(XltEngine.class, "instance", XltEngine.class);
        }
        catch (ReflectiveOperationException e)
        {
            throw new ExceptionInInitializerError(e);
        }
    }

    /**
     * Our constructor that helps us to build all our singletons in the right orders.
     *
     * @param ignoreMissingProperties true, ignore missing properties when loading, otherwise we will complain
     */
    private XltEngine(final boolean ignoreMissingProperties, final boolean staySilent)
    {
        this(XltPropertiesImpl.createInstance(ignoreMissingProperties, staySilent));
    }

    /**
     * Setup a new instance based on passed properties. It will recreated all singletons underneath
     *
     * @param properties our new central properties
     */
    private XltEngine(final XltPropertiesImpl properties)
    {
        this.xltProperties = properties;
        this.errorCounter = ErrorCounter.createInstance(this.xltProperties);
    }

    /**
     * Get the singleton of the singleton central manager. If not yet setup, we will
     * create it. If it exists, we will return what we have.
     */
    public static XltEngine get()
    {
        return instance;
    }

    /**
     * Returns a new singleton and replaces the old one using this property object
     * including setting this property object as the new central one.
     *
     * @param properties a new property source
     * @return the new engine and its dependencies
     */
    public static XltEngine reset(final XltPropertiesImpl properties)
    {
        set(new XltEngine(properties));

        return get();
    }

    /**
     * Creates a new instance, makes it available and returns it.
     * It will start with default properties it discovers on its own.
     *
     * @return the engine instance
     */
    public static XltEngine reset()
    {
        set(new XltEngine(false, false));

        return get();
    }

    private static XltEngine set(final XltEngine newInstance)
    {
        instanceHandle.setVolatile(newInstance);
        return newInstance;
    }
}
