package test.com.xceptance.xlt.engine.webdriver;

import org.junit.Test;

import com.xceptance.xlt.api.engine.scripting.AbstractWebDriverScriptTestCase;

/**
 * Runs certain redirect scenarios with the web driver/browser configured in the XLT settings. This test does not really
 * test something, but allows to generate the data (timers.csv, result browser) that have to be checked manually.
 */
public class RedirectTest extends AbstractWebDriverScriptTestCase
{
    private final String TEST_PAGE_URL = "http://localhost:8080/testpages/redirect/redirect.jsp";
    
    @Test
    public void test() throws Exception
    {
        // combine method GET and POST with each of these status code
        int[] statusCodes = new int[]
            {
                301, 302, 303, 307, 308
            };

        for (int statusCode : statusCodes)
        {
            startAction("Redirect GET " + statusCode);
            open(TEST_PAGE_URL + "?method=GET&statusCodes=" + statusCode);

            startAction("Redirect POST " + statusCode);
            open(TEST_PAGE_URL + "?method=POST&statusCodes=" + statusCode);
        }

        // special multi redirects
        startAction("Redirect POST 302,307");
        open(TEST_PAGE_URL + "?method=POST&statusCodes=302,307");

        startAction("Redirect POST 308,303");
        open(TEST_PAGE_URL + "?method=POST&statusCodes=308,303");
    }
}
