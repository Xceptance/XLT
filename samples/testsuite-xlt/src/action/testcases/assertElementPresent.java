package action.testcases;

import org.junit.Test;
import com.xceptance.xlt.api.actions.AbstractHtmlPageAction;
import com.xceptance.xlt.api.engine.scripting.AbstractHtmlUnitScriptTestCase;


/**
 * TODO: Add class description
 */
public class assertElementPresent extends AbstractHtmlUnitScriptTestCase
{

    /**
     * Constructor.
     */
    public assertElementPresent()
    {
        super("http://localhost:8080/");
    }

    @Test
    public void test() throws Throwable
    {
        AbstractHtmlPageAction lastAction = null;

        final action.modules.assertElementPresent assertElementPresent = new action.modules.assertElementPresent();
        lastAction = assertElementPresent.run(lastAction);


    }
}