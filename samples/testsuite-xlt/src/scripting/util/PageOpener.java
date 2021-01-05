/*
 * Copyright (c) 2005-2021 Xceptance Software Technologies GmbH
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
