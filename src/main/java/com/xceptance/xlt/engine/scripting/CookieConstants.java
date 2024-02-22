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
package com.xceptance.xlt.engine.scripting;

import java.util.regex.Pattern;

/**
 * Constants used for cookie validation.
 * 
 * @author Hartmut Arlt (Xceptance Software Technologies GmbH)
 */
public interface CookieConstants
{
    /**
     * Character allowed in cookie name or unquoted cookie value.
     */
    public static final String TOKEN_CHAR = "[^\\s={}<>\\[\\](),\"/?@:;]";

    /**
     * Cookie name format.
     */
    public static final String KEY = TOKEN_CHAR + "+"; // at least 1 token char

    /**
     * Unquoted cookie value format.
     */
    public static final String VALUE_TOKEN = TOKEN_CHAR + "*"; // any amount of token chars

    /**
     * Quoted cookie value format.
     */
    public static final String VALUE_QUOTED_STRING = "(\".*\")?"; // quoted string or nothing

    /**
     * Cookie value format (quoted or unquoted).
     */
    public static final String VALUE = "(" + VALUE_TOKEN + "|" + VALUE_QUOTED_STRING + ")"; // token OR quoted string

    /**
     * Cookie name/value pattern.
     */
    public static final Pattern NAME_VALUE_PAIR_PATTERN = Pattern.compile("^(" + KEY + ")\\s*=" + VALUE + "$");

    /**
     * Cookie name pattern.
     */
    public static final Pattern NAME_PATTERN = Pattern.compile("^" + KEY + "$");

    /**
     * Max-age format used in options string.
     */
    public static final Pattern MAX_AGE_PATTERN = Pattern.compile("max_age=(\\d+)");

    /**
     * Path format used in options string.
     */
    public static final Pattern PATH_PATTERN = Pattern.compile("path=([^\\s,]+)[,]?");
}
