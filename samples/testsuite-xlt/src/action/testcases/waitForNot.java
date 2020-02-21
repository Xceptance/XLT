package action.testcases;

import org.junit.Test;
import com.xceptance.xlt.api.actions.AbstractHtmlPageAction;
import com.xceptance.xlt.api.engine.scripting.AbstractHtmlUnitScriptTestCase;


/**
 * TODO: Add class description
 */
public class waitForNot extends AbstractHtmlUnitScriptTestCase
{

    /**
     * Constructor.
     */
    public waitForNot()
    {
        super("http://localhost:8080/");
    }

    @Test
    public void test() throws Throwable
    {
        AbstractHtmlPageAction lastAction = null;

        final action.modules.waitForNot waitForNot = new action.modules.waitForNot();
        lastAction = waitForNot.run(lastAction);


    }
}