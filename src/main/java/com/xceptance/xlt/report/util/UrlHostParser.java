/*
 * Copyright (c) 2005-2026 Xceptance Software Technologies GmbH
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

public class UrlHostParser
{
    // just an int to know what to exclude cheaply when searching
    private final static int MAX_CHAR = Math.max(Math.max('/', '?'), '#');

    private static final String HOSTSEPARATOR = "://";
    
    /**
     * Gets the host from a url as string as cheaply as possible. Does not pay attention to any special url formats
     * or rules. Mainly meant for report processing 
     * 
     * @param url the url to retrieve the host name from
     * @return the host name in the url or the full url if not host name can be identified
     */
    public static String retrieveHostFromUrl(final String url)
    {
        if (url == null)
        {
            return "";
        }

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
                return url.substring(start, i);
            }
        }
        
        // no end, check if we got a start
        if (start == 0)
        {
            return url;
        }
        else
        {
            return url.substring(start);
        }
    }

    /**
     * Thread-local 16-slot direct-mapped cache mapping raw UTF-8 host byte slices to canonical
     * {@link String} instances. Eliminates both full-URL {@link String} materialization and
     * substring allocations when extracting hosts from CSV byte buffers in {@code RequestData.getHost()}.
     */
    private static final ThreadLocal<HostByteCache> HOST_BYTE_CACHE = ThreadLocal.withInitial(HostByteCache::new);

    private static final class HostByteCache
    {
        private static final int SLOTS = 16;
        private static final int MASK = SLOTS - 1;
        final byte[][] bytes = new byte[SLOTS][];
        final String[] hosts = new String[SLOTS];
    }

    /**
     * Extracts the host name directly from a raw UTF-8 URL byte slice without materializing the full
     * URL {@link String}, returning a cached {@link String} instance for repeated hosts.
     *
     * @param urlBytes
     *            backing UTF-8 byte buffer containing the URL
     * @param offset
     *            start offset of the URL within {@code urlBytes}
     * @param length
     *            byte length of the URL slice
     * @return the extracted host name, or {@code ""} if empty
     */
    public static String retrieveHostFromUrl(final byte[] urlBytes, final int offset, final int length)
    {
        if (urlBytes == null || length <= 0)
        {
            return "";
        }

        final int limit = offset + length;
        int start = offset;

        // Strip protocol prefix "://" if present
        final int sepLimit = limit - 2;
        for (int i = offset; i < sepLimit; i++)
        {
            if (urlBytes[i] == ':' && urlBytes[i + 1] == '/' && urlBytes[i + 2] == '/')
            {
                start = i + 3;
                break;
            }
        }

        // Strip path ('/'), query ('?'), or fragment ('#')
        int end = limit;
        for (int i = start; i < limit; i++)
        {
            final int b = urlBytes[i] & 0xFF;
            if (b <= MAX_CHAR && (b == '/' || b == '?' || b == '#'))
            {
                end = i;
                break;
            }
        }

        final int hostLen = end - start;
        if (hostLen <= 0)
        {
            return "";
        }

        // Compute FNV-1a hash of the host byte slice and probe the thread-local 16-slot cache
        int hash = 0x811c9dc5;
        for (int i = start; i < end; i++)
        {
            hash ^= (urlBytes[i] & 0xFF);
            hash *= 0x01000193;
        }
        final int slot = (hash ^ (hash >>> 4)) & HostByteCache.MASK;
        final HostByteCache cache = HOST_BYTE_CACHE.get();
        final byte[] cachedBytes = cache.bytes[slot];
        if (cachedBytes != null && cachedBytes.length == hostLen
            && java.util.Arrays.equals(urlBytes, start, end, cachedBytes, 0, hostLen))
        {
            return cache.hosts[slot];
        }

        final String host = new String(urlBytes, start, hostLen, java.nio.charset.StandardCharsets.UTF_8);
        cache.bytes[slot] = java.util.Arrays.copyOfRange(urlBytes, start, end);
        cache.hosts[slot] = host;
        return host;
    }
}
