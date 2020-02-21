package action.testcases;

import org.junit.Test;
import com.xceptance.xlt.api.actions.AbstractHtmlPageAction;
import com.xceptance.xlt.api.engine.scripting.AbstractHtmlUnitScriptTestCase;


/**
 * TODO: Add class description
 */
public class deleteCookie extends AbstractHtmlUnitScriptTestCase
{

    /**
     * Constructor.
     */
    public deleteCookie()
    {
        super("http://localhost:8080/");
    }

    @Test
    public void test() throws Throwable
    {
        AbstractHtmlPageAction lastAction = null;

        final action.modules.deleteCookie deleteCookie = new action.modules.deleteCookie();
        lastAction = deleteCookie.run(lastAction);


    }
}