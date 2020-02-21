package scripting.modules;

import com.xceptance.xlt.api.engine.scripting.AbstractWebDriverModule;
import scripting.modules.Open_ExamplePage;

/**
 * TODO: Add class description
 */
public class TypeAndWait extends AbstractWebDriverModule
{

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doCommands(final String...parameters) throws Exception
    {
        final Open_ExamplePage _open_ExamplePage = new Open_ExamplePage();
        _open_ExamplePage.execute();

        //
        // ~~~ FillForm ~~~
        //
        startAction("FillForm");
        type("id=form1_t1", "don't submit");
        assertText("id=form1_t1", "don't submit");
        typeAndWait("id=form1_t2", "you shall submit");
        assertNotElementPresent("id=form1_t2");
        assertTextPresent("This is frame 1.");

    }
}