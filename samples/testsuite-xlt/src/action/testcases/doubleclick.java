package action.testcases;

import org.junit.Test;
import com.xceptance.xlt.api.actions.AbstractHtmlPageAction;
import com.xceptance.xlt.api.engine.scripting.AbstractHtmlUnitScriptTestCase;


/**
 * TODO: Add class description
 */
public class doubleclick extends AbstractHtmlUnitScriptTestCase
{

    /**
     * Constructor.
     */
    public doubleclick()
    {
        super("http://localhost:8080");
    }

    @Test
    public void test() throws Throwable
    {
        AbstractHtmlPageAction lastAction = null;

        final action.modules.doubleclick doubleclick = new action.modules.doubleclick();
        lastAction = doubleclick.run(lastAction);


    }
}