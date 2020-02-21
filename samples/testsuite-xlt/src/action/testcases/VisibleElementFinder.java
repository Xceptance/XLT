package action.testcases;

import org.junit.Test;
import com.xceptance.xlt.api.actions.AbstractHtmlPageAction;
import com.xceptance.xlt.api.engine.scripting.AbstractHtmlUnitScriptTestCase;


/**
 * Locator points to visible AND invisible elements. The invisible one is listed before the visible one.
 The element finder must choose the visible (second) one.
 */
public class VisibleElementFinder extends AbstractHtmlUnitScriptTestCase
{

    /**
     * Constructor.
     */
    public VisibleElementFinder()
    {
        super("http://localhost:8080");
    }

    @Test
    public void test() throws Throwable
    {
        AbstractHtmlPageAction lastAction = null;

        final action.modules.VisibleElementFinder visibleElementFinder = new action.modules.VisibleElementFinder();
        lastAction = visibleElementFinder.run(lastAction);


    }
}