package action.modules.assertText_actions;

import org.junit.Assert;

import com.xceptance.xlt.api.actions.AbstractHtmlPageAction;
import com.xceptance.xlt.api.engine.scripting.AbstractHtmlUnitScriptAction;
import com.gargoylesoftware.htmlunit.html.HtmlPage;


/**
 * TODO: Add class description
 */
public class matching_strategy0 extends AbstractHtmlUnitScriptAction
{

    /**
     * Constructor.
     * @param prevAction The previous action.
     */
    public matching_strategy0(final AbstractHtmlPageAction prevAction)
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
        assertText("id=sc_s3_2", "glob:foo:bar");
        assertText("id=sc_s3_7", "glob:exact:foobar");
        assertText("id=sc_s3_8", "glob:glob:foobar");
        assertText("id=sc_s3_9", "glob:regexp:foobar");

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

        assertText("id=sc_s3_2", "glob:foo:bar");
        assertText("id=sc_s3_7", "glob:exact:foobar");
        assertText("id=sc_s3_8", "glob:glob:foobar");
        assertText("id=sc_s3_9", "glob:regexp:foobar");

    }
}