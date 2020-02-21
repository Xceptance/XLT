package scripting.modules;

import com.xceptance.xlt.api.engine.scripting.AbstractWebDriverModule;

/**
 * no parameter defined
 */
public class AttributeLocatorTest_1 extends AbstractWebDriverModule
{

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doCommands(final String...parameters) throws Exception
    {
        assertAttribute("xpath=id('ws8_a')/input[1]@value", "foobar");

    }
}