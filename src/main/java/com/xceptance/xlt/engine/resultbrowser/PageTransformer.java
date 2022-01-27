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
package com.xceptance.xlt.engine.resultbrowser;

import java.net.URL;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.gargoylesoftware.htmlunit.WebResponse;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlStyle;
import com.gargoylesoftware.htmlunit.util.UrlUtils;
import com.xceptance.common.util.RegExUtils;
import com.xceptance.common.xml.DomUtils;
import com.xceptance.xlt.api.htmlunit.LightWeightPage;
import com.xceptance.xlt.common.XltConstants;
import com.xceptance.xlt.engine.LightWeightPageImpl;
import com.xceptance.xlt.engine.XltHttpWebConnection;
import com.xceptance.xlt.engine.XltWebClient;
import com.xceptance.xlt.engine.util.CssUtils;
import com.xceptance.xlt.engine.util.LWPageUtilities;
import com.xceptance.xlt.engine.util.TimerUtils;
import com.xceptance.xlt.engine.util.URLCleaner;

/**
 * Transforms a given page for local storage.
 *
 * @author Hartmut Arlt (Xceptance Software Technologies GmbH)
 */
final class PageTransformer
{
    /**
     * Class logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(PageTransformer.class);

    /**
     * The HTML page to transform.
     */
    private final PageDOMClone htmlPage;

    /**
     * The lightweight page to transform.
     */
    private final LightWeightPage lwPage;

    /**
     * Whether or not this (HTML or lightweight) page is the outermost page.
     */
    private final boolean outermostPage;

    /**
     * Creates a new transformer for the given HTML page.
     *
     * @param page
     *            the HTML page to transform
     * @param outermost
     *            whether or not the given HTML page is the outermost page
     */
    PageTransformer(final PageDOMClone page, final boolean outermost)
    {
        htmlPage = page;
        lwPage = null;
        outermostPage = outermost;
    }

    /**
     * Creates a new transformer for the given lightweight page.
     *
     * @param page
     *            the lightweight page to transform
     */
    PageTransformer(final LightWeightPage page)
    {
        lwPage = page;
        htmlPage = null;
        outermostPage = ((LightWeightPageImpl) page).getEnclosingPage() == null;
    }

    /**
     * Rewrites the passed HTML page so that it can be viewed in the page browser. First, the {@link HtmlPage} is cloned
     * to a standard {@link Document}, so that the original page remains untouched. Afterwards, the following changes
     * are made to the cloned document:
     * <ul>
     * <li>any base tag is removed</li>
     * <li>all script tags are removed</li>
     * <li>the URLs in frame/iframe/img/link tags are rewritten to point to the fragment cache</li>
     * </ul>
     * These measures ensure that the page, when viewed in the page browser, is completely served from disk and does not
     * change through scripting.
     *
     * @param mapping
     *            the URL mapping to be used for URL rewriting
     * @return a clone of the passed HTML page with the above modifications done
     */
    Document transform(final UrlMapping mapping)
    {
        final long start = TimerUtils.getTime();

        final Document document = htmlPage.getDocument();

        removeJSHandlerAttrs(document.getElementsByTagName("body").item(0));

        // remove any base and script tag
        DomUtils.removeElementsByTagName(document, "script");
        DomUtils.removeElementsByTagName(document, "base");

        // check whether we have a head tag
        // (HtmlUnit does not automatically add one if missing in the HTML code)
        Element head = (Element) document.getElementsByTagName("head").item(0);
        if (head == null)
        {
            // no, create an empty head tag
            head = document.createElement("head");
            Element html = document.getDocumentElement();
            if (html == null)
            {
                html = document.createElement("html");
                document.appendChild(html);
            }
            html.insertBefore(head, html.getFirstChild());
        }
        else
        {
            // yes, remove any existing content-type meta tag
            final NodeList metaTags = head.getElementsByTagName("meta");
            for (int i = 0; i < metaTags.getLength(); i++)
            {
                final Element metaTag = (Element) metaTags.item(i);
                final NamedNodeMap attributes = metaTag.getAttributes();

                for (int j = 0; j < attributes.getLength(); j++)
                {
                    final Attr attribute = (Attr) attributes.item(j);
                    if (attribute.getName().equalsIgnoreCase("http-equiv") && attribute.getValue().equalsIgnoreCase("content-type"))
                    {
                        metaTag.getParentNode().removeChild(metaTag);
                    }
                }
            }
        }

        // add a new content type meta tag with UTF-8 as the character set
        final Element meta = document.createElement("meta");
        meta.setAttribute("http-equiv", "content-type");
        meta.setAttribute("content", htmlPage.getResponse().getContentType() + "; charset=utf-8");

        head.insertBefore(meta, head.getFirstChild());

        final long urlRewriteStart = TimerUtils.getTime();

        // rewrite the URLs of images and CSS files to point to the
        // local cache
        rewriteUrls(document, "img", "src", mapping);
        rewriteUrls(document, "input", "src", mapping);
        rewriteUrls(document, "link", "href", mapping);
        rewriteInlineCssUrls(document, mapping);

        final long end = TimerUtils.getTime();

        if (LOGGER.isDebugEnabled())
        {
            LOGGER.debug("Transformation took: " + (end - start) + "ms [URL-Rewriting: " + (end - urlRewriteStart) + "ms].");
        }

        // we are done with the document
        return document;
    }

    /**
     * Rewrites all URLs in the given document.
     * <p>
     * The URL rewriting is restricted to the values of the given attribute of all tags with the given name.
     * </p>
     *
     * @param document
     *            the document
     * @param tagName
     *            name of the tag
     * @param attributeName
     *            name of the attribute
     * @param mapping
     *            URL mapping to use
     */
    private void rewriteUrls(final Document document, final String tagName, final String attributeName, final UrlMapping mapping)
    {
        final NodeList nodes = document.getElementsByTagName(tagName);
        final String urlPrefix = (outermostPage ? XltConstants.DUMP_CACHE_DIR + "/" : "");
        final Charset charset = htmlPage.getResponse().getContentCharset();
        for (int i = 0; i < nodes.getLength(); i++)
        {
            final Element element = (Element) nodes.item(i);
            final Attr attribute = element.getAttributeNode(attributeName);

            if (attribute != null)
            {
                final String attValue = attribute.getValue();
                final URL u = htmlPage.getFullyQualifiedUrl(attValue);
                if (u != null)
                {
                    final String newUrl = mapping.map(UrlUtils.encodeUrl(u, false, charset));

                    if (newUrl != null)
                    {
                        attribute.setValue(urlPrefix + newUrl);
                    }
                }
            }
        }
    }

    /**
     * Rewrites all relative URLs found in inlined CSS statements.
     *
     * @param doc
     *            root document
     * @param mapping
     *            the URL mapping to be used for URL rewriting
     */
    private void rewriteInlineCssUrls(final Document doc, final UrlMapping mapping)
    {
        final NodeList styleTags = doc.getElementsByTagName(HtmlStyle.TAG_NAME);
        for (int i = 0; i < styleTags.getLength(); i++)
        {
            final Element styleTag = (Element) styleTags.item(i);
            styleTag.setTextContent(rewriteInlineCssUrls(styleTag.getTextContent(), mapping));
        }

        final NodeList childNodes = doc.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++)
        {
            rewriteInlineCssUrls(childNodes.item(i), mapping);
        }
    }

    /**
     * Rewrites all relative URLs found in inlined CSS statements of the DOM subtree rooted at the given DOM node.
     *
     * @param node
     *            DOM node to process
     * @param mapping
     *            the URL mapping to be used for URL rewriting
     */
    private void rewriteInlineCssUrls(final Node node, final UrlMapping mapping)
    {
        final NamedNodeMap attributes = node.getAttributes();
        if (attributes != null)
        {
            final Node styleAttrValNode = attributes.getNamedItem("style");

            if (styleAttrValNode != null)
            {
                styleAttrValNode.setNodeValue(rewriteInlineCssUrls(styleAttrValNode.getTextContent(), mapping));
            }
        }

        final NodeList childNodes = node.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++)
        {
            rewriteInlineCssUrls(childNodes.item(i), mapping);
        }
    }

    /**
     * Rewrites all relative URLs found in the given CSS content.
     *
     * @param cssContent
     *            CSS content
     * @param mapping
     *            the URL mapping to be used for URL rewriting
     * @return rewritten CSS content
     */
    private String rewriteInlineCssUrls(final String cssContent, final UrlMapping mapping)
    {
        final WebResponse response = htmlPage.getResponse();
        final Charset charset = response.getContentCharset();
        String newContent = cssContent;
        final String urlPrefix = (outermostPage ? XltConstants.DUMP_CACHE_DIR + "/" : "");
        for (final String urlString : CssUtils.getUrlStrings(cssContent))
        {
            final URL url = htmlPage.getFullyQualifiedUrl(urlString);
            if (url != null)
            {
                final String newUrl = mapping.map(UrlUtils.encodeUrl(url, false, charset));

                if (newUrl != null)
                {
                    final Pattern p = RegExUtils.getPattern("(['\"\\(])\\s*" + RegExUtils.escape(urlString) + "\\s*(['\"\\)])");

                    final String newUrlPattern = "$1" + urlPrefix + newUrl + "$2";
                    newContent = p.matcher(newContent).replaceAll(newUrlPattern);
                }
            }
        }

        return newContent;
    }

    /**
     * Transforms the enclosed lightweight page and returns it afterwards.
     *
     * @param mapping
     *            the URL mapping used for URL rewriting
     * @return rewritten content
     */
    String transformLW(final UrlMapping mapping)
    {
        final long start = TimerUtils.getTime();

        // parameter validation
        final String pageContent = lwPage.getContent();
        if (pageContent == null || pageContent.length() == 0)
        {
            return pageContent;
        }

        //
        // === collect all relative URLs ===
        //
        final Set<String> urlStrings = new HashSet<String>();

        final String uncommentedPageContent = LWPageUtilities.removeHtmlComments(pageContent);

        urlStrings.addAll(LWPageUtilities.getAllLinkLinks(uncommentedPageContent));
        urlStrings.addAll(LWPageUtilities.getAllImageLinks(uncommentedPageContent));
        urlStrings.addAll(LWPageUtilities.getAllImageInputLinks(uncommentedPageContent));
        urlStrings.addAll(LWPageUtilities.getAllScriptLinks(uncommentedPageContent));
        urlStrings.addAll(LWPageUtilities.getAllInlineCssResourceUrls(uncommentedPageContent));

        // get base URL
        URL baseURL = lwPage.getWebResponse().getWebRequest().getUrl();

        final List<String> baseLinks = LWPageUtilities.getAllBaseLinks(uncommentedPageContent);
        if (!baseLinks.isEmpty())
        {
            final URL u = XltWebClient.makeUrlAbsolute(baseURL, baseLinks.get(0));
            if (u != null)
            {
                baseURL = u;
            }
        }
        
        baseURL = URLCleaner.removeUserInfoIfNecessaryAsURL(baseURL);

        // get document charset
        final Charset charset = lwPage.getCharset();
        // start rewriting of page content -> begin with original content
        String newContent = pageContent;

        /*
         * Before URL rewriting starts, we gonna have to remove some tags and set the content-type meta tag
         */

        // (1) Remove all base tags
        newContent = LWPageUtilities.removeAllBaseTags(newContent);
        // (2) Remove all script tags
        newContent = LWPageUtilities.removeAllScriptTags(newContent);
        // (3) Remove all meta tags that declare the content type
        newContent = RegExUtils.replaceAll(newContent, "(?im)<meta [^>]*?http-equiv=\"content-type\".*?>", "");

        // (4) Append our meta tag string directly after the head tag
        newContent = RegExUtils.replaceAll(newContent, "(?im)<head.*?>", "$0\n<meta http-equiv=\"content-type\" content=\"" +
                                                                         lwPage.getWebResponse().getContentType() + "; charset=utf-8\" />");
        // (5) Remove any JS handler attribute
        newContent = removeJSHandlerAttrs(newContent);

        //
        // === URL rewriting ===
        //
        final String urlPrefix = (outermostPage ? XltConstants.DUMP_CACHE_DIR + "/" : "");

        final long urlRewriteStart = TimerUtils.getTime();

        for (final String urlString : urlStrings)
        {
            final URL url = XltWebClient.makeUrlAbsolute(baseURL, urlString);
            if (url != null)
            {
                final String crcString = mapping.map(UrlUtils.encodeUrl(url, false, charset));

                if (crcString != null)
                {
                    final String pattern = "(['\"\\(])\\s*" + RegExUtils.escape(urlString) + "\\s*(['\"\\)])";
                    final String cacheUrlString = urlPrefix + crcString;

                    newContent = RegExUtils.replaceAll(newContent, pattern, "$1" + cacheUrlString + "$2");
                }
            }
        }

        for (final Entry<String, LightWeightPage> frame : ((LightWeightPageImpl) lwPage).getFramePages().entrySet())
        {
            final String frameName = frame.getKey();
            final LightWeightPageImpl framePage = (LightWeightPageImpl) frame.getValue();

            final String cacheUrlString = urlPrefix + frameName + ".html";
            final String pattern = "(<i?frame [^>]*?src=\")" + RegExUtils.escape(framePage.getSource()) + "\"";

            newContent = RegExUtils.replaceAll(newContent, pattern, "$1" + cacheUrlString + "\"");
        }

        final long end = TimerUtils.getTime();
        if (LOGGER.isDebugEnabled())
        {
            LOGGER.debug("Transformation took: " + (end - start) + "ms [URL-Rewriting: " + (end - urlRewriteStart) + "ms].");
        }

        return newContent;
    }

    /**
     * Returns a collection of all names used in the given node map.
     *
     * @param attrs
     *            named node map
     * @return names
     */
    private Collection<String> getAttrNames(final NamedNodeMap attrs)
    {
        final HashSet<String> names = new HashSet<String>();
        if (attrs != null)
        {
            for (int i = 0; i < attrs.getLength(); i++)
            {
                names.add(attrs.item(i).getNodeName());
            }
        }

        return names;
    }

    /**
     * Removes all JavaScript handler attributes in the DOM subtree rooted at the given node.
     *
     * @param node
     *            root of DOM subtree to process
     */
    private void removeJSHandlerAttrs(final Node node)
    {
        if (node != null)
        {
            final NamedNodeMap attrs = node.getAttributes();
            for (final String attName : getAttrNames(attrs))
            {
                if (attName.startsWith("on"))
                {
                    attrs.removeNamedItem(attName);
                }
            }

            final NodeList children = node.getChildNodes();
            for (int i = 0; i < children.getLength(); i++)
            {
                removeJSHandlerAttrs(children.item(i));
            }
        }

    }

    /**
     * Removes all JavaScript handler attributes used in the given HTML content.
     *
     * @param htmlContent
     *            HTML content
     * @return given HTML content after all JavaScript handler attributes have been removed
     */
    private String removeJSHandlerAttrs(final String htmlContent)
    {
        if (htmlContent == null)
        {
            return "";
        }

        final StringBuilder sb = new StringBuilder(htmlContent.length());
        final Matcher m = RegExUtils.getPattern("(?sm)<[^/].*?>").matcher(htmlContent);
        int start = 0;
        while (m.find())
        {
            sb.append(htmlContent.substring(start, m.start()));
            sb.append(RegExUtils.replaceAll(m.group(), "\\s+on\\w+\\s*=\\s*\"[^\"]*?\"", ""));
            start = m.end();
        }

        sb.append(htmlContent.substring(Math.min(start, htmlContent.length() - 1), htmlContent.length()));

        return sb.toString();

    }
}
