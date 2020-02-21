package com.xceptance.xlt.misc.performance;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.xceptance.xlt.api.util.XltLogger;
import com.xceptance.xlt.api.validators.HtmlEndTagValidator;
import com.xceptance.xlt.engine.util.TimerUtils;

/**
 * Tests the performance of the implementation of {@link HtmlEndTagValidator#validate(String)}.
 * 
 * @author René Schwietzke (Xceptance Software Technologies GmbH)
 */
public class HtmlEndTagValidatorTest extends AbstractHtmlTest
{
    /**
     * Content of a small HTML page as string.
     */
    private static final String smallPageContent = "<html><head><title></title>\t\r\n</head><body> \n\t</body>\n\r</html>\n<!-- \nComment \t-->\n <!-- Test -->";

    /**
     * Content of big HTML page.
     */
    private HtmlPage htmlPage;

    /**
     * Number of iterations.
     */
    private static final int rounds = 10000;

    /**
     * Test fixture setup.
     * 
     * @throws Exception
     *             thrown when setup failed
     */
    @Before
    public void setUp() throws Exception
    {
        // read in resource's content and construct page
        htmlPage = setUp(this);
    }

    /**
     * Test the performance of {@link HtmlEndTagValidator#validate(String)} by passing a small HTML page.
     * 
     * @throws Exception
     *             thrown when something went wrong
     */
    @Test
    @Ignore("Performance test")
    public final void testValidate_SpeedSmallPage() throws Exception
    {
        // dry run to give engine a chance to cache something
        HtmlEndTagValidator.getInstance().validate(smallPageContent);

        final long startTime = TimerUtils.getTime();
        for (int i = 0; i < rounds; i++)
        {
            HtmlEndTagValidator.getInstance().validate(smallPageContent);
        }
        final long endTime = TimerUtils.getTime();
        final long duration = endTime - startTime;
        final double rate = ((double) rounds) / ((double) duration);
        XltLogger.runTimeLogger.info(String.format("Duration for %d iterations using a small page: %dms (%.2f pages per ms).", rounds,
                                                   duration, rate));

    }

    /**
     * Test the performance of {@link HtmlEndTagValidator#validate(String)} by passing a big HTML page.
     * 
     * @throws Exception
     *             thrown when something went wrong
     */
    @Test
    @Ignore("Performance test")
    public final void testValidate_SpeedBig() throws Exception
    {
        final String content = htmlPage.getWebResponse().getContentAsString();

        // dry run to give engine a chance to cache something
        HtmlEndTagValidator.getInstance().validate(content);

        final long startTime = TimerUtils.getTime();
        for (int i = 0; i < rounds; i++)
        {
            HtmlEndTagValidator.getInstance().validate(content);
        }
        final long endTime = TimerUtils.getTime();
        final long duration = endTime - startTime;
        final double rate = ((double) rounds) / ((double) duration);
        XltLogger.runTimeLogger.info(String.format("Duration for %d iterations using a big page: %dms (%.2f pages per ms).", rounds,
                                                   duration, rate));
    }
}
