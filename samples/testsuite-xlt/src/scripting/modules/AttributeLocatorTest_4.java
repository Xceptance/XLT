package scripting.modules;

import com.xceptance.xlt.api.engine.scripting.AbstractWebDriverModule;

/**
 * attribute name as parameter
 */
public class AttributeLocatorTest_4 extends AbstractWebDriverModule
{

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doCommands(final String...parameters) throws Exception
    {
        final String attribute = parameters[0];
        assertAttribute("xpath=id('ws8_a')/input[1]@" + attribute, "foobar");

    }
}