package scripting.pos;

import org.junit.After;
import org.junit.Test;

import com.xceptance.xlt.api.engine.scripting.AbstractWebDriverScriptTestCase;
import com.xceptance.xlt.api.webdriver.XltDriver;
import scripting.util.PageOpener;


/**
 * 
 */
public class AssertText extends AbstractWebDriverScriptTestCase
{

	/**
	 * Constructor.
	 */
	public AssertText()
	{
		super( new XltDriver( true ), null );
	}

	@Test
	public void test() throws Throwable
	{
		PageOpener.examplePage( this );

		// whitespace divs
		assertText( "id=ws2_spaces_only", "          " );
		assertText( "id=ws2_html_spaces_only", "          " );

		// draw border to make 'em visible
		click( "id=ws2_spaces_only_makeVisible" );
		click( "id=ws2_html_spaces_only_makeVisible" );

		// assert again
		assertText( "id=ws2_spaces_only", "          " );
		assertText( "id=ws2_html_spaces_only", "          " );
	}

    @After
    public void after()
    {
        getWebDriver().quit();
    }
}