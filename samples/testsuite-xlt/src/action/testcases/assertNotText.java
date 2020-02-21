package action.testcases;

import org.junit.Test;
import com.xceptance.xlt.api.actions.AbstractHtmlPageAction;
import com.xceptance.xlt.api.engine.scripting.AbstractHtmlUnitScriptTestCase;


/**
 * TODO: Add class description
 */
public class assertNotText extends AbstractHtmlUnitScriptTestCase
{

    /**
     * Constructor.
     */
    public assertNotText()
    {
        super("http://localhost:8080/");
    }

    @Test
    public void test() throws Throwable
    {
        AbstractHtmlPageAction lastAction = null;

        final action.modules.assertNotText assertNotText = new action.modules.assertNotText();
        lastAction = assertNotText.run(lastAction);


    }
}