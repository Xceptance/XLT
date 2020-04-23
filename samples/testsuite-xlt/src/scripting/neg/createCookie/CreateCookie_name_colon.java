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
package scripting.neg.createCookie;

import org.junit.After;
import org.junit.Test;

import scripting.util.PageOpener;

import com.xceptance.xlt.api.engine.scripting.AbstractWebDriverScriptTestCase;
import com.xceptance.xlt.api.util.XltException;
import com.xceptance.xlt.api.webdriver.XltDriver;


/**
 * 
 */
public class CreateCookie_name_colon extends AbstractWebDriverScriptTestCase
{
	/**
	 * Constructor.
	 */
	public CreateCookie_name_colon()
	{
		super(new XltDriver(true), null);
	}

	@Test(expected = XltException.class)
	public void test() throws Throwable
	{
		PageOpener.examplePage( this );
		createCookie( "foo:bar=foofoo" );
    }

    @After
    public void after()
    {
        getWebDriver().quit();
    }
}