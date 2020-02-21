package com.gargoylesoftware.htmlunit;

import java.io.IOException;
import java.net.ServerSocket;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * @see https://lab.xceptance.de/issues/2090
 * @see https://sourceforge.net/p/htmlunit/bugs/1593/
 */
public class _2090_SocketTimeoutDuringConnectTest
{
    @Before
    public void setUp()
    {
        // create a non-responding local server
        new Thread()
        {
            public void run()
            {
                try (final ServerSocket serverSocket = new ServerSocket(12345))
                {
                    serverSocket.accept();
                }
                catch (IOException ex)
                {
                }
            }
        }.start();
    }

    @Test
    @Ignore("To be run manually only")
    public void test() throws Throwable
    {
        try (final WebClient wc = new WebClient(BrowserVersion.CHROME))
        {
            wc.getOptions().setUseInsecureSSL(true);
            wc.getOptions().setTimeout(5000);

            wc.getPage("https://127.0.0.1:12345/");
        }
    }
}
