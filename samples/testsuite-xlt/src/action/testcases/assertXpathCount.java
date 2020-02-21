package action.testcases;

import org.junit.Test;
import com.xceptance.xlt.api.actions.AbstractHtmlPageAction;
import com.xceptance.xlt.api.engine.scripting.AbstractHtmlUnitScriptTestCase;


/**
 * TODO: Add class description
 */
public class assertXpathCount extends AbstractHtmlUnitScriptTestCase
{

    /**
     * Constructor.
     */
    public assertXpathCount()
    {
        super("http://localhost:8080/");
    }

    @Test
    public void test() throws Throwable
    {
        AbstractHtmlPageAction lastAction = null;

        final action.modules.assertXpathCount assertXpathCount = new action.modules.assertXpathCount();
        lastAction = assertXpathCount.run(lastAction);


    }
}