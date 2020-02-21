package com.xceptance.xlt.api.engine.scripting;

import org.openqa.selenium.WebDriver;

/**
 * Defines the operations of custom modules, which can be used in script-based test cases.
 */
public interface WebDriverCustomModule
{
    /**
     * Executes the custom module using the given WebDriver instance.
     * 
     * @param webDriver
     *            the web driver
     * @param parameters
     *            the module parameters
     */
    public void execute(WebDriver webDriver, String... parameters);
}
