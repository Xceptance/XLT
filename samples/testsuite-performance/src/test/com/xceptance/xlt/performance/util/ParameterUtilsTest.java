/**
 * 
 */
package test.com.xceptance.xlt.performance.util;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.xceptance.xlt.api.engine.Session;
import com.xceptance.xlt.performance.util.ParameterUtils;

/**
 * Test for the parameter utils utility class.
 * 
 * @author  Rene Schwietzke
 * 
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(Session.class)
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
		
		// because Mockito cannot mock statics, we need Powermock to spice it up
		PowerMockito.mockStatic(Session.class);
		
		when(Session.getCurrent()).thenReturn(session);
		
		when(session.getUserID()).thenReturn("TTest-01");
		when(session.getUserNumber()).thenReturn(1);
		
		String s2 = ParameterUtils.replaceDynamicParameters(s1);
		
		Assert.assertEquals("Aa TTest-01 aaaaaa 1 aaaaa.", s2);
	}
	
}
