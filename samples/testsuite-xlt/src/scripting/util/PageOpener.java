package scripting.util;

import com.xceptance.xlt.api.engine.scripting.AbstractWebDriverScriptTestCase;

public class PageOpener
{
	public static void examplePage(AbstractWebDriverScriptTestCase testCase) throws IllegalStateException
	{
		try
		{
			testCase.open( "http://localhost:8080/testpages/examplePage_1.html" );
			testCase.assertText( "id=page_headline", "Example Page" );
		} catch ( Exception e )
		{
			throw new IllegalStateException();
		}
	}

	public static void exampleFrame(AbstractWebDriverScriptTestCase testCase) throws IllegalStateException
	{
		try
		{
			testCase.open( "http://localhost:8080/testpages/frame.html" );
			testCase.assertTitle( "frame_1" );
		} catch ( Exception e )
		{
			throw new IllegalStateException();
		}
	}
}
