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
package com.xceptance.xlt.api.engine;

import java.net.MalformedURLException;
import java.net.URL;

import com.gargoylesoftware.htmlunit.WebRequest;

/**
 * This class provides a dummy implementation of {@link CustomData} but makes {@link #parseRemainingValues(String[])} public to
 * allow modifications for testing purposes.
 * <p>
 * The class provides the convenience method {@link #getDefault()} which gives a new instance of this class for each
 * invocation.
 * </p>
 * 
 * @author Sebastian Oerding
 */
public class DummyNetworkData extends NetworkData
{
    /**
     * Instantiates a new instance with a request and no response. The request URL points to
     * &quot;http://localhost&quot;.
     */
    public DummyNetworkData()
    {
        super(new WebRequest(getLocalHostUrl()), null);
    }

    private static URL getLocalHostUrl()
    {
        try
        {
            return new URL("http://localhost");
        }
        catch (final MalformedURLException e)
        {
            // ignore as this won't happen
            throw new RuntimeException(e);
        }
    }
}
