package action.modules;

import com.gargoylesoftware.htmlunit.html.HtmlPage;

import com.xceptance.xlt.api.engine.scripting.AbstractHtmlUnitCommandsModule;


/**
 * parameter name fits attribute name
 */
public class AttributeLocatorTest_3 extends AbstractHtmlUnitCommandsModule
{

    /**
     * The 'value' parameter.
     */
    private final String value;


    /**
     * Constructor.
     * @param value The 'value' parameter.
     * 
     */
    public AttributeLocatorTest_3(final String value)
    {
        this.value = value;
    }


    /**
     * @{inheritDoc}
     */
    protected HtmlPage execute(final HtmlPage page) throws Exception
    {
        HtmlPage resultingPage = page;
        assertAttribute("xpath=id('ws8_a')/input[1]@value", value);

        return resultingPage;
    }
}