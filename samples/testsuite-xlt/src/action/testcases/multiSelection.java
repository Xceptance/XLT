package action.testcases;

import org.junit.Test;
import com.xceptance.xlt.api.actions.AbstractHtmlPageAction;
import com.xceptance.xlt.api.engine.scripting.AbstractHtmlUnitScriptTestCase;


/**
 * TODO: Add class description
 */
public class multiSelection extends AbstractHtmlUnitScriptTestCase
{

    /**
     * Constructor.
     */
    public multiSelection()
    {
        super("http://localhost:8080/");
    }

    @Test
    public void test() throws Throwable
    {
        AbstractHtmlPageAction lastAction = null;

        final action.modules.multiSelection multiSelection = new action.modules.multiSelection();
        lastAction = multiSelection.run(lastAction);


    }
}