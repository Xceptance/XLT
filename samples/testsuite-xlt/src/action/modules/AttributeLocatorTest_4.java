package action.modules;

import com.gargoylesoftware.htmlunit.html.HtmlPage;

import com.xceptance.xlt.api.engine.scripting.AbstractHtmlUnitCommandsModule;


/**
 * attribute name as parameter
 */
public class AttributeLocatorTest_4 extends AbstractHtmlUnitCommandsModule
{

    /**
     * The 'attribute' parameter.
     */
    private final String attribute;


    /**
     * Constructor.
     * @param attribute The 'attribute' parameter.
     * 
     */
    public AttributeLocatorTest_4(final String attribute)
    {
        this.attribute = attribute;
    }


    /**
     * @{inheritDoc}
     */
    protected HtmlPage execute(final HtmlPage page) throws Exception
    {
        HtmlPage resultingPage = page;
        assertAttribute("xpath=id('ws8_a')/input[1]@" + attribute, "foobar");

        return resultingPage;
    }
}