package scripting.modules;

import com.xceptance.xlt.api.engine.scripting.AbstractWebDriverModule;
import scripting.modules.Open_ExamplePage;
import scripting.modules.MultiSelection_easy;
import scripting.modules.MultiSelection_matching;
import scripting.modules.MultiSelection_specialChars;
import scripting.modules.MultiSelection_nonunique;

/**
 * TODO: Add class description
 */
public class MultiSelection_byLabel extends AbstractWebDriverModule
{

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doCommands(final String...parameters) throws Exception
    {
        final Open_ExamplePage _open_ExamplePage = new Open_ExamplePage();
        _open_ExamplePage.execute();

        final MultiSelection_easy _multiSelection_easy = new MultiSelection_easy();
        _multiSelection_easy.execute("label");

        final MultiSelection_matching _multiSelection_matching = new MultiSelection_matching();
        _multiSelection_matching.execute("label");

        final MultiSelection_specialChars _multiSelection_specialChars = new MultiSelection_specialChars();
        _multiSelection_specialChars.execute("label");

        final MultiSelection_nonunique _multiSelection_nonunique = new MultiSelection_nonunique();
        _multiSelection_nonunique.execute("label");


        //
        // ~~~ label_whitespace ~~~
        //
        startAction("label_whitespace");
        addSelection("id=select_18", "label= ");
        assertText("id=cc_change", "change (select_18) empty, 1 space, 2 spaces");
        removeSelection("id=select_18", "label=  ");
        assertText("id=cc_change", "change (select_18)");

        //
        // ~~~ label_empty ~~~
        //
        startAction("label_empty");
        addSelection("id=select_18", "label=");
        assertText("id=cc_change", "change (select_18) empty, 1 space, 2 spaces");
        removeSelection("id=select_18", "label=");
        assertText("id=cc_change", "change (select_18)");

    }
}