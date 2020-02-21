package action.modules;

import com.gargoylesoftware.htmlunit.html.HtmlPage;

import com.xceptance.xlt.api.engine.scripting.AbstractHtmlUnitCommandsModule;


/**
 * parameter defined but parameter name doesn't colide with attribute name
 */
public class AttributeLocatorTest_2 extends AbstractHtmlUnitCommandsModule
{

    /**
     * The 'param' parameter.
     */
    private final String param;


    /**
     * Constructor.
     * @param param The 'param' parameter.
     * 
     */
    public AttributeLocatorTest_2(final String param)
    {
        this.param = param;
    }


    /**
     * @{inheritDoc}
     */
    protected HtmlPage execute(final HtmlPage page) throws Exception
    {
        HtmlPage resultingPage = page;
        assertAttribute("xpath=id('ws8_a')/input[1]@value", param);

        return resultingPage;
    }
}