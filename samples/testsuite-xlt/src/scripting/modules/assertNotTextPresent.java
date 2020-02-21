package scripting.modules;

import com.xceptance.xlt.api.engine.scripting.AbstractWebDriverModule;
import scripting.modules.Open_ExamplePage;
import scripting.modules.SelectFrame_iframe_1;
import scripting.modules.SelectFrame_iframe_12;

/**
 * TODO: Add class description
 */
public class assertNotTextPresent extends AbstractWebDriverModule
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
        // ~~~ non_existing ~~~
        //
        startAction("non_existing");
        assertNotTextPresent("this text is not in the given page");

        //
        // ~~~ case_insensitive ~~~
        //
        startAction("case_insensitive");
        assertNotTextPresent("LOREM IPSUM");

        //
        // ~~~ iframe ~~~
        //
        startAction("iframe");
        final SelectFrame_iframe_1 _selectFrame_iframe_1 = new SelectFrame_iframe_1();
        _selectFrame_iframe_1.execute();

        assertNotTextPresent("ipsum");

        //
        // ~~~ subframe ~~~
        //
        startAction("subframe");
        final SelectFrame_iframe_12 _selectFrame_iframe_12 = new SelectFrame_iframe_12();
        _selectFrame_iframe_12.execute();

        assertNotTextPresent("iframe 1");

    }
}