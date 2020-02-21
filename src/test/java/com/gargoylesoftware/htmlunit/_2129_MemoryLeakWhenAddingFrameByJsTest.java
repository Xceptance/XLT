package com.gargoylesoftware.htmlunit;

import java.io.IOException;

import org.junit.Ignore;
import org.junit.Test;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.WebClient;

/**
 * @see https://lab.xceptance.de/issues/2129
 * @see http://sourceforge.net/p/htmlunit/bugs/1604/
 */
public class _2129_MemoryLeakWhenAddingFrameByJsTest
{
    @Test
    @Ignore("To be run manually only")
    public void test() throws IOException, InterruptedException
    {
        for (int i = 0; i < 10000; i++)
        {
            System.err.printf("### %d\n", i);

            WebClient webClient = new WebClient(BrowserVersion.CHROME);
            webClient.getPage(getClass().getResource(getClass().getSimpleName() + ".html"));
            webClient.close();
        }
    }
}
