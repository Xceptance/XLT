package action.modules.selectFrame_actions;

import java.net.URL;
import org.junit.Assert;

import com.xceptance.xlt.api.actions.AbstractHtmlPageAction;
import com.xceptance.xlt.api.engine.scripting.AbstractHtmlUnitScriptAction;
import com.gargoylesoftware.htmlunit.html.HtmlPage;


/**
 * TODO: Add class description
 */
public class open_frame_page extends AbstractHtmlUnitScriptAction
{

    /**
     * Start URL as string.
     */
    private final String urlString;


    /**
     * Start URL as URL object.
     */
    private URL url;

    /**
     * Constructor.
     * @param prevAction The previous action.
     * @param urlString The start URL as string.
     */
    public open_frame_page(final AbstractHtmlPageAction prevAction, final String urlString)
    {
        super(prevAction);
        this.urlString = urlString;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void preValidate() throws Exception
    {
        final String baseURL = getBaseUrl();
        if (baseURL != null && baseURL.trim().length() > 0)
        {
            url = new URL(new URL(baseURL), urlString);
        }
        else
        {
            url = new URL(urlString);
        }

    }


    /**
     * {@inheritDoc}
     */
    @Override
    protected void execute() throws Exception
    {
        HtmlPage page = open(url);
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