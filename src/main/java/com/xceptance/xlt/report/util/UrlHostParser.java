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
package com.xceptance.xlt.report.util;

import com.xceptance.xlt.api.util.XltCharBuffer;

public class UrlHostParser
{
    // just an int to know what to exclude cheaply when searching
    private final static int MAX_CHAR = Math.max(Math.max('/', '?'), '#');

    private static final XltCharBuffer HOSTSEPARATOR = XltCharBuffer.valueOf("://");
    
    /**
     * Gets the host from a url as string as cheaply as possible. Does not pay attention to any special url formats
     * or rules. Mainly meant for report processing 
     * 
     * @param url the url to retrieve the host name from
     * @return the host name in the url or the full url if not host name can be identified
     */
    public static XltCharBuffer retrieveHostFromUrl(final XltCharBuffer url)
    {
        // strip protocol
        int start = url.indexOf(HOSTSEPARATOR);
        start = start == -1 ? 0 : start + 3;

        // strip path/query/fragment if present (whatever comes first)
        final int length = url.length();
        
        for (int i = start; i < length; i++)
        {
            final char c = url.charAt(i);
            
            // avoid all three comparison by checking for a lot of
            // not relevant chars first
            if (c <= MAX_CHAR && (c == '/' || c == '?' || c == '#'))
            {
                final XltCharBuffer result = url.substring(start, i);
                result.hashCode();
                return result;
            }
        }
        
        // no end, check if we got a start
        if (start == 0)
        {
            // no start, use the original, create the hashcode if needed
            url.hashCode();
            return url;
        }
        else
        {
            // at least we had a start
            final XltCharBuffer result = url.substring(start);
            result.hashCode();
            return result;
        }
    }
}
