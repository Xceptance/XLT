package scripting.modules;

import com.xceptance.xlt.api.engine.scripting.AbstractWebDriverModule;

/**
 * parameter name fits attribute name
 */
public class AttributeLocatorTest_3 extends AbstractWebDriverModule
{

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doCommands(final String...parameters) throws Exception
    {
        final String value = parameters[0];
        assertAttribute("xpath=id('ws8_a')/input[1]@value", value);

    }
}