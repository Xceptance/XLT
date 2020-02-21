/*
 * File: _2897_ClickOnDisabledElementTest.java
 * Created on: Apr 10, 2017
 */
package com.xceptance.xlt.engine.xltdriver;

import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.InvalidElementStateException;

import com.gargoylesoftware.htmlunit.MockWebConnection;
import com.xceptance.xlt.api.webdriver.XltDriver;

/**
 * Test for issue 2897 (click on disabled element throws {@link InvalidElementStateException}).
 */
public class _2897_ClickOnDisabledElementTest
{
    @Test(expected = org.openqa.selenium.InvalidElementStateException.class)
    public void ensureInvalidElementStateException() throws Throwable
    {
        final String html = "<form action='/foo' method='POST'><input type=hidden name=bar value=bum />" +
                            "<button disabled name=submit value=submit id=btn>Click Me</button></form>";
        final XltDriver driver = new XltDriver(false);
        final MockWebConnection mockConn = new MockWebConnection();
        mockConn.setDefaultResponse(html);

        driver.getWebClient().setWebConnection(mockConn);

        driver.get("http://example.org");

        driver.findElement(By.id("btn")).click();
    }
}
