package action.testcases;

import org.junit.Test;
import com.xceptance.xlt.api.actions.AbstractHtmlPageAction;
import com.xceptance.xlt.api.engine.scripting.AbstractHtmlUnitScriptTestCase;


/**
 * TODO: Add class description
 */
public class assertTitle_singlePage extends AbstractHtmlUnitScriptTestCase
{

    /**
     * Constructor.
     */
    public assertTitle_singlePage()
    {
        super("http://localhost:8080/");
    }

    @Test
    public void test() throws Throwable
    {
        AbstractHtmlPageAction lastAction = null;

        final action.modules.assertTitle_singlePage assertTitle_singlePage = new action.modules.assertTitle_singlePage();
        lastAction = assertTitle_singlePage.run(lastAction);


    }
}