/*
 * Copyright (c) 2005-2022 Xceptance Software Technologies GmbH
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

import com.xceptance.xlt.common.XltConstants;

/**
 * (Future) main entry point into the XLT framework.
 */
public class XltEngine
{
    /**
     * The {@link XltEngine} singleton instance.
     */
    private static final XltEngine instance = new XltEngine();

    /**
     * Returns the one and only {@link XltEngine} instance.
     */
    public static XltEngine getInstance()
    {
        return instance;
    }

    /**
     * Whether or not XLT is run in "dev mode".
     */
    private final boolean devMode;

    /**
     * Constructor.
     */
    private XltEngine()
    {
        // TODO: This is rather hack-ish.
        devMode = (System.getenv("XLT_HOME") == null && System.getProperty(XltConstants.XLT_PACKAGE_PATH + ".home") == null);
    }

    /**
     * Returns whether or not XLT is run in "dev mode".
     */
    public boolean isDevMode()
    {
        return devMode;
    }
}
