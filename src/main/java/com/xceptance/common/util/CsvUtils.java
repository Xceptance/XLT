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

import java.util.ArrayList;
import java.util.List;

/**
 * The {@link CsvUtils} class provides helper methods to encode and decode values to/from the CSV format. Note that we
 * define the "C" in "CSV" to stand for "comma", so other characters are not allowed as field separator.
 */
public final class CsvUtils
{
    /**
     * Character constant representing a comma.
     */
    private static final char COMMA = ',';

    /**
     * Character constant representing a double quote.
     */
    private static final char QUOTE_CHAR = '"';

    /**
     * Character constant representing a line feed.
     */
    private static final char LF = '\n';

    /**
     * Character constant representing a carriage return.
     */
    private static final char CR = '\r';

    /**
     * Default constructor. Declared private to prevent external instantiation.
     */
    private CsvUtils()
    {
    }

    /**
     * Decodes the given CSV-encoded data record and returns the plain unquoted fields.
     * 
     * @param s
     *            the CSV-encoded data record
     * @return the plain fields
     */
    public static String[] decode(final String s)
    {
        return decode(s, COMMA);
    }

    /**
     * Decodes one field of the data record.
     * 
     * @param s
     *            the encoded field
     * @return the plain field
     */
    public static String decodeField(final String s)
    {
        if (s == null)
        {
            return s;
        }

        if (s.length() < 2)
        {
            return s;
        }

        final int length = s.length();

        if (s.charAt(0) != QUOTE_CHAR || s.charAt(length - 1) != QUOTE_CHAR)
        {
            return s;
        }
        
        // source and target
        final char[] buffer = s.toCharArray();

        // iterate from second up to second last character
        int target = 0;

        for (int src = 1; src < length - 1; src++)
        {
            final char c = buffer[src];

            if (c == QUOTE_CHAR)
            {
                // the next character must be a quote character as well
                src++;

                if (src >= length - 1 || buffer[src] != QUOTE_CHAR)
                {
                    throw new IllegalArgumentException("Parameter '" + s + "' is not properly CSV-encoded.");
                }
            }
            
            buffer[target] = c;
            target++;
        }

        return new String(buffer, 0, target);
    }

    /**
     * Encodes the given fields to a CSV-encoded data record.
     * 
     * @param fields
     *            the plain fields
     * @return the CSV-encoded data record
     */
    public static String encode(final String[] fields)
    {
        return encode(fields, COMMA);
    }

    /**
     * Encodes one field of the data record.
     * 
     * @param s
     *            the plain field
     * @return the encoded field
     */
    public static String encodeField(final String s)
    {
        return encodeField(s, COMMA);
    }

    /**
     * Encodes one field of the data record.
     * 
     * @param s
     *            the plain field
     * @param fieldSeparator
     *            the field separator character
     * @return the encoded field
     */
    public static String encodeField(final String s, final char fieldSeparator)
    {
        if (s == null || s.length() == 0)
        {
            return s;
        }

        final char[] sourceChars = s.toCharArray();
        final int sourceLength = sourceChars.length;

        // check whether we have to quote at all
        boolean needsQuoting = false;
        int quotesRead = 0;
        for (int i = 0; i < sourceLength; i++)
        {
            final char c = sourceChars[i];
            if (c == QUOTE_CHAR)
            {
                quotesRead++;
            }

            if (!needsQuoting && needsQuote(c, fieldSeparator))
            {
                needsQuoting = true;
            }
        }

        if (!needsQuoting)
        {
            return s;
        }

        // quote
        final char[] targetChars = new char[sourceLength + quotesRead + 2];

        int j = 1;
        for (int i = 0; i < sourceLength; i++, j++)
        {
            final char c = sourceChars[i];

            if (c == QUOTE_CHAR)
            {
                // add another quote
                targetChars[j++] = c;
            }

            targetChars[j] = c;
        }

        targetChars[0] = QUOTE_CHAR;
        targetChars[j] = QUOTE_CHAR;

        return new String(targetChars);
    }

    /**
     * Decodes the given fields from a CSV-encoded data record using the given field separator.
     * 
     * @param s
     *            the CSV record
     * @param fieldSeparator
     *            the field separator to use
     * @return the decoded array of Strings
     */
    public static String[] decode(final String s, final char fieldSeparator)
    {
        ParameterCheckUtils.isNotNull(s, "s");

        final String[] fields = split(s, fieldSeparator);

        final int length = fields.length;
        for (int i = 0; i < length; i++)
        {
            fields[i] = decodeField(fields[i]);
        }

        return fields;
    }

    /**
     * Encodes the given fields to a CSV-encoded data record.
     * 
     * @param fields
     *            the plain fields
     * @param fieldSeparator
     *            the field separator to use
     * @return the CSV-encoded data record
     */
    public static String encode(final String[] fields, final char fieldSeparator)
    {
        // parameter validation
        ParameterCheckUtils.isNotNullOrEmpty(fields, "fields");

        final StringBuilder result = new StringBuilder(256);
        boolean isFirstField = true;

        for (String field : fields)
        {
            if (field == null)
            {
                throw new IllegalArgumentException("Array entry must not be null.");
            }

            // first append the separator except for the first entry
            if (isFirstField)
            {
                isFirstField = false;
            }
            else
            {
                result.append(fieldSeparator);
            }

            // now add the encoded field
            field = encodeField(field, fieldSeparator);
            result.append(field);
        }

        return result.toString();
    }

    /**
     * Splits the given string into parts at field boundaries. Field separators occurring inside quoted fields are
     * ignored.
     * 
     * @param s
     *            the encoded data record
     * @param fieldSeparator
     *            the field separator used
     * @return the encoded fields
     */
    private static String[] split(final String s, final char fieldSeparator)
    {
        int length = s.length();
        if (length == 0)
        {
            return new String[]{""};
        }

        final List<String> fields = new ArrayList<>(32);

        int beginIndex = 0;
        boolean insideQuotes = false;
        
        final char[] chars = s.toCharArray();
        for (int i = 0; i < length; i++)
        {
            final char c = chars[i];

            if (c == fieldSeparator)
            {
                if (insideQuotes == false)
                {
                    fields.add(s.substring(beginIndex, i));
                    beginIndex = i + 1;
                }
            }
            else if (c == QUOTE_CHAR)
            {
                insideQuotes = !insideQuotes;
            }
        }

        // add the last field
        fields.add(s.substring(beginIndex));

        // return a sub array with only the fields found
        // the new array for type safety is stack local and cheaper
        return fields.toArray(new String[0]);
    }

    /**
     * Determines whether or not the given character needs to be quoted.
     * 
     * @param c
     *            character to be checked
     * @param separatorChar
     *            the field separator character
     * @return <code>true</code> if the given character needs to be quoted, <code>false</code> otherwise
     */
    private static boolean needsQuote(final char c, final char separatorChar)
    {
        return c == QUOTE_CHAR || c == LF || c == CR || c == separatorChar;
    }
}
