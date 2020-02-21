package action.testcases;

import org.junit.Test;
import com.xceptance.xlt.api.actions.AbstractHtmlPageAction;
import com.xceptance.xlt.api.engine.scripting.AbstractHtmlUnitScriptTestCase;

import action.modules.TypeAndWait;

/**
 * TODO: Add class description
 */
public class type extends AbstractHtmlUnitScriptTestCase
{

    /**
     * Constructor.
     */
    public type()
    {
        super("http://localhost:8080/");
    }

    @Test
    public void test() throws Throwable
    {
        AbstractHtmlPageAction lastAction = null;

        final action.modules.type type = new action.modules.type();
        lastAction = type.run(lastAction);

        final TypeAndWait typeAndWait = new TypeAndWait();
        lastAction = typeAndWait.run(lastAction);


    }
}