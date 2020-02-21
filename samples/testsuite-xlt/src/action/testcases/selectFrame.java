package action.testcases;

import org.junit.Test;
import com.xceptance.xlt.api.actions.AbstractHtmlPageAction;
import com.xceptance.xlt.api.engine.scripting.AbstractHtmlUnitScriptTestCase;


/**
 * TODO: Add class description
 */
public class selectFrame extends AbstractHtmlUnitScriptTestCase
{

    /**
     * Constructor.
     */
    public selectFrame()
    {
        super("http://localhost:8080/");
    }

    @Test
    public void test() throws Throwable
    {
        AbstractHtmlPageAction lastAction = null;

        final action.modules.selectFrame selectFrame = new action.modules.selectFrame();
        lastAction = selectFrame.run(lastAction);


    }
}