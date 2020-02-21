package scripting.modules;

import com.xceptance.xlt.api.engine.scripting.AbstractWebDriverModule;
import scripting.modules.Open_ExamplePage;

/**
 * TODO: Add class description
 */
public class check extends AbstractWebDriverModule
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
        // ~~~ initial ~~~
        //
        startAction("initial");
        assertChecked("id=in_chk_5");
        assertNotChecked("id=in_chk_1");

        //
        // ~~~ check ~~~
        //
        startAction("check");
        check("id=in_chk_1");
        assertText("id=cc_change", "change (in_chk_1) true");
        assertChecked("id=in_chk_1");

        //
        // ~~~ double_check ~~~
        //
        startAction("double_check");
        check("id=in_chk_3");
        check("id=in_chk_3");
        assertText("id=cc_change", "change (in_chk_3) true");

        //
        // ~~~ uncheck ~~~
        //
        startAction("uncheck");
        uncheck("id=in_chk_5");
        assertText("id=cc_change", "change (in_chk_5) false");
        assertNotChecked("id=in_chk_5");

        //
        // ~~~ double_uncheck ~~~
        //
        startAction("double_uncheck");
        uncheck("id=in_chk_6");
        uncheck("id=in_chk_6");
        assertText("id=cc_change", "change (in_chk_6) false");

        //
        // ~~~ sequence ~~~
        //
        startAction("sequence");
        check("id=in_chk_4");
        assertText("id=cc_change", "change (in_chk_4) true");
        uncheck("id=in_chk_4");
        assertText("id=cc_change", "change (in_chk_4) false");
        check("id=in_chk_4");
        assertText("id=cc_change", "change (in_chk_4) true");
        uncheck("id=in_chk_4");
        assertText("id=cc_change", "change (in_chk_4) false");

        //
        // ~~~ radio_button ~~~
        //
        startAction("radio_button");
        check("id=in_rad_1");
        assertText("id=cc_change", "change (in_rad_1) true");

        //
        // ~~~ different_selectors ~~~
        //
        startAction("different_selectors");
        check("id=in_chk_7");
        assertText("id=cc_change", "change (in_chk_7) true");
        check("name=in_chk_8");
        assertText("id=cc_change", "change (in_chk_8) true");
        check("xpath=id('in_checkbox')/input[@value='in_chk_9' and @type='checkbox']");
        assertText("id=cc_change", "change (in_chk_9) true");
        check("xpath=id('in_checkbox')/input[@type='checkbox'][10]");
        assertText("id=cc_change", "change (check plain) true");
        check("dom=document.getElementById('in_chk_2')");
        assertText("id=cc_change", "change (in_chk_2) true");
        check("css=input#in_chk_6");
        assertText("id=cc_change", "change (in_chk_6) true");

        //
        // ~~~ different_selectors ~~~
        //
        startAction("different_selectors");
        uncheck("id=in_chk_7");
        assertText("id=cc_change", "change (in_chk_7) false");
        uncheck("name=in_chk_8");
        assertText("id=cc_change", "change (in_chk_8) false");
        uncheck("xpath=id('in_checkbox')/input[@value='in_chk_9' and @type='checkbox']");
        assertText("id=cc_change", "change (in_chk_9) false");
        uncheck("xpath=id('in_checkbox')/input[@type='checkbox'][10]");
        assertText("id=cc_change", "change (check plain) false");
        uncheck("dom=document.getElementById('in_chk_2')");
        assertText("id=cc_change", "change (in_chk_2) false");

    }
}