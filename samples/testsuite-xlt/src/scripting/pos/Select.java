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
