package action.testcases.storeEval_actions;

import org.junit.Assert;

import com.xceptance.xlt.api.actions.AbstractHtmlPageAction;
import com.xceptance.xlt.api.engine.scripting.AbstractHtmlUnitScriptAction;
import com.gargoylesoftware.htmlunit.html.HtmlPage;


/**
 * TODO: Add class description
 */
public class storeEvalAction extends AbstractHtmlUnitScriptAction
{

    /**
     * Constructor.
     * @param prevAction The previous action.
     */
    public storeEvalAction(final AbstractHtmlPageAction prevAction)
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
        storeEval("(function(){\nvar e = document.getElementById('specialchar_1');\nreturn e != null ? e.textContent : '';\n})();", "storeEval_1");
        assertText("id=specialchar_1", resolve("${storeEval_1}"));
        assertNotText("id=specialchar_2", resolve("${storeEval_1}"));
        storeText("css=#priceText > span", "price");
        storeEval(resolve("'${price}'.replace(/([\\/\\\\^$*+?.()|[\\]{}])/g, '\\\\$1')"), "priceRex");

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

        assertText("id=priceText", resolve("regexpi:.*${priceRex}"));

    }
}