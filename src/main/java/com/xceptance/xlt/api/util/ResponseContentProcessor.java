package com.xceptance.xlt.api.util;

import java.util.regex.Pattern;

import com.gargoylesoftware.htmlunit.WebResponse;
import com.xceptance.common.util.ParameterCheckUtils;
import com.xceptance.common.util.RegExUtils;

/**
 * A {@link ResponseProcessor} implementation, which modifies the body of a web response based on regular expressions.
 * 
 * @author Jörg Werner (Xceptance Software Technologies GmbH)
 */
public class ResponseContentProcessor extends AbstractResponseProcessor
{
    /**
     * A regex specifying the piece of content to be replaced.
     */
    private final Pattern contentPattern;

    /**
     * The replacement string.
     */
    private final String replacement;

    /**
     * A regex specifying the URL(s) for which to apply the replacement.
     */
    private final Pattern urlPattern;

    /**
     * Creates a new {@link ResponseContentProcessor} object.
     * 
     * @param contentPattern
     *            a regex specifying the piece of content to be replaced
     * @param replacement
     *            the replacement string
     */
    public ResponseContentProcessor(final String contentPattern, final String replacement)
    {
        this(contentPattern, replacement, null);
    }

    /**
     * Creates a new {@link ResponseContentProcessor} object.
     * 
     * @param contentPattern
     *            a regex specifying the piece of content to be replaced
     * @param replacement
     *            the replacement string
     * @param urlPattern
     *            a regex specifying the URL(s) for which to apply the replacement
     */
    public ResponseContentProcessor(final String contentPattern, final String replacement, final String urlPattern)
    {
        ParameterCheckUtils.isNotNull(contentPattern, "contentPattern");
        ParameterCheckUtils.isNotNull(replacement, "replacement");

        this.contentPattern = RegExUtils.getPattern(contentPattern);
        this.replacement = replacement;
        this.urlPattern = (urlPattern != null) ? RegExUtils.getPattern(urlPattern) : null;
    }

    /**
     * Creates a new {@link ResponseContentProcessor} object.
     * 
     * @param contentPattern
     *            a regex specifying the piece of content to be replaced
     * @param replacement
     *            the replacement string
     */
    public ResponseContentProcessor(final Pattern contentPattern, final String replacement)
    {
        this(contentPattern, replacement, null);
    }

    /**
     * Creates a new {@link ResponseContentProcessor} object.
     * 
     * @param contentPattern
     *            a regex specifying the piece of content to be replaced
     * @param replacement
     *            the replacement string
     * @param urlPattern
     *            a regex specifying the URL(s) for which to apply the replacement
     */
    public ResponseContentProcessor(final Pattern contentPattern, final String replacement, final Pattern urlPattern)
    {
        ParameterCheckUtils.isNotNull(contentPattern, "contentPattern");
        ParameterCheckUtils.isNotNull(replacement, "replacement");

        this.urlPattern = urlPattern;
        this.contentPattern = contentPattern;
        this.replacement = replacement;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public WebResponse processResponse(WebResponse webResponse)
    {
        final String urlString = webResponse.getWebRequest().getUrl().toString();
        if (urlPattern == null || urlPattern.matcher(urlString).find())
        {
            final String content = webResponse.getContentAsString();
            if (content != null)
            {
                final String newContent = contentPattern.matcher(content).replaceAll(replacement);
                if (!newContent.equals(content))
                {
                    if (XltLogger.runTimeLogger.isDebugEnabled())
                    {
                        XltLogger.runTimeLogger.debug("Content replacement applied for URL: " + urlString);
                    }

                    webResponse = createWebResponse(webResponse, newContent);
                }
            }
        }

        return webResponse;
    }
}
