package scripting.modules;

import com.xceptance.xlt.api.engine.scripting.AbstractWebDriverModule;

/**
 * parameter defined but parameter name doesn't colide with attribute name
 */
public class AttributeLocatorTest_2 extends AbstractWebDriverModule
{

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doCommands(final String...parameters) throws Exception
    {
        final String param = parameters[0];
        assertAttribute("xpath=id('ws8_a')/input[1]@value", param);

    }
}