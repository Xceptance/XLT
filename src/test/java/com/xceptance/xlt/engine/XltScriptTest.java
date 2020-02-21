package com.xceptance.xlt.engine;

import net.sourceforge.htmlunit.corejs.javascript.Context;
import net.sourceforge.htmlunit.corejs.javascript.Script;
import net.sourceforge.htmlunit.corejs.javascript.Scriptable;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Sebastian Oerding
 */
public class XltScriptTest
{
    @Test
    public void test()
    {
        final String expected = "Artifical implementation only for testing purpose";
        final Script dummy = new Script()
        {
            @Override
            public Object exec(final Context cx, final Scriptable scope)
            {
                return null;
            }

            @Override
            public String toString()
            {
                return expected;
            }
        };
        final XltScript script = new XltScript(dummy, "bla");
        Assert.assertSame("Wrong wrapped script, ", dummy, script.getWrappedScript());
        Assert.assertEquals("Wrong source name, ", "bla", script.getSourceName());
        Assert.assertEquals("Script is not returned as expected", expected, script.toString());
    }
}
