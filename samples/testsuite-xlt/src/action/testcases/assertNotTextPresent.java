package action.testcases;

import org.junit.Test;
import com.xceptance.xlt.api.actions.AbstractHtmlPageAction;
import com.xceptance.xlt.api.engine.scripting.AbstractHtmlUnitScriptTestCase;


/**
 * TODO: Add class description
 */
public class assertNotTextPresent extends AbstractHtmlUnitScriptTestCase
{

    /**
     * Constructor.
     */
    public assertNotTextPresent()
    {
        super("http://localhost:8080/");
    }

    @Test
    public void test() throws Throwable
    {
        AbstractHtmlPageAction lastAction = null;

        final action.modules.assertNotTextPresent assertNotTextPresent = new action.modules.assertNotTextPresent();
        lastAction = assertNotTextPresent.run(lastAction);


    }
}