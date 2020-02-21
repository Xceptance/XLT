package action.testcases;

import org.junit.Test;
import com.xceptance.xlt.api.actions.AbstractHtmlPageAction;
import com.xceptance.xlt.api.engine.scripting.AbstractHtmlUnitScriptTestCase;


/**
 * TODO: Add class description
 */
public class assertNotElementPresent extends AbstractHtmlUnitScriptTestCase
{

    /**
     * Constructor.
     */
    public assertNotElementPresent()
    {
        super("http://localhost:8080/");
    }

    @Test
    public void test() throws Throwable
    {
        AbstractHtmlPageAction lastAction = null;

        final action.modules.assertNotElementPresent assertNotElementPresent = new action.modules.assertNotElementPresent();
        lastAction = assertNotElementPresent.run(lastAction);


    }
}