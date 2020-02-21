package action.testcases.assertNotValue_actions;

import org.junit.Assert;

import com.xceptance.xlt.api.actions.AbstractHtmlPageAction;
import com.xceptance.xlt.api.engine.scripting.AbstractHtmlUnitScriptAction;
import com.gargoylesoftware.htmlunit.html.HtmlPage;


/**
 * TODO: Add class description
 */
public class assertNotValueAction extends AbstractHtmlUnitScriptAction
{

    /**
     * Constructor.
     * @param prevAction The previous action.
     */
    public assertNotValueAction(final AbstractHtmlPageAction prevAction)
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
        assertNotValue("id=in_ta_1", "regexp:.+");
        // filled textarea
        assertNotValue("id=in_ta_2", "");
        // input with set value
        assertNotValue("id=in_txt_1", "");
        // input with no value
        assertNotValue("id=in_txt_5", "regexp:.+");
        // input with empty value
        assertNotValue("id=in_txt_13", "regexp:.+");
        // hidden input
        assertNotValue("id=invisible_hidden_input", "");
        // hidden input
        assertNotValue("id=invisible_hidden_input", "");
        // display:none
        assertNotValue("id=invisible_display_ancestor", "");
        // visibility:invisible
        assertNotValue("id=invisible_visibility_style_ancestor", "");

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
        assertNotValue("id=in_ta_1", "regexp:.+");
        // filled textarea
        assertNotValue("id=in_ta_2", "");
        // input with set value
        assertNotValue("id=in_txt_1", "");
        // input with no value
        assertNotValue("id=in_txt_5", "regexp:.+");
        // input with empty value
        assertNotValue("id=in_txt_13", "regexp:.+");
        // hidden input
        assertNotValue("id=invisible_hidden_input", "");
        // hidden input
        assertNotValue("id=invisible_hidden_input", "");
        // display:none
        assertNotValue("id=invisible_display_ancestor", "");
        // visibility:invisible
        assertNotValue("id=invisible_visibility_style_ancestor", "");

    }
}