package action.testcases;

import org.junit.Test;
import com.xceptance.xlt.api.actions.AbstractHtmlPageAction;
import com.xceptance.xlt.api.engine.scripting.AbstractHtmlUnitScriptTestCase;


/**
 * TODO: Add class description
 */
public class AlertConfirm extends AbstractHtmlUnitScriptTestCase
{

    /**
     * Constructor.
     */
    public AlertConfirm()
    {
        super("http://localhost:8080");
    }

    @Test
    public void test() throws Throwable
    {
        AbstractHtmlPageAction lastAction = null;

        final action.modules.AlertConfirm alertConfirm = new action.modules.AlertConfirm();
        lastAction = alertConfirm.run(lastAction);


    }
}