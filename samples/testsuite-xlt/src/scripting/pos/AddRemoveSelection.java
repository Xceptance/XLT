package scripting.pos;

import org.junit.After;
import org.junit.Test;

import com.xceptance.xlt.api.engine.scripting.AbstractWebDriverScriptTestCase;
import com.xceptance.xlt.api.webdriver.XltDriver;
import scripting.util.PageOpener;


/**
 * 
 */
public class AddRemoveSelection extends AbstractWebDriverScriptTestCase
{

	/**
	 * Constructor.
	 */
	public AddRemoveSelection()
	{
		super( new XltDriver( true ), null );
	}

	@Test
	public void test() throws Throwable
	{
		PageOpener.examplePage( this );

		// empty string -> select all options that have no label or label
		// consists of whitespaces only
		{
			// empty
			addSelection( "id=select_18", "" );
			assertText( "id=cc_change", "change (select_18) empty, 1 space, 2 spaces" );
			removeSelection( "id=select_18", "" );
			assertText( "id=cc_change", "change (select_18)" );

			// empty label
			addSelection( "id=select_18", "label=" );
			assertText( "id=cc_change", "change (select_18) empty, 1 space, 2 spaces" );
			removeSelection( "id=select_18", "label=" );
			assertText( "id=cc_change", "change (select_18)" );

			// whitespace label
			addSelection( "id=select_18", "label=  " );
			assertText( "id=cc_change", "change (select_18) empty, 1 space, 2 spaces" );
			removeSelection( "id=select_18", "label=  " );
			assertText( "id=cc_change", "change (select_18)" );
		}
	}

    @After
    public void after()
    {
        getWebDriver().quit();
    }
}