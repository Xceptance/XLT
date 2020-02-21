/*
 * File: ScriptExecutionPostponedTest.java
 * Created on: Apr 2, 2014
 * 
 * Copyright 2014
 * Xceptance Software Technologies GmbH, Germany.
 */
package com.gargoylesoftware.htmlunit;

import java.net.URL;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

/**
 * Test that assures postponed execution of &lt;script&gt; elements. See issue #2089 for details.
 */
public class ScriptExecutionPostponedTest
{
    @Test
    public void test() throws Throwable
    {
        final URL u1 = new URL("http://www.example.org/");
        final URL u2 = new URL("http://www.example.org/foo.js");
        final URL u3 = new URL("http://www.example.org/bar.js");
        final MockWebConnection conn = new MockWebConnection();
        conn.setResponse(u1,
                         "<html><body><h1>Hello World!</h1>"
                             + "<script>"
                             + "window.onload = function(){ var e = document.createElement('script'); e.src = 'foo.js'; document.body.appendChild(e); }"
                             + "</script></body></html>");
        conn.setResponse(u2,
                         "(function(app){"
                             + "function f(){ var e = document.createElement('script');e.src='bar.js'; document.body.appendChild(e); this._created = true }"
                             + "app.TestObj = new f();" + "})(window.app = window.app || {})", "text/javascript");
        conn.setResponse(u3, "(function(){ alert(window.app.TestObj._created) })()", "text/javascript");

        try (final WebClient wc = new WebClient(BrowserVersion.CHROME))
        {
            wc.getOptions().setJavaScriptEnabled(true);
            wc.setWebConnection(conn);

            final CollectingAlertHandler alertHandler = new CollectingAlertHandler();
            wc.setAlertHandler(alertHandler);

            wc.getPage(u1);

            final List<String> alerts = alertHandler.getCollectedAlerts();
            Assert.assertEquals(1, alerts.size());
            Assert.assertEquals("true", alerts.get(0));
        }
    }
}
