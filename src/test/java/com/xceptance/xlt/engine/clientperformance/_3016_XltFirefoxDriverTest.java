package com.xceptance.xlt.engine.clientperformance;

import org.openqa.selenium.WebDriver;

import com.xceptance.xlt.api.webdriver.XltFirefoxDriver;

public class _3016_XltFirefoxDriverTest extends _3016_XltChromeDriverTest
{
    @Override
    public String getWebdriverName()
    {
        return "firefox_clientperformance";
    }

    @Override
    public Class<? extends WebDriver> getWebdriverClass()
    {
        return XltFirefoxDriver.class;
    }

}
