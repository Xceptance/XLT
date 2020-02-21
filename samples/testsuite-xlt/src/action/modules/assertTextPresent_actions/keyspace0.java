package action.modules.assertTextPresent_actions;

import org.junit.Assert;

import com.xceptance.xlt.api.actions.AbstractHtmlPageAction;
import com.xceptance.xlt.api.engine.scripting.AbstractHtmlUnitScriptAction;
import com.gargoylesoftware.htmlunit.html.HtmlPage;


/**
 * TODO: Add class description
 */
public class keyspace0 extends AbstractHtmlUnitScriptAction
{

    /**
     * Constructor.
     * @param prevAction The previous action.
     */
    public keyspace0(final AbstractHtmlPageAction prevAction)
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
        assertTextPresent("glob:1234567890 qwertzuiop asdfghjkl yxcvbnm QWERTZUIOP ASDFGHJKL YXCVBNM äöü ÄÖÜ ß !\"§$%&/()=? `´^° <> ,;.:-_ #'+*²³{[]}\\ @€~ |µ ©«» ¼×");
        assertTextPresent("glob:1234567890 qwertzuiop asdfghjkl yxcvbnm QWERTZUIOP ASDFGHJKL YXCVBNM äöü ÄÖÜ ß !\"§$%&/()=? `´^° <> ,;.:-_ #'+*²³{[]}\\ @€~ |µ ©«» ¼×");
        assertTextPresent("glob:1234567890 qwertzuiop asdfghjkl yxcvbnm QWERTZUIOP ASDFGHJKL YXCVBNM äöü ÄÖÜ ß !\"§$%&/()=? `´^° <> ,;.:-_ #'+*²³{[]}\\ @€~ |µ ©«» ¼×");

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

        assertTextPresent("glob:1234567890 qwertzuiop asdfghjkl yxcvbnm QWERTZUIOP ASDFGHJKL YXCVBNM äöü ÄÖÜ ß !\"§$%&/()=? `´^° <> ,;.:-_ #'+*²³{[]}\\ @€~ |µ ©«» ¼×");
        assertTextPresent("glob:1234567890 qwertzuiop asdfghjkl yxcvbnm QWERTZUIOP ASDFGHJKL YXCVBNM äöü ÄÖÜ ß !\"§$%&/()=? `´^° <> ,;.:-_ #'+*²³{[]}\\ @€~ |µ ©«» ¼×");
        assertTextPresent("glob:1234567890 qwertzuiop asdfghjkl yxcvbnm QWERTZUIOP ASDFGHJKL YXCVBNM äöü ÄÖÜ ß !\"§$%&/()=? `´^° <> ,;.:-_ #'+*²³{[]}\\ @€~ |µ ©«» ¼×");

    }
}