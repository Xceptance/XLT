/*
 * Copyright (c) 2005-2025 Xceptance Software Technologies GmbH
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
package scripting.neg.assertNotText;

import org.junit.Test;
import org.junit.After;

import com.xceptance.xlt.api.engine.scripting.AbstractWebDriverScriptTestCase;
import com.xceptance.xlt.api.webdriver.XltDriver;
import scripting.util.PageOpener;
import scripting.util.Visibility;


/**
 * 
 */
public class AssertNotText_emptyDiv_empty extends AbstractWebDriverScriptTestCase
{

	/**
	 * Constructor.
	 */
	public AssertNotText_emptyDiv_empty()
	{
		super(new XltDriver(true), null);
	}

	/**
	 * 
	 * @throws Throwable
	 */
	@Test(expected = AssertionError.class)
	public void test() throws Throwable
	{
		PageOpener.examplePage( this );
		Visibility.showEmptyDiv( this );
		assertNotText( "id=invisible_empty_div", "" );
    }

    @After
    public void after()
    {
        getWebDriver().quit();
    }
}