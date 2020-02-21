package action.testcases.assertValue_actions;

import org.junit.Assert;

import com.xceptance.xlt.api.actions.AbstractHtmlPageAction;
import com.xceptance.xlt.api.engine.scripting.AbstractHtmlUnitScriptAction;
import com.gargoylesoftware.htmlunit.html.HtmlPage;


/**
 * TODO: Add class description
 */
public class assertValueAction extends AbstractHtmlUnitScriptAction
{

    /**
     * Constructor.
     * @param prevAction The previous action.
     */
    public assertValueAction(final AbstractHtmlPageAction prevAction)
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
        // empty textarea
        assertValue("id=in_ta_1", "");
        // filled textarea
        assertValue("id=in_ta_2", "in_ta_2");
        // input with set value
        assertValue("id=in_txt_1", "in_txt_1");
        // input with no value
        assertValue("id=in_txt_5", "");
        // input with empty value
        assertValue("id=in_txt_13", "");
        // hidden input
        assertValue("id=invisible_hidden_input", "invisible_hidden_input");
        // display:none
        assertValue("id=invisible_display_ancestor", "invisible_display_ancestor");
        // visibility:invisible
        assertValue("id=invisible_visibility_style_ancestor", "invisible_visibility_style_ancestor");
        // misc
        assertValue("xpath=/html/body", "");

    }


    /**
     * {@inheritDoc}
     */
    @Override
    protected void execute() throws Exception
    {
        HtmlPage page = getPreviousAction().getHtmlPage();

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

        // empty textarea
        assertValue("id=in_ta_1", "");
        // filled textarea
        assertValue("id=in_ta_2", "in_ta_2");
        // input with set value
        assertValue("id=in_txt_1", "in_txt_1");
        // input with no value
        assertValue("id=in_txt_5", "");
        // input with empty value
        assertValue("id=in_txt_13", "");
        // hidden input
        assertValue("id=invisible_hidden_input", "invisible_hidden_input");
        // display:none
        assertValue("id=invisible_display_ancestor", "invisible_display_ancestor");
        // visibility:invisible
        assertValue("id=invisible_visibility_style_ancestor", "invisible_visibility_style_ancestor");
        // misc
        assertValue("xpath=/html/body", "");

    }
}