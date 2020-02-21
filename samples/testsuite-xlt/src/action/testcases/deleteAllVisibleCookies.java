package action.testcases;

import org.junit.Test;
import com.xceptance.xlt.api.actions.AbstractHtmlPageAction;
import com.xceptance.xlt.api.engine.scripting.AbstractHtmlUnitScriptTestCase;


/**
 * TODO: Add class description
 */
public class deleteAllVisibleCookies extends AbstractHtmlUnitScriptTestCase
{

    /**
     * Constructor.
     */
    public deleteAllVisibleCookies()
    {
        super("http://localhost:8080/");
    }

    @Test
    public void test() throws Throwable
    {
        AbstractHtmlPageAction lastAction = null;

        final action.modules.deleteAllVisibleCookies deleteAllVisibleCookies = new action.modules.deleteAllVisibleCookies();
        lastAction = deleteAllVisibleCookies.run(lastAction);


    }
}