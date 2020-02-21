package action.modules;

import com.xceptance.xlt.api.actions.AbstractHtmlPageAction;
import com.xceptance.xlt.api.engine.scripting.AbstractHtmlUnitActionsModule;

import action.modules.Open_ExamplePage;
import action.modules.assertText_actions.link;
import action.modules.assertText_actions.whitespaces;
import action.modules.assertText_actions.glob_RegEx;
import action.modules.assertText_actions.keyspace;
import action.modules.assertText_actions.pangram;
import action.modules.assertText_actions.format_bold;
import action.modules.assertText_actions.format_underline;
import action.modules.assertText_actions.format_italic;
import action.modules.assertText_actions.format_mixed;
import action.modules.assertText_actions.format_lineBreaks;
import action.modules.assertText_actions.format_table;
import action.modules.assertText_actions.emptyDiv0;
import action.modules.assertText_actions.emptyDivVisible;
import action.modules.assertText_actions.invisibleDiv0;
import action.modules.assertText_actions.matching_strategy0;
import action.modules.assertText_actions.textfield0;
import action.modules.assertText_actions.popup;
import action.modules.assertText_actions.iframe10;
import action.modules.assertText_actions.iframe20;
import action.modules.assertText_actions.iframe30;

/**
 * TODO: Add class description
 */
public class assertText extends AbstractHtmlUnitActionsModule
{


    /**
     * Constructor.
     */
    public assertText()
    {
    }


    /**
     * @{inheritDoc}
     */
    protected AbstractHtmlPageAction execute(final AbstractHtmlPageAction prevAction) throws Throwable
    {
        AbstractHtmlPageAction lastAction = prevAction;
        final Open_ExamplePage open_ExamplePage = new Open_ExamplePage();
        lastAction = open_ExamplePage.run(lastAction);

        lastAction = new link(lastAction);
        lastAction.run();

        lastAction = new whitespaces(lastAction);
        lastAction.run();

        lastAction = new glob_RegEx(lastAction);
        lastAction.run();

        lastAction = new keyspace(lastAction);
        lastAction.run();

        lastAction = new pangram(lastAction);
        lastAction.run();

        lastAction = new format_bold(lastAction);
        lastAction.run();

        lastAction = new format_underline(lastAction);
        lastAction.run();

        lastAction = new format_italic(lastAction);
        lastAction.run();

        lastAction = new format_mixed(lastAction);
        lastAction.run();

        lastAction = new format_lineBreaks(lastAction);
        lastAction.run();

        lastAction = new format_table(lastAction);
        lastAction.run();

        lastAction = new emptyDiv0(lastAction);
        lastAction.run();

        lastAction = new emptyDivVisible(lastAction);
        lastAction.run();

        lastAction = new invisibleDiv0(lastAction);
        lastAction.run();

        lastAction = new matching_strategy0(lastAction);
        lastAction.run();

        lastAction = new textfield0(lastAction);
        lastAction.run();

        lastAction = new popup(lastAction);
        lastAction.run();

        lastAction = new iframe10(lastAction);
        lastAction.run();

        lastAction = new iframe20(lastAction);
        lastAction.run();

        lastAction = new iframe30(lastAction);
        lastAction.run();


        return lastAction;
    }
}