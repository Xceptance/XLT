package action.testcases;

import org.junit.Test;
import com.xceptance.xlt.api.actions.AbstractHtmlPageAction;
import com.xceptance.xlt.api.engine.scripting.AbstractHtmlUnitScriptTestCase;


/**
 * TODO: Add class description
 */
public class assertNotTitle extends AbstractHtmlUnitScriptTestCase
{

    /**
     * Constructor.
     */
    public assertNotTitle()
    {
        super("http://localhost:8080/");
    }

    @Test
    public void test() throws Throwable
    {
        AbstractHtmlPageAction lastAction = null;

        final action.modules.assertNotTitle assertNotTitle = new action.modules.assertNotTitle();
        lastAction = assertNotTitle.run(lastAction);


    }
}