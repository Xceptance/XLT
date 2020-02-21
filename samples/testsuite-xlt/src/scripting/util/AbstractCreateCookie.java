package scripting.util;

import org.openqa.selenium.WebDriver;

import com.xceptance.xlt.api.engine.scripting.AbstractWebDriverScriptTestCase;
import scripting.modules.AssertCookie;

public abstract class AbstractCreateCookie extends AbstractWebDriverScriptTestCase
{
	public AbstractCreateCookie( final WebDriver driver, final String baseUrl )
	{
		super( driver, baseUrl );
	}

	public void create(final String name, final String value) throws IllegalStateException, Exception
	{
		create( name, value, null );
	}

	public void create(final String name, final String value, final String options) throws IllegalStateException, Exception
	{
		cleanup( name );

		// create
		if ( options != null )
		{
			createCookie( name + "=" + value, options );
		} else
		{
			createCookie( name + "=" + value );
		}

		// check
		final AssertCookie assertCookie = new AssertCookie();
		assertCookie.execute( name, value );

		cleanup( name );
	}

	private void cleanup(final String name) throws IllegalStateException
	{
		try
		{
			deleteCookie( name );
		} catch ( Exception e )
		{
			throw new IllegalStateException();
		}
	}
}
