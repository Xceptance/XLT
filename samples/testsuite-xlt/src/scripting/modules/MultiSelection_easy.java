package scripting.modules;

import com.xceptance.xlt.api.engine.scripting.AbstractWebDriverModule;

/**
 * TODO: Add class description
 */
public class MultiSelection_easy extends AbstractWebDriverModule
{

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doCommands(final String...parameters) throws Exception
    {
        final String optionLocator = parameters[0];

        //
        // ~~~ lettersOnly_add ~~~
        //
        startAction("lettersOnly_add");
        addSelection("name=select_9", optionLocator + "=select_9_b");
        assertText("id=cc_change", "change (select_9) select_9_b");
        addSelection("name=select_9", optionLocator + "=select_9_c");
        assertText("id=cc_change", "change (select_9) select_9_b, select_9_c");

        //
        // ~~~ lettersOnly_remove ~~~
        //
        startAction("lettersOnly_remove");
        removeSelection("name=select_9", optionLocator + "=select_9_b");
        assertText("id=cc_change", "change (select_9) select_9_c");
        removeSelection("name=select_9", optionLocator + "=select_9_c");
        assertText("id=cc_change", "change (select_9)");

        //
        // ~~~ withWhitespace_add ~~~
        //
        startAction("withWhitespace_add");
        addSelection("xpath=//select[@id='select_16']", optionLocator + "=select_16 a");
        assertText("id=cc_change", "change (select_16) select_16 a");
        addSelection("xpath=//select[@id='select_16']", optionLocator + "=select_16 b");
        assertText("id=cc_change", "change (select_16) select_16 a, select_16 b");
        addSelection("xpath=//select[@id='select_16']", optionLocator + "=select_16 c");
        assertText("id=cc_change", "change (select_16) select_16 a, select_16 b, select_16 c");

        //
        // ~~~ withWhitespace_remove ~~~
        //
        startAction("withWhitespace_remove");
        removeSelection("xpath=//select[@id='select_16']", optionLocator + "=select_16 a");
        assertText("id=cc_change", "change (select_16) select_16 b, select_16 c");
        removeSelection("xpath=//select[@id='select_16']", optionLocator + "=select_16 b");
        assertText("id=cc_change", "change (select_16) select_16 c");
        removeSelection("xpath=//select[@id='select_16']", optionLocator + "=select_16 c");
        assertText("id=cc_change", "change (select_16)");

        //
        // ~~~ doubleSelect ~~~
        //
        startAction("doubleSelect");
        addSelection("id=select_9", optionLocator + "=select_9_b");
        assertText("id=cc_change", "change (select_9) select_9_b");
        addSelection("id=select_9", optionLocator + "=select_9_b");
        assertText("id=cc_change", "change (select_9) select_9_b");

        //
        // ~~~ doubleSelect_cleanup ~~~
        //
        startAction("doubleSelect_cleanup");
        removeSelection("id=select_9", optionLocator + "=select_9_b");
        assertText("id=cc_change", "change (select_9)");

        //
        // ~~~ removeUnselected ~~~
        //
        startAction("removeUnselected");
        removeSelection("id=select_9", optionLocator + "=select_9_b");
        assertText("id=cc_change", "change (select_9)");

    }
}