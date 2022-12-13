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
package com.xceptance.xlt.engine.util;

import java.net.MalformedURLException;
import java.net.URL;

import com.xceptance.xlt.api.util.XltLogger;
import com.xceptance.xlt.util.XltPropertiesImpl;

public final class URLCleaner
{
    /**
     * Default constructor. Declared private to prevent external instantiation.
     */
    private URLCleaner()
    {
    }

    public static URL removeUserInfoIfNecessaryAsURL(final URL url)
    {
        // remove user-info from request URL if we need to (GH #57)
        if (XltPropertiesImpl.removeUserInfoFromRequestUrl())
        {
            try
            {
                return UrlUtils.getURLWithoutUserInfo(url);
            }
            catch (final MalformedURLException mue)
            {
                XltLogger.runTimeLogger.error("Failed to remove user-info from URL '{}'", url, mue);
            }
        }

        // return URL as is
        return url;
    }

    public static String removeUserInfoIfNecessaryAsString(final URL url)
    {
        // remove user-info from request URL if we need to (GH #57)
        if (XltPropertiesImpl.removeUserInfoFromRequestUrl())
        {
            return UrlUtils.removeUserInfo(url);
        }

        // return URL as string
        return url.toExternalForm();
    }

    public static String removeUserInfoIfNecessaryAsString(final String url)
    {
        // remove user-info from request URL if we need to (GH #57)
        if (XltPropertiesImpl.removeUserInfoFromRequestUrl())
        {
            return UrlUtils.removeUserInfo(url);
        }

        // return URL string as is
        return url;
    }

}
