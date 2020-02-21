package action.testcases;

import org.junit.Test;
import com.xceptance.xlt.api.actions.AbstractHtmlPageAction;
import com.xceptance.xlt.api.engine.scripting.AbstractHtmlUnitScriptTestCase;


/**
 * TODO: Add class description
 */
public class assertTitle_frame extends AbstractHtmlUnitScriptTestCase
{

    /**
     * Constructor.
     */
    public assertTitle_frame()
    {
        super("http://localhost:8080/");
    }

    @Test
    public void test() throws Throwable
    {
        AbstractHtmlPageAction lastAction = null;

        final action.modules.assertTitle_frame assertTitle_frame = new action.modules.assertTitle_frame();
        lastAction = assertTitle_frame.run(lastAction);


    }
}