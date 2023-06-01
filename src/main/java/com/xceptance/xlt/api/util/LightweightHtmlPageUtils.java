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
package com.xceptance.xlt.api.util;

import java.util.List;

import com.xceptance.xlt.engine.util.LWPageUtilities;

/**
 * The LightweightHtmlPageUtils class provides some convenience methods for dealing with unparsed HTML pages, i.e. with
 * strings containing the page's HTML source.
 * 
 * @author Jörg Werner (Xceptance Software Technologies GmbH)
 */
public class LightweightHtmlPageUtils extends BasicPageUtils
{
    /**
     * Returns the values of the "href" attribute of all "a" tags on the page as a list of strings.
     * 
     * @param page
     *            the page source
     * @return the href attribute values
     */
    public static List<String> getAllAnchorLinks(final String page)
    {
        return LWPageUtilities.getAllAnchorLinks(page);
    }

    /**
     * Returns the values of the "src" attribute of all "img" tags on the page as a list of strings.
     * 
     * @param page
     *            the page source
     * @return the src attribute values
     */
    public static List<String> getAllImageLinks(final String page)
    {
        return LWPageUtilities.getAllImageLinks(page);
    }

    /**
     * Returns the values of the "href" attribute of all "link" tags on the page as a list of strings.
     * 
     * @param page
     *            the page source
     * @return the href attribute values
     */
    public static List<String> getAllLinkLinks(final String page)
    {
        return LWPageUtilities.getAllLinkLinks(page);
    }

    /**
     * Returns the values of the "src" attribute of all "script" tags on the page as a list of strings.
     * 
     * @param page
     *            the page source
     * @return the src attribute values
     */
    public static List<String> getAllScriptLinks(final String page)
    {
        return LWPageUtilities.getAllScriptLinks(page);
    }
}
