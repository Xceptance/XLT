package action.placeholders.overrideTestdata.TOverrideTestData_actions;

import org.junit.Assert;

import com.xceptance.xlt.api.actions.AbstractHtmlPageAction;
import com.xceptance.xlt.api.engine.scripting.AbstractHtmlUnitScriptAction;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import action.placeholders.overrideTestdata.Mod_2c;
import action.placeholders.overrideTestdata.Mod_2b;
import action.placeholders.overrideTestdata.Mod_2a;
import action.placeholders.overrideTestdata.Mod_3;

/**
 * TODO: Add class description
 */
public class TOverrideTestDataAction extends AbstractHtmlUnitScriptAction
{

    /**
     * Constructor.
     * @param prevAction The previous action.
     */
    public TOverrideTestDataAction(final AbstractHtmlPageAction prevAction)
    {
        super(prevAction);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void preValidate() throws Exception
    {
        final HtmlPage page = getPreviousAction().getHtmlPage();
        Assert.assertNotNull("Failed to get page from previous action", page);
        assertText("id=specialchar_1", resolve("${gtd1}"));

    }


    /**
     * {@inheritDoc}
     */
    @Override
    protected void execute() throws Exception
    {
        HtmlPage page = getPreviousAction().getHtmlPage();
        // reset input for further testing
        page = type("id=in_txt_1", resolve("${t1} - 0"));
        assertText("id=cc_keyup", "keyup (in_txt_1) fromTestcase - 0");
        final Mod_2c mod_2c = new Mod_2c();
        page = mod_2c.run(page);

        assertText("id=cc_keyup", "keyup (in_txt_1) fromTestcase - 3");
        final Mod_2b mod_2b = new Mod_2b();
        page = mod_2b.run(page);

        assertText("id=cc_keyup", "keyup (in_txt_1) fromTestcase - 2");
        final Mod_2a mod_2a = new Mod_2a();
        page = mod_2a.run(page);

        assertText("id=cc_keyup", "keyup (in_txt_1) fromTestcase - 1");
        final Mod_3 mod_3 = new Mod_3();
        page = mod_3.run(page);


        setHtmlPage(page);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    protected void postValidate() throws Exception
    {
        final HtmlPage page = getHtmlPage();
        Assert.assertNotNull("Failed to load page", page);


    }
}