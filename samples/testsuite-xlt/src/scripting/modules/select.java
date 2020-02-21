package scripting.modules;

import com.xceptance.xlt.api.engine.scripting.AbstractWebDriverModule;
import scripting.modules.Select_byLabel;
import scripting.modules.Select_byValue;
import scripting.modules.Select_byIndex;
import scripting.modules.SelectAndWait;
import scripting.modules.Open_ExamplePage;
import scripting.modules.Select_easy;

/**
 * TODO: Add class description
 */
public class select extends AbstractWebDriverModule
{

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doCommands(final String...parameters) throws Exception
    {
        final Select_byLabel _select_byLabel = new Select_byLabel();
        _select_byLabel.execute();

        final Select_byValue _select_byValue = new Select_byValue();
        _select_byValue.execute();

        final Select_byIndex _select_byIndex = new Select_byIndex();
        _select_byIndex.execute();

        final SelectAndWait _selectAndWait = new SelectAndWait();
        _selectAndWait.execute();

        final Open_ExamplePage _open_ExamplePage = new Open_ExamplePage();
        _open_ExamplePage.execute();

        final Select_easy _select_easy = new Select_easy();
        _select_easy.execute("id");

        //
        // ~~~ multi_select ~~~
        //
        startAction("multi_select");
        select("id=select_18", "label=select_18");
        assertText("id=cc_change", "change (select_18) select_18a, select_18b");
        select("id=select_18", "label=");
        assertText("id=cc_change", "change (select_18) empty, 1 space, 2 spaces");
        select("id=select_18", "index=8");
        assertText("id=cc_change", "change (select_18) select_18b");
        select("id=select_18", "value=select_18");
        assertText("id=cc_change", "change (select_18) select_18a, select_18b");
        //
        // ~~~ locators ~~~
        //
        startAction("locators");
        select("id=select_1", "label=select_1_b");
        assertText("id=cc_change", "change (select_1) select_1_b");
        select("name=select_2", "label=select_2_b");
        assertText("id=cc_change", "change (select_2) select_2_b");
        select("xpath=id('select_1')", "label=select_1_c");
        assertText("id=cc_change", "change (select_1) select_1_c");
        select("css=select#select_2", "label=select_2_a");
        assertText("id=cc_change", "change (select_2) select_2_a");
        select("dom=document.getElementById('select_1')", "label=select_1_a");
        assertText("id=cc_change", "change (select_1) select_1_a");

    }
}