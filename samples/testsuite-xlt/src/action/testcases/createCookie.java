package action.testcases;

import org.junit.Test;
import com.xceptance.xlt.api.actions.AbstractHtmlPageAction;
import com.xceptance.xlt.api.engine.scripting.AbstractHtmlUnitScriptTestCase;


/**
 * TODO: Add class description
 */
public class createCookie extends AbstractHtmlUnitScriptTestCase
{

    /**
     * Constructor.
     */
    public createCookie()
    {
        super("http://localhost:8080/");
    }

    @Test
    public void test() throws Throwable
    {
        AbstractHtmlPageAction lastAction = null;

        final action.modules.createCookie createCookie = new action.modules.createCookie();
        lastAction = createCookie.run(lastAction);


    }
}