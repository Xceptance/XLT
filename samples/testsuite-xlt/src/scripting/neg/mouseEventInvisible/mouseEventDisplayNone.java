package scripting.neg.mouseEventInvisible;

import org.junit.After;
import org.junit.Test;

import scripting.util.PageOpener;

import com.xceptance.xlt.api.engine.scripting.AbstractWebDriverScriptTestCase;
import com.xceptance.xlt.api.util.XltException;
import com.xceptance.xlt.api.webdriver.XltDriver;

/**
 * Mouse events must fail if target is invisible.
 */
public class mouseEventDisplayNone extends AbstractWebDriverScriptTestCase
{
    public static final String TARGET = "id=invisible_anchor_display_none";

    public mouseEventDisplayNone()
    {
        super(new XltDriver(true), null);
    }

    @Test(expected = XltException.class)
    public void mousedOver() throws Exception
    {
        PageOpener.examplePage(this);
        mouseOver(TARGET);
    }

    @Test(expected = XltException.class)
    public void mouseDown() throws Exception
    {
        PageOpener.examplePage(this);
        mouseDown(TARGET);
    }

    @Test(expected = XltException.class)
    public void mouseDownAt() throws Exception
    {
        PageOpener.examplePage(this);
        mouseDownAt(TARGET, "1,1");
    }

    @Test(expected = XltException.class)
    public void mouseMove() throws Exception
    {
        PageOpener.examplePage(this);
        mouseMove(TARGET);
    }

    @Test(expected = XltException.class)
    public void mouseMoveAt() throws Exception
    {
        PageOpener.examplePage(this);
        mouseMoveAt(TARGET, "1,1");
    }

    @Test(expected = XltException.class)
    public void mouseUp() throws Exception
    {
        PageOpener.examplePage(this);
        mouseUp(TARGET);
    }

    @Test(expected = XltException.class)
    public void mouseUpAt() throws Exception
    {
        PageOpener.examplePage(this);
        mouseUpAt(TARGET, "1,1");
    }

    @Test(expected = XltException.class)
    public void mouseOut() throws Exception
    {
        PageOpener.examplePage(this);
        mouseOut(TARGET);
    }

    @After
    public void after()
    {
        getWebDriver().quit();
    }
}
