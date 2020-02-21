package action.testcases;

import org.junit.Test;
import com.xceptance.xlt.api.actions.AbstractHtmlPageAction;
import com.xceptance.xlt.api.engine.scripting.AbstractHtmlUnitScriptTestCase;


/**
 * TODO: Add class description
 */
public class assertTextPresent extends AbstractHtmlUnitScriptTestCase
{

    /**
     * Constructor.
     */
    public assertTextPresent()
    {
        super("http://localhost:8080/");
    }

    @Test
    public void test() throws Throwable
    {
        AbstractHtmlPageAction lastAction = null;

        final action.modules.assertTextPresent assertTextPresent = new action.modules.assertTextPresent();
        lastAction = assertTextPresent.run(lastAction);


    }
}