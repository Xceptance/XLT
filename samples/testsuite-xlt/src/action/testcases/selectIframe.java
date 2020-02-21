package action.testcases;

import org.junit.Test;
import com.xceptance.xlt.api.actions.AbstractHtmlPageAction;
import com.xceptance.xlt.api.engine.scripting.AbstractHtmlUnitScriptTestCase;


/**
 * TODO: Add class description
 */
public class selectIframe extends AbstractHtmlUnitScriptTestCase
{

    /**
     * Constructor.
     */
    public selectIframe()
    {
        super("http://localhost:8080/");
    }

    @Test
    public void test() throws Throwable
    {
        AbstractHtmlPageAction lastAction = null;

        final action.modules.selectIframe selectIframe = new action.modules.selectIframe();
        lastAction = selectIframe.run(lastAction);


    }
}