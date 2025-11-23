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
/**
 * 
 */
package test.com.xceptance.xlt.performance.util;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import com.xceptance.xlt.api.engine.Session;
import com.xceptance.xlt.performance.util.ParameterUtils;

/**
 * Test for the parameter utils utility class.
 * 
 * @author  Rene Schwietzke
 * 
 */
public class ParameterUtilsTest
{
	/**
	 * Test method for {@link com.xceptance.xlt.performance.util.ParameterUtils#replaceDynamicParameters(java.lang.String)}.
	 */
	@Test
	public final void testReplaceDynamicParameters_None()
	{
		final String s1 = "This is a string without parameters.";
		String s2 = ParameterUtils.replaceDynamicParameters(s1);
		Assert.assertEquals(s1, s2);
	}

	@Test
	public final void testReplaceDynamicParameters_Unknown()
	{
		final String s1 = "This is a string {something:0} parameters.";
		String s2 = ParameterUtils.replaceDynamicParameters(s1);
		Assert.assertEquals(s1, s2);
	}

	@Test
	public final void testReplaceDynamicParameters_OneRandom()
	{
		final String s1 = "Aa aaa aaaaaa {random:1} aaaaa.";
		String s2 = ParameterUtils.replaceDynamicParameters(s1);
		
		Assert.assertTrue(s2.matches("Aa aaa aaaaaa .{1} aaaaa."));
	}

	@Test
	public final void testReplaceDynamicParameters_TwoRandom()
	{
		final String s1 = "Aa {random:10} aaaaaa {random:1} aaaaa.";
		String s2 = ParameterUtils.replaceDynamicParameters(s1);
		
		Assert.assertTrue(s2.matches("Aa .{10} aaaaaa .{1} aaaaa."));
	}
	
	@Test
	public final void testReplaceSessionParameters()
	{
		final String s1 = "Aa {userID} aaaaaa {userNumber} aaaaa.";
		
		Session session = mock(Session.class);
		
		when(session.getUserID()).thenReturn("TTest-01");
		when(session.getUserNumber()).thenReturn(1);

		try (MockedStatic<Session> sessionMock = Mockito.mockStatic(Session.class))
		{
		sessionMock.when(Session::getCurrent).thenReturn(session);

		String s2 = ParameterUtils.replaceDynamicParameters(s1);

		Assert.assertEquals("Aa TTest-01 aaaaaa 1 aaaaa.", s2);
        }
	}
	
}
