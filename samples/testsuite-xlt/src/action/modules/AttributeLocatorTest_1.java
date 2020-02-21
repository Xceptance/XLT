package action.modules;

import com.gargoylesoftware.htmlunit.html.HtmlPage;

import com.xceptance.xlt.api.engine.scripting.AbstractHtmlUnitCommandsModule;


/**
 * no parameter defined
 */
public class AttributeLocatorTest_1 extends AbstractHtmlUnitCommandsModule
{


    /**
     * Constructor.
     * 
     */
    public AttributeLocatorTest_1()
    {
    }


    /**
     * @{inheritDoc}
     */
    protected HtmlPage execute(final HtmlPage page) throws Exception
    {
        HtmlPage resultingPage = page;
        assertAttribute("xpath=id('ws8_a')/input[1]@value", "foobar");

        return resultingPage;
    }
}