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
package com.xceptance.xlt.engine.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.xceptance.common.util.RegExUtils;

/**
 * Melting pot for all lightweight page utilities needed across the XLT engine.
 * <p>
 * All methods for parsing URLs return raw strings (as encountered in source) and might contain HTML entity references.
 * <p>
 * 
 * @author Hartmut Arlt (Xceptance Software Technologies GmbH)
 */
public final class LWPageUtilities
{
    /**
     * A regex pattern that matches the value of the "src" attribute of all "script" tags on the page.
     */
    private static final String SCRIPT_PATTERN = "<script\\b[^<>]*?\\ssrc=\\s*?[\"']([^\"']+?)[\"']";

    /**
     * A regex pattern that matches the value of the "href" attribute of all "link" tags on the page.
     */
    private static final String LINK_PATTERN = "<link\\b[^<>]*?\\shref=\\s*?[\"']([^\"']+?)[\"']";

    /**
     * A regex pattern that matches the attribute definitions of all HTML link tags on a given page.
     */
    private static final String LINK_ATTRIBUTE_PATTERN = "<link\\b\\s([^<>]+?)>";

    /**
     * A regex pattern that matches the value of the "src" attribute of all "img" tags on the page.
     */
    private static final String IMG_PATTERN = "<img\\b[^<>]*?\\ssrc=\\s*?[\"']([^\"']+?)[\"']";

    /**
     * A regex pattern that matches the value of the "src" attribute of all image "input" tags on the page
     * (type="image").
     */
    private static final String IMAGE_INPUT_PATTERN = "<input\\b[^<>]*?\\ssrc=\"([^\"]+?)\"";

    /**
     * A regex pattern that matches the value of the "href" attribute of all "a" tags on the page.
     */
    private static final String ANCHOR_PATTERN = "<a\\b[^<>]*?\\shref=\\s*?[\"']([^\"']+?)[\"']";

    /**
     * A regex pattern that matches the value of the "style" attribute of all tags on the page.
     */
    private static final String INLINE_CSS_PATTERN = "<[A-Za-z]+?\\b[^<>]*?\\sstyle=\"([^\"]+?)\"";

    /**
     * A regex pattern that matches the value of the "href" attribute of all "base" tags on the page.
     */
    private static final String BASE_PATTERN = "<base\\b[^<>]*?\\shref=\"([^\"]+?)\"";

    /**
     * A regex pattern that matches the attributes of all "(i)frame" tags on the page.
     */
    private static final String FRAME_PATTERN = "<i?frame\\s([^<>]+?)>";

    /**
     * A regex pattern that matches the content snippet enclosed in a HTML style tag.
     */
    private static final String STYLE_CONTENT_PATTERN = "<style\\b[^>]*?>((?!</style).*?)</style>";

    /**
     * A regex pattern that matches any "script" tag on the page.
     */
    private static final String SCRIPT_TAG_PATTERN = "<script\\b.*?(/>|</script>)";

    /**
     * A regex pattern that matches any "base" tag on the page.
     */
    private static final String BASE_TAG_PATTERN = "<base\\b.*?(></base>|/>|>)";

    /**
     * A regex that matches any HTML comment on the page, including unclosed ones.
     */
    private static final String HTML_COMMENT_PATTERN = "(?-m)<!--.*?(-->|$)";

    /**
     * Default constructor. Declared private to prevent external instantiation.
     */
    private LWPageUtilities()
    {
    }

    /**
     * Returns the values of the "style" attribute of all tags on the page as a list of strings.
     * 
     * @param page
     *            the page source
     * @return the style attribute values
     */
    public static List<String> getAllInlineCssStatements(final String page)
    {
        return getAllMatches(page, LWPageUtilities.INLINE_CSS_PATTERN, 1);
    }

    /**
     * Returns the values of the "href" attribute of all "base" tags on the page as a list of strings.
     * 
     * @param page
     *            the page source
     * @return the href attribute values
     */
    public static List<String> getAllBaseLinks(final String page)
    {
        return getAllMatches(page, LWPageUtilities.BASE_PATTERN, 1);
    }

    /**
     * Returns all inlined CSS resource URLs found in the given page.
     * 
     * @param page
     *            the page source
     * @return inlined CSS resource URLs
     */
    public static Set<String> getAllInlineCssResourceUrls(final String page)
    {
        return CssUtils.getUrlStrings(StringUtils.join(getAllInlineCssStatements(page), " ") +
                                              StringUtils.join(getAllStyleContents(page), " "));
    }

    /**
     * Returns the values of the "href" attribute of all "a" tags on the page as a list of strings.
     * 
     * @param page
     *            the page source
     * @return the href attribute values
     */
    public static List<String> getAllAnchorLinks(final String page)
    {
        return getAllMatches(page, ANCHOR_PATTERN, 1);
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
        return getAllMatches(page, IMG_PATTERN, 1);
    }

    /**
     * Returns the values of the "src" attribute of all image "input" tags on the page as a list of strings.
     * 
     * @param page
     *            the page source
     * @return the src attribute values
     */
    public static List<String> getAllImageInputLinks(final String page)
    {
        return getAllMatches(page, IMAGE_INPUT_PATTERN, 1);
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
        return getAllMatches(page, LINK_PATTERN, 1);
    }

    /**
     * Returns the attribute definitions of all HTML link tags on the given page as a list of strings.
     * 
     * @param page
     *            the page source
     * @return attribute definitions of all HTML link tags
     */
    public static List<String> getAllLinkAttributes(final String page)
    {
        return getAllMatches(page, LINK_ATTRIBUTE_PATTERN, 1);
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
        return getAllMatches(page, SCRIPT_PATTERN, 1);
    }

    /**
     * Returns all attributes contained in a (i)frame tag.
     * 
     * @param page
     *            page content
     * @return list of all frame attributes
     */
    public static List<String> getAllFrameAttributes(final String page)
    {
        return getAllMatches(page, FRAME_PATTERN, 1);
    }

    /**
     * Returns the value of the given attribute.
     * 
     * @param attributes
     *            attribute definitions as one string
     * @param attributeName
     *            name of attribute
     * @return value of attribute
     */
    public static String getAttributeValue(final String attributes, final String attributeName)
    {
        if (attributes == null || attributeName == null || attributes.length() == 0 || attributeName.length() == 0)
        {
            return null;
        }

        final String attributeValue = RegExUtils.getFirstMatch(attributes, "(?i)" + attributeName + "=\"([^\"]+)\"", 1);
        return (attributeValue != null) ? attributeValue.trim() : attributeValue;
    }

    /**
     * Returns a list of all content snippets that are enclosed in a HTML style tag.
     * 
     * @param page
     *            the page source
     * @return list of HTML style element content snippets
     */
    public static List<String> getAllStyleContents(final String page)
    {
        return getAllMatches(page, STYLE_CONTENT_PATTERN, 1);
    }

    /**
     * Removes all "base" tags found on the given page.
     * 
     * @param page
     *            the page source
     * @return modified page source after all "base" tags have been removed
     */
    public static String removeAllBaseTags(final String page)
    {
        return removeAllTags(page, BASE_TAG_PATTERN);
    }

    /**
     * Removes all "script" tags found on the given page.
     * 
     * @param page
     *            the page source
     * @return modified page source after all "script" tags have been removed
     */
    public static String removeAllScriptTags(final String page)
    {
        return removeAllTags(page, SCRIPT_TAG_PATTERN);
    }

    /**
     * Removes all tags found on the given page that match the given pattern string.
     * 
     * @param page
     *            the page source
     * @param pattern
     *            pattern string used to identify the tags to be removed
     * @return modified page source after all tags that match the given pattern string have been removed
     */
    private static String removeAllTags(final String page, final String pattern)
    {
        if (page == null || page.length() == 0)
        {
            return null;
        }

        return RegExUtils.replaceAll(page, "(?i)" + pattern, "");
    }

    /**
     * Returns all matches for the given pattern found in the given page source.
     * 
     * @param page
     *            the page source
     * @param pattern
     *            the pattern
     * @param group
     *            the group to read from the match
     * @return the matches
     */
    private static List<String> getAllMatches(final String page, final String pattern, final int group)
    {
        if (page == null || page.length() == 0)
        {
            return Collections.emptyList();
        }

        final List<String> trimmedMatches = new ArrayList<String>();
        final List<String> matches = RegExUtils.getAllMatches(page, "(?is)" + pattern, group);

        for (int i = 0; i < matches.size(); i++)
        {
            final String s = matches.get(i);

            final String trimmed = s.trim();
            if (trimmed.length() > 0)
            {
                trimmedMatches.add(trimmed);
            }
        }

        return trimmedMatches;
    }

    /**
     * Removes any HTML comment found on the given page.
     * 
     * @param page
     *            the page source
     * @return modified page source after all HTML comments have been removed
     */
    public static String removeHtmlComments(final String page)
    {
        return RegExUtils.removeAll(page, HTML_COMMENT_PATTERN);
    }
}
