/*
 * Copyright (c) 2005-2024 Xceptance Software Technologies GmbH
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
package com.xceptance.xlt.agentcontroller;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.xceptance.common.lang.ParseNumbers;

/**
 * Helper methods to parse HTTP headers involved when performing partial GET requests.
 */
class PartialGetUtils
{
    /**
     * A pattern to validate a Range request header (for example, <code>bytes=1000-1999</code>) and extract values from
     * it.
     */
    private static final Pattern RANGE_PATTERN = Pattern.compile("bytes=(\\d+)-(\\d+)");

    /**
     * A pattern to validate a Content-Range response header (for example, <code>bytes 1000-1999/12345</code>) and
     * extract values from it.
     */
    private static final Pattern CONTENT_RANGE_PATTERN = Pattern.compile("bytes (\\d+)-(\\d+)/(\\d+)");

    /**
     * The values passed in a Range request header value.
     */
    static class RangeHeaderData
    {
        /** The start position of the requested part. **/
        public final long startPos;

        /** The end position (inclusive) of the requested part. **/
        public final long endPos;

        public RangeHeaderData(final long startPos, final long endPos)
        {
            this.startPos = startPos;
            this.endPos = endPos;
        }
    }

    /**
     * The values passed in a Content-Range response header value.
     */
    static class ContentRangeHeaderData
    {
        /** The start position of the returned part. */
        public final long startPos;

        /** The end position (inclusive) of the returned part. */
        public final long endPos;

        /** The total size of the resource. */
        public final long totalBytes;

        public ContentRangeHeaderData(final long startPos, final long endPos, final long totalBytes)
        {
            this.startPos = startPos;
            this.endPos = endPos;
            this.totalBytes = totalBytes;
        }
    }

    /**
     * Formats the given values as a valid Range request header value.
     *
     * @param startPos
     *            the start position of the requested part
     * @param endPos
     *            the end position (inclusive) of the requested part
     * @return the formatted header value
     */
    static String formatRangeHeader(final long startPos, final long endPos)
    {
        return "bytes=" + startPos + "-" + endPos;
    }

    /**
     * Parses the given header value as a Range header and returns the extracted data.
     *
     * @param rangeHeaderValue
     *            the header value to parse
     * @return the extracted data if the header could be parsed successfully, <code>null</code> otherwise
     */
    static RangeHeaderData parseRangeHeader(final String rangeHeaderValue)
    {
        if (rangeHeaderValue != null)
        {
            final Matcher matcher = RANGE_PATTERN.matcher(rangeHeaderValue);
            if (matcher.matches())
            {
                final long startPos = ParseNumbers.parseLong(matcher.group(1));
                final long endPos = ParseNumbers.parseLong(matcher.group(2));

                return new RangeHeaderData(startPos, endPos);
            }
        }

        return null;
    }

    /**
     * Formats the given values as a valid Content-Range response header value.
     *
     * @param startPos
     *            the start position of the returned part
     * @param endPos
     *            the end position (inclusive) of the returned part
     * @param totalBytes
     *            the total size of the resource
     * @return the formatted header value
     */
    static String formatContentRangeHeader(final long startPos, final long endPos, final long totalBytes)
    {
        return "bytes " + startPos + "-" + endPos + "/" + totalBytes;
    }

    /**
     * Parses the given header value as a Content-Range header and returns the extracted data.
     *
     * @param contentRangeHeaderValue
     *            the header value to parse
     * @return the extracted data if the header could be parsed successfully, <code>null</code> otherwise
     */
    static ContentRangeHeaderData parseContentRangeHeader(final String contentRangeHeaderValue)
    {
        if (contentRangeHeaderValue != null)
        {
            final Matcher matcher = CONTENT_RANGE_PATTERN.matcher(contentRangeHeaderValue);
            if (matcher.matches())
            {
                final long startPos = ParseNumbers.parseLong(matcher.group(1));
                final long endPos = ParseNumbers.parseLong(matcher.group(2));
                final long totalBytes = ParseNumbers.parseLong(matcher.group(3));

                return new ContentRangeHeaderData(startPos, endPos, totalBytes);
            }
        }

        return null;
    }
}
