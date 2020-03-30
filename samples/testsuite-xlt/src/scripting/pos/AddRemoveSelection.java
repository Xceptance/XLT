/*
 * Copyright (c) 2005-2020 Xceptance Software Technologies GmbH
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