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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;

import org.apache.commons.lang3.StringUtils;
import org.htmlunit.WebResponse;
import org.htmlunit.util.UrlUtils;

import com.xceptance.common.util.RegExUtils;

/**
 * Utility class for CSS-handling.
 * 
 * @author Hartmut Arlt (Xceptance Software Technologies GmbH)
 */
public final class CssUtils
{

    /**
     * Pattern used to determine the file names of imported style sheets using the <code>&#064;import</code> rule.
     * <p style="color:red">
     * This pattern has to be applied in conjunction with <code>URL_PATTERN</code> to cover all import rules.
     * </p>
     */
    private static final String IMPORT_RULE_PATTERN = "(?m)@import\\s+(['\"])([^'\"]+?)\\1.*?;";

    /**
     * Pattern used to determine referenced resources.
     */
    private static final String URL_PATTERN = "url\\(\\s*(?:(['\"])([^'\")]+?)\\1|([^'\")]+?))\\s*\\)";

    /**
     * Pattern used to determine the file names of imported style sheets using the <code>&#064;import</code> pattern.
     */
    private static final String IMPORT_ONLY_RULE_PATTERN = "(?m)@import\\s+(?:" + URL_PATTERN + "|(['\"])([^'\"]+?)\\4).*?;";

    /**
     * Pattern used to clear CSS content of <code>&#064;namespace</code> rules.
     */
    private static final String CSS_NAMESPACE_PATTERN = "(?m)@namespace\\s+url\\([^)]+?\\)\\s*[;}]";

    /**
     * Pattern used to clear CSS content of comments.
     */
    private static final String COMMENT_PATTERN = "(?sm)/\\*.*?\\*/";

    /**
     * Returns the URLs of all resources referenced in the given CSS content. Each resource URL is resolved against the
     * given base URL.
     * 
     * @param cssContent
     *            CSS content
     * @param baseUrl
     *            base URL
     * @return resource URLs
     */
    public static List<URL> getResourceUrls(final String cssContent, final String baseUrl)
    {
        try
        {
            return getResourceUrls(cssContent, new URL(baseUrl));
        }
        catch (final MalformedURLException e)
        {
            return Collections.emptyList();
        }
    }

    /**
     * Returns the URL strings of all resources referenced by the given CSS content as encountered in source.
     * 
     * @param cssContent
     *            CSS content
     * @return resource URL strings
     */
    public static Set<String> getUrlStrings(final String cssContent)
    {
        // shortcut
        if (StringUtils.isEmpty(cssContent))
        {
            return Collections.emptySet();
        }

        // use a hash set to avoid adding the same URL multiple times
        final Set<String> urls = new HashSet<String>();
        // clear content
        final String cleanContent = cleanCSS(cssContent);
        // get all matches for url pattern

        final List<String> resources = new ArrayList<String>();
        final Matcher m = RegExUtils.getPattern(URL_PATTERN).matcher(cleanContent);
        while (m.find())
        {
            final int groupCount = m.groupCount();
            String match = m.group(groupCount);
            if (match == null)
            {
                match = m.group(groupCount - 1);
            }
            resources.add(match);
        }

        // note, that import rules, which use the url pattern, don't have to be
        // treated here, since they were handled by the regex above
        resources.addAll(RegExUtils.getAllMatches(cleanContent, IMPORT_RULE_PATTERN, 2));

        // process all resource URL strings and add them to the set if they are
        // non-blank
        for (final String s : resources)
        {
            final String match = s.trim();
            if (match.length() > 0)
            {
                urls.add(match);
            }
        }

        return urls;
    }

    /**
     * Returns the URLs of all resources referenced in the given CSS content. Each resource URL is resolved against the
     * given base URL.
     * 
     * @param cssContent
     *            CSS content
     * @param baseUrl
     *            base URL
     * @return resource URLs
     */
    public static List<URL> getResourceUrls(final String cssContent, final URL baseUrl)
    {
        return createAbsoluteUrls(getUrlStrings(cssContent), baseUrl);
    }

    /**
     * Returns the URLs of all resources referenced by the content of the given response.
     * 
     * @param response
     *            response which is assumed to encapsulate CSS content
     * @return resource URLs of encapsulated CSS content
     */
    public static List<URL> getResourceUrls(final WebResponse response)
    {
        return getResourceUrls(response, response.getWebRequest().getUrl());
    }

    /**
     * Returns the URLs of all resources referenced by the content of the given response. All URLs are resolved using
     * the given base URL.
     * 
     * @param response
     *            response which is assumed to encapsulate CSS content
     * @param baseURL
     *            base URL to be used for URL resolving
     * @return resource URLs of encapsulated CSS content
     */
    public static List<URL> getResourceUrls(final WebResponse response, final URL baseURL)
    {
        if (isCssResponse(response))
        {
            return getResourceUrls(response.getContentAsString(), baseURL);
        }

        return Collections.emptyList();
    }

    /**
     * Returns all import URLs for the given web response.
     * 
     * @param response
     *            the web response
     * @return list of import URLs
     */
    public static List<URL> getImportUrls(final WebResponse response)
    {
        if (isCssResponse(response))
        {
            return getImportUrls(response.getContentAsString(), response.getWebRequest().getUrl());
        }

        return Collections.emptyList();
    }

    /**
     * Returns all import URLs of the given CSS string after they're resolved using the given base URL.
     * 
     * @param cssContent
     *            CSS content
     * @param baseURL
     *            base URL
     * @return list of import URLs
     */
    public static List<URL> getImportUrls(final String cssContent, final URL baseURL)
    {
        return createAbsoluteUrls(getRelativeImportUrlStrings(cssContent), baseURL);
    }

    /**
     * Returns all relative import URL strings contained in the given CSS string.
     * 
     * @param cssContent
     *            CSS content
     * @return list of relative import URL strings
     */
    public static Set<String> getRelativeImportUrlStrings(final String cssContent)
    {
        if (StringUtils.isEmpty(cssContent))
        {
            return Collections.emptySet();
        }

        // use a hash set to avoid adding the same URL multiple times
        final String cleanContent = cleanCSS(cssContent);
        final Set<String> urls = new HashSet<String>();
        final Matcher m = RegExUtils.getPattern(IMPORT_ONLY_RULE_PATTERN).matcher(cleanContent);
        while (m.find())
        {
            String match = null;
            for (int group = m.groupCount(); group > 0 && match == null; group--)
            {
                match = m.group(group);
            }

            if (match != null)
            {
                match = match.trim();
                if (match.length() > 0)
                {
                    urls.add(match);
                }
            }
        }

        return urls;
    }

    /**
     * Determines if the given response encapsulates CSS content.
     * 
     * @param response
     *            response to check
     * @return <tt>true</tt> if the given response encapsulates CSS content, <tt>false</tt> otherwise
     */
    public static boolean isCssResponse(final WebResponse response)
    {
        if (response != null)
        {
            final URL url = response.getWebRequest().getUrl();
            if (url != null)
            {
                if (url.getPath().toLowerCase().endsWith(".css"))
                {
                    return true;
                }
            }

            if (response.getContentType() != null)
            {
                if (response.getContentType().trim().equalsIgnoreCase("text/css"))
                {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Clears the given CSS content string of all import rules.
     * 
     * @param cssContent
     *            CSS content to be cleared
     * @return cleared CSS content
     */
    public static String clearImportRules(final String cssContent)
    {
        return RegExUtils.replaceAll(cssContent, IMPORT_ONLY_RULE_PATTERN, "");
    }

    /**
     * Clears the given CSS response of all import rules.
     * 
     * @param response
     *            CSS web response
     * @return cleared CSS content
     */
    public static String clearImportRules(final WebResponse response)
    {
        if (isCssResponse(response))
        {
            return clearImportRules(response.getContentAsString());
        }

        return "";
    }

    /**
     * Creates a list of absolute URLs from the given list of relative URL strings. The relative URL strings are made
     * absolute using the specified base URL.
     * 
     * @param relativeUrls
     *            the list of relative URL strings
     * @param baseUrl
     *            the base URL
     * @return the list of absolute URLs
     */
    private static List<URL> createAbsoluteUrls(final Collection<String> relativeUrls, final URL baseUrl)
    {
        if (baseUrl == null || relativeUrls.isEmpty())
        {
            return Collections.emptyList();
        }

        // create absolute URLs from the relative ones
        final List<URL> urls = new ArrayList<URL>(relativeUrls.size());
        for (final String urlString : relativeUrls)
        {
            final URL url = resolveUrl(baseUrl, urlString);
            if (url != null)
            {
                urls.add(url);
            }
        }

        return urls;
    }

    /**
     * Helper method which resolves the given relative URL string against the given base URL.
     * 
     * @param baseUrl
     *            base URL
     * @param relativeUrl
     *            relative URL string
     * @return resolved URL
     */
    private static URL resolveUrl(final URL baseUrl, final String relativeUrl)
    {
        try
        {
            return new URL(UrlUtils.resolveUrl(baseUrl, relativeUrl));
        }
        catch (final MalformedURLException mfe)
        {
            return null;
        }
    }

    /**
     * Cleans the given CSS content.
     * 
     * @param cssContent
     *            CSS content to be cleaned.
     * @return given CSS content where all namespace rules and comments have been removed
     */
    private static String cleanCSS(final String cssContent)
    {
        if (cssContent == null)
        {
            return null;
        }

        String cleanCSS = RegExUtils.replaceAll(cssContent, CSS_NAMESPACE_PATTERN, "");
        cleanCSS = RegExUtils.replaceAll(cleanCSS, COMMENT_PATTERN, "");

        return cleanCSS;
    }

    /**
     * Private constructor to avoid object instantiation.
     */
    private CssUtils()
    {
    }
}
