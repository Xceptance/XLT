/*
 * Copyright (c) 2005-2023 Xceptance Software Technologies GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
