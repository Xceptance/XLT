package scripting.pos;

import org.junit.After;
import org.junit.Test;

import com.xceptance.xlt.api.engine.scripting.AbstractWebDriverScriptTestCase;
import com.xceptance.xlt.api.webdriver.XltDriver;
import scripting.util.PageOpener;


/**
 * 
 */
public class Select extends AbstractWebDriverScriptTestCase
{

	/**
	 * Constructor.
	 */
	public Select()
	{
		super( new XltDriver( true ), null );
	}

	@Test
	public void test() throws Throwable
	{
		PageOpener.examplePage( this );

		{
			// implicit empty label
			select( "id=select_17", "" );
			assertText( "id=cc_change", "change (select_17) empty" );
		}
		reset();
		{
			// 1 whitespace
			select( "id=select_17", " " );
			assertText( "id=cc_change", "change (select_17) empty" );
		}
		reset();
		{
			// 2 whitespaces
            select( "id=select_17", "  " );
            assertText( "id=cc_change", "change (select_17) empty" );
        }
	}
	
	private void reset()
	{
		select( "id=select_17", ":" );
		assertText( "id=cc_change", "change (select_17) :" );
	}

    @After
    public void after()
    {
        getWebDriver().quit();
    }
}
