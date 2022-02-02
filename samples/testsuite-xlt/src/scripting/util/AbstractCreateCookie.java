/*
 * Copyright (c) 2005-2022 Xceptance Software Technologies GmbH
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
