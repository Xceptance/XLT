package scripting.modules;

import com.xceptance.xlt.api.engine.scripting.AbstractWebDriverModule;
import scripting.modules.MultiSelection_byLabel;
import scripting.modules.MultiSelection_byValue;
import scripting.modules.MultiSelection_byIndex;
import scripting.modules.Open_ExamplePage;
import scripting.modules.MultiSelection_easy;

/**
 * TODO: Add class description
 */
public class multiSelection extends AbstractWebDriverModule
{

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doCommands(final String...parameters) throws Exception
    {
        final MultiSelection_byLabel _multiSelection_byLabel = new MultiSelection_byLabel();
        _multiSelection_byLabel.execute();

        final MultiSelection_byValue _multiSelection_byValue = new MultiSelection_byValue();
        _multiSelection_byValue.execute();

        final MultiSelection_byIndex _multiSelection_byIndex = new MultiSelection_byIndex();
        _multiSelection_byIndex.execute();

        final Open_ExamplePage _open_ExamplePage = new Open_ExamplePage();
        _open_ExamplePage.execute();

        final MultiSelection_easy _multiSelection_easy = new MultiSelection_easy();
        _multiSelection_easy.execute("id");


        //
        // ~~~ locators ~~~
        //
        startAction("locators");
        addSelection("id=select_9", "label=select_9_b");
        assertText("id=cc_change", "change (select_9) select_9_b");
        removeSelection("id=select_9", "label=select_9_b");
        assertText("id=cc_change", "change (select_9)");
        addSelection("name=select_10", "label=select_10_b");
        assertText("id=cc_change", "change (select_10) select_10_b");
        removeSelection("name=select_10", "label=select_10_b");
        assertText("id=cc_change", "change (select_10)");
        addSelection("xpath=//select[@id='select_9']", "label=select_9_b");
        assertText("id=cc_change", "change (select_9) select_9_b");
        removeSelection("xpath=//select[@id='select_9']", "label=select_9_b");
        assertText("id=cc_change", "change (select_9)");
        addSelection("dom=document.getElementById('select_10')", "label=select_10_b");
        assertText("id=cc_change", "change (select_10) select_10_b");
        removeSelection("dom=document.getElementById('select_10')", "label=select_10_b");
        assertText("id=cc_change", "change (select_10)");
        addSelection("css=#select_9", "label=select_9_b");
        assertText("id=cc_change", "change (select_9) select_9_b");
        removeSelection("css=#select_9", "label=select_9_b");
        assertText("id=cc_change", "change (select_9)");

    }
}