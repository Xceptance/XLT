package scripting.util;

import com.xceptance.xlt.api.engine.scripting.AbstractWebDriverScriptTestCase;

public class Visibility
{
	public static void showEmptyDiv(AbstractWebDriverScriptTestCase testCase) throws IllegalStateException
	{
		try
		{
			testCase.assertElementPresent( "id=invisible_empty_div" );
			testCase.click( "id=invisible_showEmptyDiv" );
		} catch ( Exception e )
		{
			throw new IllegalStateException();
		}
	}

	public static void showSpacesDiv(AbstractWebDriverScriptTestCase testCase) throws IllegalStateException
	{
		try
		{
			testCase.assertElementPresent( "id=ws2_spaces_only" );
			testCase.click( "id=ws2_spaces_only_makeVisible" );
		} catch ( Exception e )
		{
			throw new IllegalStateException();
		}
	}

	public static void showHtmlSpacesDiv(AbstractWebDriverScriptTestCase testCase) throws IllegalStateException
	{
		try
		{
			testCase.assertElementPresent( "id=ws2_html_spaces_only" );
			testCase.click( "id=ws2_html_spaces_only_makeVisible" );
		} catch ( Exception e )
		{
			throw new IllegalStateException();
		}
	}
}
