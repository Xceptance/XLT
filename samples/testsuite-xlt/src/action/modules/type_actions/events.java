package action.modules.type_actions;

import org.junit.Assert;

import com.xceptance.xlt.api.actions.AbstractHtmlPageAction;
import com.xceptance.xlt.api.engine.scripting.AbstractHtmlUnitScriptAction;
import com.gargoylesoftware.htmlunit.html.HtmlPage;


/**
 * TODO: Add class description
 */
public class events extends AbstractHtmlUnitScriptAction
{

    /**
     * Constructor.
     * @param prevAction The previous action.
     */
    public events(final AbstractHtmlPageAction prevAction)
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

    }


    /**
     * {@inheritDoc}
     */
    @Override
    protected void execute() throws Exception
    {
        HtmlPage page = getPreviousAction().getHtmlPage();
        page = type("id=in_txt_1", "foo");
        // for change and blur event
        page = click("xpath=/html/body");
        assertText("id=cc_focus", "focus (in_txt_1)*");
        assertText("id=cc_keydown", "keydown (in_txt_1) fo");
        assertText("id=cc_keyup", "keyup (in_txt_1) foo");
        assertText("id=cc_keypress", "keypress (in_txt_1) fo");
        assertText("id=cc_change", "change (in_txt_1) foo");
        assertText("id=cc_blur", "blur (in_txt_1) foo");
        // page = type("id=fileInput","c:\\bar");
        // page = type("id=fileInput","/home/hardy/Desktop/foo.js");
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

        // assertText("id=cc_change","change (fileInput)*");
    }
}