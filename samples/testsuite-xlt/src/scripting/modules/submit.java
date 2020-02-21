package scripting.modules;

import com.xceptance.xlt.api.engine.scripting.AbstractWebDriverModule;
import scripting.modules.Open_ExamplePage;

/**
 * TODO: Add class description
 */
public class submit extends AbstractWebDriverModule
{

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doCommands(final String...parameters) throws Exception
    {

        //
        // ~~~ with_button_byID ~~~
        //
        startAction("with_button_byID");
        final Open_ExamplePage _open_ExamplePage = new Open_ExamplePage();
        _open_ExamplePage.execute();

        submitAndWait("id=form1");
        assertTextPresent("This is frame 1. ");

        //
        // ~~~ no_button_byName ~~~
        //
        startAction("no_button_byName");
        _open_ExamplePage.execute();

        submitAndWait("name=form2");
        assertTextPresent("This is frame 2.");

        //
        // ~~~ no_button_byXpath ~~~
        //
        startAction("no_button_byXpath");
        _open_ExamplePage.execute();

        submitAndWait("xpath=//div[@id='form']/form[@id='form1']");
        assertTextPresent("This is frame 1.");

        //
        // ~~~ no_action ~~~
        //
        startAction("no_action");
        _open_ExamplePage.execute();

        submitAndWait("dom=document.getElementById('form3')");
        assertTitle("example page");

        //
        // ~~~ empty_form ~~~
        //
        startAction("empty_form");
        _open_ExamplePage.execute();

        submitAndWait("id=form4");
        assertText("id=f3", "This is iframe 3.");

        //
        // ~~~ empty_form_byDom ~~~
        //
        startAction("empty_form_byDom");
        // _open_ExamplePage.execute();
        // click("id=form4_border");
        // submitAndWait("id=form4");
        // assertTextPresent("This is iframe 3.");
    }
}