/*
 * Copyright (c) 2005-2023 Xceptance Software Technologies GmbH
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
package com.xceptance.common.lang;

/**
 * A class the supports special ways to hash a string to improve the overall performance and
 * reduce cache misses.
 * 
 * @author rschwietzke
 *
 */
public class StringHasher
{
    /**
     * Hashes the characters up to the limiter excluding it. If there is no limiter, this
     * result matches the hashcode of a similar string.
     * 
     * @param s the sequence of characters to hash up to the limiter
     * @return the hashcode
     */
    public static int hashCodeWithLimit(final CharSequence s, final char limitingChar)
    {
        int hash = 0;

        final int length = s.length();
        for (int i = 0; i < length; i++) 
        {
            final char c = s.charAt(i);

            if (c != limitingChar)
            {
                final int h1 = hash << 5;
                final int h2 = c - hash;
                hash = h1 + h2;
            }
            else
            {
                break;
            }

        }

        return hash;
    }
}
