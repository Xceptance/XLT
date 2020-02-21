/**
 * Copyright is held by Xceptance Software Technologies GmbH.
 */
package test.com.gargoylesoftware.htmlunit;

import org.junit.After;
import org.junit.Test;

import com.xceptance.xlt.api.engine.scripting.AbstractWebDriverScriptTestCase;
import com.xceptance.xlt.api.webdriver.XltDriver;

/**
 * Added due to <a href="https://lab.xceptance.de/issues/891">891</a>
 * 
 * @author Sebastian Oerding
 */
public class IncorrectlySkippedJavaScriptExecutionTest extends AbstractWebDriverScriptTestCase
{
    /**
     * We have to give a default public no arg constructor for JUnit as there is none in the super class.
     */
    public IncorrectlySkippedJavaScriptExecutionTest()
    {
        super(new XltDriver());
    }

    /**
     * There was a bug in HtmlUnit that caused JavaScipts not to be executed under specific circumstances (having
     * anchors targeting on the same page and some more). This bug was fixed in HtmlUnit 2.9.
     */
    @Test
    public void test()
    {
        open("http://localhost:8080/testpages/examplePage_1.html");

        click("link=anc_sel1");
        assertText("id=cc_click_content", "anc_sel1");

        click("link=anc_sel2");
        assertText("id=cc_click_content", "anc_sel2");

        click("link=anc_sel3");
        assertText("id=cc_click_content", "anc_sel3");

        click("link=anc_sel4");
        assertText("id=cc_click_content", "anc_sel4");
    }

    @After
    public void after()
    {
        getWebDriver().quit();
    }
}
