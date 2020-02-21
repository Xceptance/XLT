package scripting.util;

import com.xceptance.xlt.api.engine.scripting.AbstractWebDriverScriptTestCase;

public class Title
{
	public static void change(AbstractWebDriverScriptTestCase testCase) throws IllegalStateException
	{
		try
		{
			testCase.click( "id=title_change" );
		} catch ( Exception e )
		{
			throw new IllegalStateException();
		}
	}

	public static void empty(AbstractWebDriverScriptTestCase testCase) throws IllegalStateException
	{
		try
		{
			testCase.click( "id=title_empty" );
		} catch ( Exception e )
		{
			throw new IllegalStateException();
		}
	}

	public static void remove(AbstractWebDriverScriptTestCase testCase) throws IllegalStateException
	{
		try
		{
			testCase.click( "id=title_remove" );
			testCase.assertNotElementPresent( "xpath=//title" );
		} catch ( Exception e )
		{
			throw new IllegalStateException();
		}
	}
}
