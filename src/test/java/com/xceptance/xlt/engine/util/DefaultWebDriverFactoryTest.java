package com.xceptance.xlt.engine.util;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

public class DefaultWebDriverFactoryTest
{
    @Test
    public void testParseBrowserArgs()
    {
        // single parameter flavors
        parseAndTest("a", "a");
        parseAndTest(" a ", "a");
        parseAndTest("-a=bbb", "-a=bbb");
        parseAndTest("-a=a b c", "-a=a", "b", "c");
        parseAndTest("'-a=a b c'", "-a=a b c");
        parseAndTest("\"-a=a b c\"", "-a=a b c");
        parseAndTest("'-a=\"a b c\"'", "-a=\"a b c\"");
        parseAndTest("\"-a='a b c'\"", "-a='a b c'");
        parseAndTest("\"\"", "");
        parseAndTest("''", "");

        // multiple parameters
        parseAndTest(" -a \"-b=a b c\" --ccc-ccc '--d=a b c' -e '' ", "-a", "-b=a b c", "--ccc-ccc", "--d=a b c", "-e", "");

        // special cases with yet undefined behavior
        parseAndTest("aaa'bbb'", "aaa'bbb'");
        parseAndTest("'aaa'bbb", "aaa", "bbb");

        // real-world example
        final String userAgent = "Mozilla/5.0 (Linux; Android 4.2.1; en-us; Nexus 5 Build/JOP40D) AppleWebKit/535.19 (KHTML, like Gecko) Chrome/18.0.1025.166 Mobile Safari/535.19"; 
        final String userAgentArg = "--user-agent=" + userAgent; 
        parseAndTest("\"" + userAgentArg + "\"", userAgentArg);
    }

    private void parseAndTest(final String browserArgs, final String... expectedArgs)
    {
        final List<String> actualArgs = DefaultWebDriverFactory.parseBrowserArgs(browserArgs);

        Assert.assertArrayEquals(expectedArgs, actualArgs.toArray());
    }
}
