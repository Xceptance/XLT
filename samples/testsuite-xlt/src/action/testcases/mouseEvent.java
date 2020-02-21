package action.testcases;

import org.junit.Test;
import com.xceptance.xlt.api.actions.AbstractHtmlPageAction;
import com.xceptance.xlt.api.engine.scripting.AbstractHtmlUnitScriptTestCase;


/**
 * TODO: Add class description
 */
public class mouseEvent extends AbstractHtmlUnitScriptTestCase
{

    /**
     * Constructor.
     */
    public mouseEvent()
    {
        super("http://localhost:8080/");
    }

    @Test
    public void test() throws Throwable
    {
        AbstractHtmlPageAction lastAction = null;

        final action.modules.mouseEvent mouseEvent = new action.modules.mouseEvent();
        lastAction = mouseEvent.run(lastAction);


    }
}