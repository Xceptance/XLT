package action.testcases;

import org.junit.Test;
import com.xceptance.xlt.api.actions.AbstractHtmlPageAction;
import com.xceptance.xlt.api.engine.scripting.AbstractHtmlUnitScriptTestCase;


/**
 * TODO: Add class description
 */
public class assertNotXpathCount extends AbstractHtmlUnitScriptTestCase
{

    /**
     * Constructor.
     */
    public assertNotXpathCount()
    {
        super("http://localhost:8080/");
    }

    @Test
    public void test() throws Throwable
    {
        AbstractHtmlPageAction lastAction = null;

        final action.modules.assertNotXpathCount assertNotXpathCount = new action.modules.assertNotXpathCount();
        lastAction = assertNotXpathCount.run(lastAction);


    }
}