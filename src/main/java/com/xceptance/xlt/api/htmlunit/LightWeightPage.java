package com.xceptance.xlt.api.htmlunit;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import org.apache.commons.lang3.StringUtils;

import com.gargoylesoftware.htmlunit.WebRequest;
import com.gargoylesoftware.htmlunit.WebResponse;
import com.xceptance.common.util.RegExUtils;

/**
 * A simple page object for light-weight operations.
 * 
 * @author René Schwietzke (Xceptance Software Technologies GmbH)
 */
public class LightWeightPage
{
    /**
     * The web response.
     */
    private final WebResponse response;

    /**
     * The timer name.
     */
    private final String name;

    /**
     * Contents character set.
     */
    private final Charset charset;

    /**
     * Constructor.
     * 
     * @param webResponse
     *            the web response
     */
    public LightWeightPage(final WebResponse webResponse, final String timerName)
    {
        response = webResponse;
        name = timerName;
        charset = determineContentCharset();
    }

    /**
     * Returns the page content.
     * 
     * @return the content
     */
    public String getContent()
    {
        return response.getContentAsString(getCharset());
    }

    /**
     * Returns the status code of the web response.
     * 
     * @return status code of response
     */
    public int getHttpResponseCode()
    {
        return response.getStatusCode();
    }

    /**
     * Returns the web response.
     * 
     * @return web response
     */
    public WebResponse getWebResponse()
    {
        return response;
    }

    /**
     * Returns the timer name.
     * 
     * @return timer name
     */
    public String getTimerName()
    {
        return name;
    }

    /**
     * Returns the content character set.
     * 
     * @return content character set
     */
    public String getContentCharset()
    {
        return charset.name();
    }

    /**
     * Returns the content character set.
     * 
     * @return content character set
     */
    public Charset getCharset()
    {
        return charset;
    }

    /**
     * Determines the content character set.
     * 
     * @return content character set
     */
    private Charset determineContentCharset()
    {
        if (response != null)
        {
            // 1st: get value of content-type response header
            String charsetName = StringUtils.substringAfter(response.getResponseHeaderValue("content-type"), "charset=");
            if (StringUtils.isEmpty(charsetName))
            {
                final String content = response.getContentAsString(StandardCharsets.ISO_8859_1);
                if (!StringUtils.isEmpty(content))
                {
                    // 2nd: get the encoding attribute from a potential <?xml?>
                    // header (in case of XHTML)
                    charsetName = RegExUtils.getFirstMatch(content, "<\\?xml.*? encoding=\"(.+?)\".*?\\?>", 1);
                    if (StringUtils.isEmpty(charsetName))
                    {
                        // 3rd: get declared charset in content-type meta tag
                        charsetName = RegExUtils.getFirstMatch(content, "<meta [^>]*?content=\"[^\"]*?charset=([^\";]+)\"", 1);
                    }
                }

                if (StringUtils.isEmpty(charsetName))
                {
                    // 4th: get content charset of request settings
                    final WebRequest request = response.getWebRequest();
                    charsetName = request != null ? request.getCharset().name() : null;
                }
            }

            if (!StringUtils.isEmpty(charsetName) && Charset.isSupported(charsetName))
            {
                return Charset.forName(charsetName);
            }
        }

        return StandardCharsets.ISO_8859_1;
    }
}
