/*
 * Copyright (c) 2005-2024 Xceptance Software Technologies GmbH
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
package com.xceptance.xlt.engine.scripting.webdriver;

import java.lang.reflect.Proxy;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;

import org.apache.commons.lang3.StringUtils;
import org.htmlunit.util.UrlUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.HasCapabilities;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoSuchWindowException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.interactions.Interactive;
import org.openqa.selenium.interactions.Locatable;
import org.openqa.selenium.remote.CapabilityType;

import com.xceptance.xlt.api.util.XltProperties;
import com.xceptance.xlt.api.webdriver.XltDriver;
import com.xceptance.xlt.common.XltConstants;
import com.xceptance.xlt.engine.TimeoutException;
import com.xceptance.xlt.engine.scripting.CookieConstants;
import com.xceptance.xlt.engine.scripting.PageLoadTimeoutException;
import com.xceptance.xlt.engine.scripting.TestContext;
import com.xceptance.xlt.engine.scripting.util.AbstractCommandAdapter;
import com.xceptance.xlt.engine.scripting.util.Condition;
import com.xceptance.xlt.engine.scripting.util.ReplayUtils;

/**
 * Command adapter.
 * 
 * @author Hartmut Arlt (Xceptance Software Technologies GmbH)
 */
public final class WebDriverCommandAdapter extends AbstractCommandAdapter implements WebDriverScriptCommands
{
    /**
     * The JavaScript snippet used to evaluate an expression.
     */
    private static final String EVAL_SCRIPT = "var r = null; try { r = eval(arguments[0]); } catch(e){ throw new Error(String(e)) } return String(r)";

    /**
     * The name of the 'setPageLoadTimeoutAtDriver' property.
     */
    private static final String PROP_SET_PAGE_LOAD_TIMEOUT = XltConstants.XLT_PACKAGE_PATH + ".scripting.setPageLoadTimeoutAtDriver";

    /**
     * Creates a {@link WebDriverCommandAdapter} object and returns it proxied. The proxy object passes method calls on
     * to the internal command adapter, but adds cross-cutting functionality.
     * 
     * @param webDriver
     *            the underlying web driver
     * @param baseUrl
     *            the base URL
     * @return the proxied command adapter
     */
    public static WebDriverScriptCommands createInstance(final WebDriver webDriver, final String baseUrl)
    {
        final WebDriverCommandAdapter adapter = new WebDriverCommandAdapter(webDriver, baseUrl);
        final WebDriverScriptCommandsInvocationHandler handler = new WebDriverScriptCommandsInvocationHandler(adapter,
                                                                                                              AbstractCommandAdapter.LOGGER);
        final Class<?>[] proxiedInterfaces =
            {
                WebDriverScriptCommands.class
            };

        return (WebDriverScriptCommands) Proxy.newProxyInstance(WebDriverCommandAdapter.class.getClassLoader(), proxiedInterfaces, handler);
    }

    /**
     * The finder to use.
     */
    private final WebDriverFinder finder = new WebDriverFinder();

    /**
     * The handle of the current web driver window when this class was initialized.
     */
    private final String originalWindowHandle;

    /**
     * The web driver to use.
     */
    private final WebDriver webDriver;

    /**
     * The test's base URL.
     */
    private final String baseUrl;

    /**
     * Whether or not the driver will wait for page loads.
     */
    private final boolean driverWaitsForPageLoad;

    /**
     * Whether or not the script timeout will also be set as page load timeout at the driver.
     */
    private final boolean pageLoadTimeoutAtDriverEnabled;

    /**
     * Constructor.
     * 
     * @param webDriver
     *            the underlying web driver
     * @param baseUrl
     *            the base URL
     */
    private WebDriverCommandAdapter(final WebDriver webDriver, final String baseUrl)
    {
        this.webDriver = webDriver;
        this.baseUrl = baseUrl;

        // TODO: What happens if the original window is closed later on?
        originalWindowHandle = webDriver.getWindowHandle();

        // whether the driver or XLT will wait for page loads
        driverWaitsForPageLoad = isDriverWaitingForPageLoad(webDriver);

        if (!driverWaitsForPageLoad)
        {
            // give a hint that the non-standard settings are effective
            LOGGER.info("Driver will *not* wait for page loads, but the XLT scripting layer will");
        }

        // whether or not to set a page load timeout at the driver
        pageLoadTimeoutAtDriverEnabled = XltProperties.getInstance().getProperty(PROP_SET_PAGE_LOAD_TIMEOUT, true);
        if (!pageLoadTimeoutAtDriverEnabled)
        {
            // give a hint that the non-standard settings are effective
            LOGGER.info("Script timeout will *not* be set as page load timeout at the driver");
        }

        // set initial timeouts
        setTimeout(TestContext.getDefaultTimeout());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public WebDriver getUnderlyingWebDriver()
    {
        return webDriver;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public WebElement findElement(String elementLocator)
    {
        return finder.findElement(webDriver, elementLocator);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<WebElement> findElements(String elementLocator)
    {
        return finder.findElements(webDriver, elementLocator);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addSelection(final String selectLocator, final String optionLocator)
    {
        checkElementLocator(selectLocator);

        final WebElement select = finder.findElement(webDriver, selectLocator);

        checkIsEqual("Element '" + selectLocator + "' is not an HTML select element", "select", select.getTagName());
        checkIsTrue("Select '" + selectLocator + "' does not support multiple selection", isMultipleSelect(select));
        checkIsTrue("Select '" + selectLocator + "' is disabled", select.isEnabled());

        final List<WebElement> options = finder.findOptions(select, optionLocator);
        for (final WebElement option : options)
        {
            setSelected(select, option);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void check(final String elementLocator)
    {
        checkElementLocator(elementLocator);

        final WebElement input = finder.findElement(webDriver, elementLocator);

        checkIsEqual("Element '" + elementLocator + "' is not a HTML input element", "input", input.getTagName());
        final String typeAttribute = input.getAttribute("type");
        checkIsTrue("Check is only allowed on radio/checkbox input elements",
                    typeAttribute.equals("radio") || typeAttribute.equals("checkbox"));
        checkIsTrue("Radio/checkbox '" + elementLocator + "' is disabled", input.isEnabled());

        WebDriverUtils.assumeOkOnAlertOrConfirm(webDriver);
        if (!input.isSelected())
        {
            if (WebDriverUtils.isClickable(webDriver, input))
            {
                input.click();
            }
            else
            {
                WebDriverUtils.setAttribute(webDriver, input, "checked", "true");
                WebDriverUtils.fireChangeEvent(webDriver, input);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void checkAndWait(final String elementLocator)
    {
        executeCommandAndWait(() -> check(elementLocator));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void click(final String elementLocator)
    {
        checkElementLocator(elementLocator);

        WebDriverUtils.assumeOkOnAlertOrConfirm(webDriver);

        final WebElement e = finder.findElement(webDriver, elementLocator);
        if (WebDriverUtils.isClickable(webDriver, e))
        {
            e.click();
        }
        else
        {
            WebDriverUtils.fireClickEvent(webDriver, e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void clickAndWait(final String elementLocator)
    {
        executeCommandAndWait(() -> click(elementLocator));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close()
    {
        webDriver.close();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void contextMenu(final String elementLocator)
    {
        checkElementLocator(elementLocator);

        WebDriverUtils.assumeOkOnAlertOrConfirm(webDriver);

        final WebElement element = finder.findElement(webDriver, elementLocator);
        checkIsTrue("Cannot interact with invisible elements", element.isDisplayed());

        new Actions(webDriver).contextClick(element).build().perform();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void contextMenuAt(final String elementLocator, final String coordinates)
    {
        final int[] offset = ReplayUtils.parseCoordinates(coordinates);
        checkIsTrue("Invalid coordinates: " + coordinates, offset != null);

        contextMenuAt(elementLocator, offset[0], offset[1]);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void contextMenuAt(final String elementLocator, final int coordX, final int coordY)
    {
        checkElementLocator(elementLocator);

        WebDriverUtils.assumeOkOnAlertOrConfirm(webDriver);

        final WebElement element = finder.findElement(webDriver, elementLocator);
        checkIsTrue("Cannot interact with invisible elements", element.isDisplayed());

        new Actions(webDriver).moveToElement(element, coordX, coordY).contextClick().build().perform();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void createCookie(final String cookie)
    {
        createCookie(cookie, "");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void createCookie(final String cookie, final String options)
    {
        final Matcher m = CookieConstants.NAME_VALUE_PAIR_PATTERN.matcher(cookie);
        checkIsTrue("Invalid cookie string: " + cookie, m.matches());

        final String cookieName = m.group(1);
        final String cookieValue = m.group(2);

        final Matcher maxAgeMatcher = CookieConstants.MAX_AGE_PATTERN.matcher(options);
        final Date maxAge;
        if (maxAgeMatcher.find())
        {
            maxAge = new Date(System.currentTimeMillis() + Integer.parseInt(maxAgeMatcher.group(1)) * 1000);
        }
        else
        {
            maxAge = null;
        }

        String path = null;
        final Matcher pathMatcher = CookieConstants.PATH_PATTERN.matcher(options);
        if (pathMatcher.find())
        {
            path = pathMatcher.group(1);
            try
            {
                if (path.startsWith("http"))
                {
                    path = new URL(path).getPath();
                }
            }
            catch (final MalformedURLException e)
            {
                // Fine.
                // TODO: Really fine? Check this.
            }
        }

        // TODO: support domain/secure attributes?
        // final String domain = null;
        // final boolean secure = false;
        // final Cookie cookie2 = new Cookie(cookieName, cookieValue, domain, path, maxAge, secure);

        final Cookie cookie2 = new Cookie(cookieName, cookieValue, path, maxAge);
        webDriver.manage().addCookie(cookie2);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteAllVisibleCookies()
    {
        webDriver.manage().deleteAllCookies();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteCookie(final String name)
    {
        deleteCookie(name, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteCookie(final String name, final String options)
    {
        checkIsTrue("Invalid cookie name: " + name, CookieConstants.NAME_PATTERN.matcher(name).find());
        webDriver.manage().deleteCookieNamed(name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void doubleClick(final String elementLocator)
    {
        checkIsTrue("Current webdriver has no input devices: cannot double-click", webDriver instanceof Interactive);
        checkElementLocator(elementLocator);

        WebDriverUtils.assumeOkOnAlertOrConfirm(webDriver);

        final WebElement e = finder.findElement(webDriver, elementLocator);
        checkIsTrue("Cannot double-click on element '" + elementLocator + "' since it is not locatable", e instanceof Locatable);

        new Actions(webDriver).doubleClick(e).perform();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void doubleClickAndWait(final String elementLocator)
    {
        executeCommandAndWait(() -> doubleClick(elementLocator));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void mouseDown(final String elementLocator)
    {
        checkElementLocator(elementLocator);

        final WebElement element = finder.findElement(webDriver, elementLocator);
        checkIsTrue("Cannot interact with invisible elements", element.isDisplayed());

        new Actions(webDriver).clickAndHold(element).build().perform();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void mouseDownAt(final String elementLocator, final String coordinates)
    {
        final int[] offset = ReplayUtils.parseCoordinates(coordinates);
        checkIsTrue("Invalid coordinates: " + coordinates, offset != null);

        mouseDownAt(elementLocator, offset[0], offset[1]);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void mouseDownAt(final String elementLocator, final int coordX, final int coordY)
    {
        checkElementLocator(elementLocator);

        final WebElement element = finder.findElement(webDriver, elementLocator);
        checkIsTrue("Cannot interact with invisible elements", element.isDisplayed());

        new Actions(webDriver).moveToElement(element, coordX, coordY).clickAndHold().build().perform();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void mouseMove(final String elementLocator)
    {
        checkElementLocator(elementLocator);

        final WebElement element = finder.findElement(webDriver, elementLocator);
        checkIsTrue("Cannot interact with invisible elements", element.isDisplayed());

        new Actions(webDriver).moveToElement(element).build().perform();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void mouseMoveAt(final String elementLocator, final String coordinates)
    {
        final int[] offset = ReplayUtils.parseCoordinates(coordinates);
        checkIsTrue("Invalid coordinates: " + coordinates, offset != null);

        mouseMoveAt(elementLocator, offset[0], offset[1]);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void mouseMoveAt(final String elementLocator, final int coordX, final int coordY)
    {
        checkElementLocator(elementLocator);

        final WebElement element = finder.findElement(webDriver, elementLocator);
        checkIsTrue("Cannot interact with invisible elements", element.isDisplayed());

        new Actions(webDriver).moveToElement(element, coordX, coordY).build().perform();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void mouseOut(final String elementLocator)
    {
        checkElementLocator(elementLocator);

        final WebElement element = finder.findElement(webDriver, elementLocator);
        checkIsTrue("Cannot interact with invisible elements", element.isDisplayed());

        // TODO: may not be safe if the actual target element spans the whole screen
        mouseOver("xpath=/html/body");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void mouseOver(final String elementLocator)
    {
        checkElementLocator(elementLocator);

        final WebElement element = finder.findElement(webDriver, elementLocator);
        checkIsTrue("Cannot interact with invisible elements", element.isDisplayed());

        new Actions(webDriver).moveToElement(element).build().perform();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void mouseUp(final String elementLocator)
    {
        checkElementLocator(elementLocator);

        final WebElement element = finder.findElement(webDriver, elementLocator);
        checkIsTrue("Cannot interact with invisible elements", element.isDisplayed());

        new Actions(webDriver).release(element).build().perform();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void mouseUpAt(final String elementLocator, final String coordinates)
    {
        final int[] offset = ReplayUtils.parseCoordinates(coordinates);
        checkIsTrue("Invalid coordinates: " + coordinates, offset != null);

        mouseUpAt(elementLocator, offset[0], offset[1]);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void mouseUpAt(final String elementLocator, final int coordX, final int coordY)
    {
        checkElementLocator(elementLocator);

        final WebElement element = finder.findElement(webDriver, elementLocator);
        checkIsTrue("Cannot interact with invisible elements", element.isDisplayed());

        new Actions(webDriver).moveToElement(element, coordX, coordY).release().build().perform();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void open(final String url)
    {
        executeCommandAndWait(() -> {
            final String urlString = baseUrl != null ? UrlUtils.resolveUrl(baseUrl, url) : url;
            webDriver.get(rewriteUrl(urlString).toString());
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void open(final URL url)
    {
        open(url.toString());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void pause(final long waitingTime)
    {
        try
        {
            Thread.sleep(waitingTime);
        }
        catch (final InterruptedException ie)
        {
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void pause(final String waitingTime)
    {
        pause(Long.parseLong(waitingTime));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeSelection(final String selectLocator, final String optionLocator)
    {
        checkElementLocator(selectLocator);

        final WebElement select = finder.findElement(webDriver, selectLocator);
        checkIsEqual("Element '" + selectLocator + "' is not a HTML select element", "select", select.getTagName());
        checkIsTrue("Select '" + selectLocator + "' does not support multiple selection", isMultipleSelect(select));
        checkIsTrue("Select '" + selectLocator + "' is disabled", select.isEnabled());

        final List<WebElement> options = finder.findOptions(select, optionLocator);
        for (final WebElement option : options)
        {
            setUnselected(select, option);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void select(final String selectLocator, final String optionLocator)
    {
        checkElementLocator(selectLocator);

        WebDriverUtils.assumeOkOnAlertOrConfirm(webDriver);

        final WebElement select = finder.findElement(webDriver, selectLocator);
        checkIsEqual("Element '" + selectLocator + "' is not a HTML select element", "select", select.getTagName());
        checkIsTrue("Select '" + selectLocator + "' is disabled", select.isEnabled());

        if (isMultipleSelect(select))
        {
            for (final WebElement o : select.findElements(By.tagName("option")))
            {
                setUnselected(select, o);
            }

            final List<WebElement> options = finder.findOptions(select, optionLocator);
            for (final WebElement option : options)
            {
                setSelected(select, option);
            }
        }
        else
        {
            final WebElement option = finder.findOption(select, optionLocator);
            if (WebDriverUtils.isClickable(webDriver, option))
            {
                if (!option.isSelected())
                {
                    option.click();
                }
            }
            else if (!option.isSelected() && option.isEnabled())
            {
                WebDriverUtils.setAttribute(webDriver, option, "selected", "false");
                WebDriverUtils.executeJavaScriptIfPossible(webDriver, "arguments[0].selectedIndex = arguments[1].index;", select, option);
                WebDriverUtils.fireChangeEvent(webDriver, select);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void selectAndWait(final String selectLocator, final String optionLocator)
    {
        executeCommandAndWait(() -> select(selectLocator, optionLocator));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void selectFrame(final String frameLocator)
    {
        checkIsTrue("Frame locator is empty", StringUtils.isNotBlank(frameLocator));

        if (frameLocator.startsWith("index="))
        {
            checkIsTrue("Invalid frame locator: " + frameLocator, FRAME_INDEX_PATTERN.matcher(frameLocator).matches());

            final String frameID = StringUtils.substring(frameLocator, 6);
            webDriver.switchTo().frame(Integer.parseInt(frameID));
        }
        else if (FRAME_NAME_LOCATOR_PATTERN.matcher(frameLocator).matches())
        {
            final String frameID = StringUtils.substring(frameLocator, 4);

            final Matcher m = FRAME_NAME_PATTERN.matcher(frameID);
            while (m.find())
            {
                webDriver.switchTo().frame(m.group(2));
            }
        }
        else if (frameLocator.equals("relative=top"))
        {
            webDriver.switchTo().defaultContent();
        }
        else if (frameLocator.equals("relative=parent"))
        {
            // check if we are already on top
            final JavascriptExecutor exec = (JavascriptExecutor) webDriver;
            final Boolean isParent = (Boolean) exec.executeScript("return (window.parent == window.top);");
            if (isParent != null && isParent)
            {
                webDriver.switchTo().defaultContent();
            }
            else
            {
                // get frame locator path
                // to select the frame by name/ID
                if (!switchToParentByNameOrId())
                {
                    // if that fails locate it by frame index number
                    switchToParentByIndex();
                }
            }
        }
        else
        {
            final WebElement frame = finder.findElement(webDriver, frameLocator);
            webDriver.switchTo().frame(frame);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void selectWindow()
    {
        selectWindow(null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void selectWindow(final String windowLocator)
    {
        if (StringUtils.isBlank(windowLocator) || windowLocator.equals("null"))
        {
            webDriver.switchTo().window(originalWindowHandle);
        }
        else
        {
            final String windowhandle = finder.findWindow(webDriver, windowLocator, false);
            webDriver.switchTo().window(windowhandle);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setTimeout(final long timeout)
    {
        // set the general waitFor.../...AndWait timeout
        super.setTimeout(timeout);

        // for Web drivers, use the timeout also as page-load/script timeout
        if (pageLoadTimeoutAtDriverEnabled)
        {
            webDriver.manage().timeouts().pageLoadTimeout(Duration.ofMillis(timeout));
            webDriver.manage().timeouts().scriptTimeout(Duration.ofMillis(timeout));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void submit(final String formLocator)
    {
        checkElementLocator(formLocator);

        final WebElement form = finder.findElement(webDriver, formLocator);
        checkIsEqual("Element '" + form + "' is not a HTML form element", "form", form.getTagName());
        form.submit();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void submitAndWait(final String formLocator)
    {
        executeCommandAndWait(() -> submit(formLocator));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void type(final String elementLocator, final String text)
    {
        checkElementLocator(elementLocator);
        checkIsTrue("Text is null", text != null);

        WebDriverUtils.assumeOkOnAlertOrConfirm(webDriver);

        final WebElement element = finder.findElement(webDriver, elementLocator);
        final boolean isDisplayed = element.isDisplayed();

        // first of all clear the element
        if (isDisplayed && WebDriverUtils.isEditable(webDriver, element, false))
        {
            element.clear();
        }

        if (!isDisplayed || !WebDriverUtils.isClickable(webDriver, element, false))
        {
            typeKeys(element, text);
        }
        else
        {
            if (text.length() > 0)
            {
                // type the text as is
                element.sendKeys(text);
            }
            else
            {
                // type some "null" text to trigger the events
                element.sendKeys(" \b");
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void typeAndWait(final String elementLocator, final String text)
    {
        executeCommandAndWait(() -> type(elementLocator, text + Keys.ENTER));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void uncheck(final String elementLocator)
    {
        checkElementLocator(elementLocator);

        WebDriverUtils.assumeOkOnAlertOrConfirm(webDriver);

        final WebElement input = finder.findElement(webDriver, elementLocator);

        checkIsEqual("Element '" + elementLocator + "' is not a HTML input element", "input", input.getTagName());
        final String typeAttribute = input.getAttribute("type");
        checkIsTrue("Only check boxes can be unchecked", typeAttribute.equals("checkbox"));
        checkIsTrue("Checkbox '" + elementLocator + "' is disabled", input.isEnabled());

        if (input.isSelected())
        {
            if (WebDriverUtils.isClickable(webDriver, input))
            {
                input.click();
            }
            else
            {
                WebDriverUtils.setAttribute(webDriver, input, "checked", "false");
                WebDriverUtils.fireChangeEvent(webDriver, input);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void uncheckAndWait(final String elementLocator)
    {
        executeCommandAndWait(() -> uncheck(elementLocator));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void waitForAttribute(final String attributeLocator, final String textPattern)
    {
        waitForCondition(attributeMatches(attributeLocator, textPattern, true));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void waitForAttribute(final String elementLocator, final String attributeName, final String textPattern)
    {
        waitForCondition(attributeMatches(elementLocator, attributeName, textPattern, true));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void waitForChecked(final String elementLocator)
    {
        waitForCondition(elementChecked(elementLocator, true));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void waitForClass(final String elementLocator, final String clazzString)
    {
        waitForCondition(classMatches(elementLocator, clazzString, true));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void waitForElementCount(final String elementLocator, final int count)
    {
        executeRunnable(count == 0, () -> waitForCondition(elementCountEqual(elementLocator, count, true)));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void waitForElementCount(final String elementLocator, final String count)
    {
        waitForElementCount(elementLocator, Integer.parseInt(count));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void waitForElementPresent(final String elementLocator)
    {
        waitForCondition(elementPresent(elementLocator, true));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void waitForEval(final String expression, final String textPattern)
    {
        waitForCondition(evalMatches(expression, textPattern, true));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void waitForNotAttribute(final String attributeLocator, final String textPattern)
    {
        waitForCondition(attributeMatches(attributeLocator, textPattern, false));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void waitForNotAttribute(final String elementLocator, final String attributeName, final String textPattern)
    {
        waitForCondition(attributeMatches(elementLocator, attributeName, textPattern, false));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void waitForNotChecked(final String elementLocator)
    {
        waitForCondition(elementChecked(elementLocator, false));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void waitForNotClass(final String elementLocator, final String clazzString)
    {
        waitForCondition(classMatches(elementLocator, clazzString, false));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void waitForNotElementCount(final String elementLocator, final int count)
    {
        executeRunnable(count != 0, () -> waitForCondition(elementCountEqual(elementLocator, count, false)));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void waitForNotElementCount(final String elementLocator, final String count)
    {
        waitForNotElementCount(elementLocator, Integer.parseInt(count));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void waitForNotElementPresent(final String elementLocator)
    {
        executeRunnable(true, () -> waitForCondition(elementPresent(elementLocator, false)));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void waitForNotEval(final String expression, final String textPattern)
    {
        waitForCondition(evalMatches(expression, textPattern, false));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void waitForNotText(final String elementLocator, final String textPattern)
    {
        waitForCondition(textMatches(elementLocator, textPattern, false));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void waitForNotTextPresent(final String textPattern)
    {
        waitForCondition(pageTextMatches(textPattern, false));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void waitForNotTitle(final String titlePattern)
    {
        waitForCondition(titleMatches(titlePattern, false));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void waitForNotXpathCount(final String xpath, final int count)
    {
        executeRunnable(count != 0, () -> waitForCondition(xpathCountEqual(xpath, count, false)));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void waitForNotXpathCount(final String xpath, final String count)
    {
        waitForNotXpathCount(xpath, Integer.parseInt(count));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void waitForPageToLoad()
    {
        if (!(webDriver instanceof JavascriptExecutor))
        {
            return;
        }

        final String loadListenerScript = "return (function(scope) {\n" +
                                          "  function XltLoadListener() {\n" +
                                          "    this._loadDetected = false;\n" + 
                                          "    this._initialized = false;\n" + 
                                          "    this.init();\n" +
                                          "  }\n" + 
                                          "  XltLoadListener.prototype = {\n" + 
                                          "    init: function() {\n" +
                                          "      if (this._initialized) return;\n" + 
                                          "      this._initialized = true;\n" +
                                          "      scope.addEventListener(\"load\", this._handleLoadEvent.bind(this));\n" +
                                          "    },\n" +
                                          "    _handleLoadEvent: function(event) {\n" +
                                          "      if (event.target.defaultView === event.target.defaultView.top) {\n" +
                                          "        this._loadDetected = true;\n" + 
                                          "      }\n" + 
                                          "    },\n" +
                                          "    get loadDetected() {\n" +
                                          "      return this._loadDetected;\n" + 
                                          "    }\n" + 
                                          "  };\n" +
                                          "  return (scope._xll = scope._xll || new XltLoadListener()).loadDetected;\n" +
                                          "})(window)";

        final String desc = "PAGE LOADED";
        final Condition condition = new Condition(desc)
        {
            /**
             * {@inheritDoc}
             */
            @Override
            protected boolean evaluate()
            {
                final boolean ready = (Boolean) WebDriverUtils.executeJavaScript(webDriver, loadListenerScript);
                setReason(ready ? "Page loaded" : "Page did not load");
                return ready;
            }
        };

        try
        {
            waitForCondition(condition);
        }
        catch (TimeoutException e)
        {
            // wrap exception as PageLoadTimeoutException as only those can be ignored
            throw new PageLoadTimeoutException(e.getMessage(), e);
        }
        finally
        {
            WebDriverUtils.executeJavaScriptIfPossible(webDriver, "delete window._xll");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void waitForPopUp()
    {
        waitForCondition(new Condition("POPUP LOADED")
        {
            @Override
            protected boolean evaluate()
            {
                final Set<String> handles = webDriver.getWindowHandles();
                final boolean found = (handles != null && handles.size() > 1);
                setReason((found ? "At least one" : "No") + " window found");
                return found;
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void waitForPopUp(final String windowID)
    {
        waitForPopUp(windowID, TestContext.getCurrent().getTimeout());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void waitForPopUp(final String windowID, final long maxWaitingTime)
    {
        // determine whether the current window is open/closed
        boolean isCurrentWindowOpen = false;
        try
        {
            webDriver.getWindowHandle();
            isCurrentWindowOpen = true;
        }
        catch (final NoSuchWindowException e)
        {
        }

        // decide whether we can safely return to the current window
        final boolean switchBackToCurrentWindow = isCurrentWindowOpen;

        waitForCondition(new Condition("WINDOW PRESENT")
        {
            @Override
            protected boolean evaluate()
            {
                try
                {
                    finder.findWindow(webDriver, windowID, switchBackToCurrentWindow);
                    setReason("At least one window found");
                    return true;
                }
                catch (final NoSuchWindowException e)
                {
                    setReason("No such window found");
                    return false;
                }
            }
        }, maxWaitingTime);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void waitForPopUp(final String windowID, final String maxWaitingTime)
    {
        waitForPopUp(windowID, Long.parseLong(maxWaitingTime));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void waitForText(final String elementLocator, final String textPattern)
    {
        waitForCondition(textMatches(elementLocator, textPattern, true));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void waitForTextPresent(final String textPattern)
    {
        waitForCondition(pageTextMatches(textPattern, true));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void waitForValue(final String elementLocator, final String valuePattern)
    {
        waitForCondition(valueMatches(elementLocator, valuePattern, true));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void waitForNotValue(final String elementLocator, final String valuePattern)
    {
        waitForCondition(valueMatches(elementLocator, valuePattern, false));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void waitForTitle(final String titlePattern)
    {
        waitForCondition(titleMatches(titlePattern, true));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void waitForXpathCount(final String xpath, final int count)
    {
        executeRunnable(count == 0, () -> waitForCondition(xpathCountEqual(xpath, count, true)));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void waitForXpathCount(final String xpath, final String count)
    {
        waitForXpathCount(xpath, Integer.parseInt(count));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String _getText(final String elementLocator)
    {

        final WebElement element = finder.findElement(webDriver, elementLocator, false);

        final String elementText;
        if (element.isDisplayed())
        {
            final String tagName = element.getTagName();
            if ("input".equals(tagName))
            {
                final String type = element.getAttribute("type");
                if (type == null || type.equals("radio") || type.equals("checkbox"))
                {
                    elementText = "";
                }
                else
                {
                    elementText = getElementValue(element);
                }
            }
            else if ("textarea".equals(tagName))
            {
                elementText = getElementValue(element);
            }
            else
            {
                elementText = element.getText();
            }
        }
        else
        {
            elementText = "";
        }

        return elementText;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String _getValue(final String elementLocator)
    {
        final WebElement element = finder.findElement(webDriver, elementLocator, false);
        return getElementValue(element);
    }

    private String getElementValue(final WebElement element)
    {
        final String elementValue;
        {
            final String value = element.getAttribute("value");
            elementValue = value == null ? "" : value;
        }

        return elementValue;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getPageText()
    {
        final WebElement body = webDriver.findElement(By.tagName("body"));
        final String pageText = body.getText();

        return pageText == null ? "" : pageText;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getTitle()
    {
        return webDriver.getTitle();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected int _getXpathCount(final String xpath)
    {
        return webDriver.findElements(By.xpath(xpath)).size();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean _isElementPresent(final String elementLocator)
    {
        return finder.isElementPresent(webDriver, elementLocator);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String _evaluate(final String expression)
    {
        return (String) WebDriverUtils.executeJavaScriptIfPossible(webDriver, EVAL_SCRIPT, expression);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean _isChecked(final String elementLocator)
    {
        final WebElement checkboxOrRadio = finder.findElement(webDriver, elementLocator);
        checkIsEqual(String.format("Element '%s' is not a HTML input element", elementLocator), "input", checkboxOrRadio.getTagName());

        final String inputType = checkboxOrRadio.getAttribute("type");
        checkIsTrue(String.format("Input '%s' is neither a checkbox nor a radio button", elementLocator),
                    "radio".equals(inputType) || "checkbox".equals(inputType));

        return checkboxOrRadio.isSelected();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean _isEnabled(String elementLocator)
    {
        return finder.findElement(webDriver, elementLocator).isEnabled();
    }

    /**
     * Checks whether the passed HTML select element supports multi-selection.
     * 
     * @param select
     *            the select element
     * @return whether or not multi-selection is supported
     */
    private boolean isMultipleSelect(final WebElement select)
    {
        final String value = select.getAttribute("multiple");

        return (value != null && !value.equals("false"));
    }

    /**
     * Un-selects the given element.
     * 
     * @param selectElement
     *            the select element
     * @param element
     *            the element to un-select
     */
    private void setUnselected(final WebElement selectElement, final WebElement element)
    {
        doSelect(selectElement, element, false);
    }

    /**
     * Selects the given element.
     * 
     * @param selectElement
     *            the select element
     * @param element
     *            the element to select
     */
    private void setSelected(final WebElement selectElement, final WebElement element)
    {
        doSelect(selectElement, element, true);
    }

    /**
     * Changes the selected state of the given element.
     * 
     * @param selectElement
     *            the select element
     * @param element
     *            the element whose selected state should be changed
     * @param selected
     *            whether or not the given element should be selected
     */
    private void doSelect(final WebElement selectElement, final WebElement element, final boolean selected)
    {
        if (element.isSelected() != selected && element.isEnabled())
        {
            if (WebDriverUtils.isClickable(webDriver, element))
            {
                if ((webDriver instanceof XltDriver || webDriver instanceof HtmlUnitDriver))
                {
                    // HtmlUnit now deselects sibling options if CTRL is not pressed
                    new Actions(webDriver).keyDown(Keys.CONTROL).click(element).keyUp(Keys.CONTROL).perform();
                }
                else
                {
                    element.click();
                }
            }
            else
            {
                WebDriverUtils.setAttribute(webDriver, element, "selected", Boolean.toString(selected));
                WebDriverUtils.fireChangeEvent(webDriver, selectElement);
            }
        }
    }

    /**
     * Type keys into the given element.
     * 
     * @param element
     *            the element to type into
     * @param text
     *            the text to type in
     */
    private void typeKeys(final WebElement element, final String text)
    {
        final String typeAttribute = element.getAttribute("type");
        final boolean adjustValue = element.getTagName().equalsIgnoreCase("input") &&
                                    (typeAttribute.equalsIgnoreCase("hidden") || typeAttribute.equalsIgnoreCase("text"));

        final StringBuilder sb = new StringBuilder(text.length());
        for (final char c : text.toCharArray())
        {
            sb.append(c);

            if (adjustValue)
            {
                WebDriverUtils.setAttribute(webDriver, element, "value", "'" + sb.toString() + "'");
            }

            WebDriverUtils.fireKeyDownEvent(webDriver, element, c);
            WebDriverUtils.fireKeyPressEvent(webDriver, element, c);
            WebDriverUtils.fireKeyUpEvent(webDriver, element, c);

        }
    }

    /**
     * Switch to parent frame.
     * 
     * @return <code>true</code> if a switch was performed, <code>false</code> otherwise
     */
    private boolean switchToParentByNameOrId()
    {
        final String javaScript = "var parentWindow = window.parent; var winStack = []; while(parentWindow != parentWindow.top) {winStack.unshift(parentWindow.id||parentWindow.name||parentWindow.title); parentWindow = parentWindow.parent;} return winStack.join(',');";
        return switchToParent(javaScript, false);
    }

    /**
     * Switch to parent frame.
     * 
     * @return <code>true</code> if a switch was performed, <code>false</code> otherwise
     */
    private boolean switchToParentByIndex()
    {
        final String javaScript = "var frameWindow = window.parent; var parentWindow = frameWindow.parent;  var indexStack = [];  while(frameWindow != parentWindow) { var frameSize = parentWindow.frames.length; for(var i=0; i<frameSize; i++) { if(parentWindow.frames[i] == frameWindow) { indexStack.unshift(i); break; } } frameWindow = parentWindow; parentWindow = frameWindow.parent; } return indexStack.join(',');";
        return switchToParent(javaScript, true);
    }

    /**
     * Switch to parent frame.
     * 
     * @param javaScript
     *            javascript code to get the names/IDs/indexes
     * @param isById
     *            switch by frame index number?
     * @return <code>true</code> if a switch was performed, <code>false</code> otherwise
     */
    private boolean switchToParent(final String javaScript, final boolean isById)
    {
        final JavascriptExecutor exec = (JavascriptExecutor) webDriver;
        final String frameLocatorsRAW = (String) exec.executeScript(javaScript);
        if (StringUtils.isNotBlank(frameLocatorsRAW) && frameLocatorsRAW.length() > 1)
        {
            final String[] frameLocators = frameLocatorsRAW.split(",");

            // go to target frame beginning at the top window
            webDriver.switchTo().defaultContent();
            for (final String frameLocator : frameLocators)
            {
                if (isById)
                {
                    webDriver.switchTo().frame(Integer.valueOf(frameLocator));
                }
                else
                {
                    webDriver.switchTo().frame(frameLocator);
                }
            }

            return true;
        }

        return false;
    }

    /**
     * Waits until the ID of at least one selected option of the given select element matches the given pattern.
     * 
     * @param selectLocator
     *            the select element locator
     * @param idPattern
     *            the ID pattern that must match
     */
    public void waitForSelectedId(final String selectLocator, final String idPattern)
    {
        waitForCondition(idSelected(selectLocator, idPattern, true));
    }

    /**
     * Waits until the IDs of all selected options of the given select element do not match the given pattern.
     * 
     * @param selectLocator
     *            the select element locator
     * @param idPattern
     *            the ID pattern that must not match
     */
    public void waitForNotSelectedId(final String selectLocator, final String idPattern)
    {
        waitForCondition(idSelected(selectLocator, idPattern, false));
    }

    /**
     * Waits until the option of the given select element at the given index is selected.
     * 
     * @param selectLocator
     *            the select element locator
     * @param indexPattern
     *            the option index pattern
     */
    public void waitForSelectedIndex(final String selectLocator, final String indexPattern)
    {
        waitForCondition(indexSelected(selectLocator, indexPattern, true));
    }

    /**
     * Waits until the option of the given select element at the given index is not selected.
     * 
     * @param selectLocator
     *            the select element locator
     * @param indexPattern
     *            the option index pattern
     */
    public void waitForNotSelectedIndex(final String selectLocator, final String indexPattern)
    {
        waitForCondition(indexSelected(selectLocator, indexPattern, false));
    }

    /**
     * Waits until the label of at least one selected option of the given select element matches the given pattern.
     * 
     * @param selectLocator
     *            the select element locator
     * @param labelPattern
     *            the label pattern that must match
     */
    public void waitForSelectedLabel(final String selectLocator, final String labelPattern)
    {
        waitForCondition(labelSelected(selectLocator, labelPattern, true));
    }

    /**
     * Waits until no label of all selected options of the given select element matches the given pattern.
     * 
     * @param selectLocator
     *            the select element locator
     * @param labelPattern
     *            the label pattern that must not match
     */
    public void waitForNotSelectedLabel(final String selectLocator, final String labelPattern)
    {
        waitForCondition(labelSelected(selectLocator, labelPattern, false));
    }

    /**
     * Waits until the value of at least one selected option of the given select element matches the given pattern.
     * 
     * @param selectLocator
     *            the select element locator
     * @param valuePattern
     *            the value pattern that must match
     */
    public void waitForSelectedValue(final String selectLocator, final String valuePattern)
    {
        waitForCondition(valueSelected(selectLocator, valuePattern, true));
    }

    /**
     * Waits until no value of all selected options of the given select element matches the given pattern.
     * 
     * @param selectLocator
     *            the select element locator
     * @param valuePattern
     *            the value pattern that must not match
     */
    public void waitForNotSelectedValue(final String selectLocator, final String valuePattern)
    {
        waitForCondition(valueSelected(selectLocator, valuePattern, false));
    }

    /**
     * Waits until the given element becomes visible.
     * 
     * @param elementLocator
     *            the element locator
     */
    public void waitForVisible(final String elementLocator)
    {
        waitForCondition(elementVisible(elementLocator, true));
    }

    /**
     * Waits until the given element becomes invisible.
     * 
     * @param elementLocator
     *            the element locator
     */
    public void waitForNotVisible(final String elementLocator)
    {
        waitForCondition(elementVisible(elementLocator, false));
    }

    /**
     * Waits until the effective style of the element identified by the given element locator matches the given style.
     * 
     * @param elementLocator
     *            the element locator
     * @param styleText
     *            the style that must match (e.g. <code>width: 10px; overflow: hidden;</code>)
     */
    public void waitForStyle(final String elementLocator, final String styleText)
    {
        waitForCondition(styleMatches(elementLocator, styleText, true));
    }

    /**
     * Waits until the effective style of the element identified by the given element locator does NOT match the given
     * style.
     * 
     * @param elementLocator
     *            the element locator
     * @param styleText
     *            the style that must NOT match (e.g. <code>width: 10px; overflow: hidden;</code>)
     */
    public void waitForNotStyle(final String elementLocator, final String styleText)
    {
        waitForCondition(styleMatches(elementLocator, styleText, false));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected List<String> getSelectedIds(final String elementLocator)
    {
        final WebElement selectElement = finder.findElement(webDriver, elementLocator);
        checkIsEqual("Element '" + elementLocator + "' is not a HTML select element", "select", selectElement.getTagName());
        final List<WebElement> options = selectElement.findElements(By.xpath("./option"));
        checkIsTrue(String.format("Select '%s' does not contain any option", elementLocator), !options.isEmpty());

        final ArrayList<String> ids = new ArrayList<String>();
        for (final WebElement option : selectElement.findElements(By.xpath("./option")))
        {
            if (option.isSelected())
            {
                ids.add(option.getAttribute("id"));
            }
        }

        if (ids.isEmpty())
        {
            ids.add(options.get(0).getAttribute("id"));
        }

        return ids;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected List<Integer> getSelectedIndices(final String elementLocator)
    {
        final WebElement selectElement = finder.findElement(webDriver, elementLocator);
        checkIsEqual("Element '" + elementLocator + "' is not a HTML select element", "select", selectElement.getTagName());
        final List<WebElement> options = selectElement.findElements(By.xpath("./option"));
        checkIsTrue(String.format("Select '%s' does not contain any option", elementLocator), !options.isEmpty());

        final List<Integer> indices = new ArrayList<Integer>();
        for (int i = 0; i < options.size(); i++)
        {
            if (options.get(i).isSelected())
            {
                indices.add(Integer.valueOf(i));
            }
        }

        if (indices.isEmpty())
        {
            indices.add(Integer.valueOf(0));
        }

        return indices;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected List<String> getSelectedLabels(final String elementLocator)
    {
        final WebElement selectElement = finder.findElement(webDriver, elementLocator);
        checkIsEqual("Element '" + elementLocator + "' is not a HTML select element", "select", selectElement.getTagName());
        final List<WebElement> options = selectElement.findElements(By.xpath("./option"));
        checkIsTrue(String.format("Select '%s' does not contain any option", elementLocator), !options.isEmpty());

        final List<String> labels = new ArrayList<String>();
        for (final WebElement option : options)
        {
            if (option.isSelected())
            {
                labels.add(option.isDisplayed() ? option.getText() : "");
            }
        }

        if (labels.isEmpty())
        {
            final WebElement firstOption = options.get(0);
            labels.add(firstOption.isDisplayed() ? firstOption.getText() : "");
        }

        return labels;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected List<String> getSelectedValues(final String elementLocator)
    {
        final WebElement selectElement = finder.findElement(webDriver, elementLocator);
        checkIsEqual("Element '" + elementLocator + "' is not a HTML select element", "select", selectElement.getTagName());
        final List<WebElement> options = selectElement.findElements(By.xpath("./option"));
        checkIsTrue(String.format("Select '%s' does not contain any option", elementLocator), !options.isEmpty());

        final List<String> values = new ArrayList<String>();
        for (final WebElement option : options)
        {
            if (option.isSelected())
            {
                values.add(option.getAttribute("value"));
            }
        }

        if (values.isEmpty())
        {
            values.add(options.get(0).getAttribute("value"));
        }

        return values;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean _isVisible(final String elementLocator)
    {
        return finder.findElement(webDriver, elementLocator).isDisplayed();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String _getAttribute(final String elementLocator, final String attributeName)
    {
        final WebElement element = finder.findElement(webDriver, elementLocator);

        return element.getAttribute(attributeName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String _getEffectiveStyle(final String elementLocator, final String propertyName)
    {
        checkIsTrue("CSS property name is blank", StringUtils.isNotBlank(propertyName));

        final WebElement element = finder.findElement(webDriver, elementLocator);
        return WebDriverUtils.getEffectiveStyle(webDriver, element, propertyName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected int _getElementCount(final String elementLocator)
    {
        return finder.findElements(webDriver, elementLocator).size();
    }

    /**
     * {@inheritDoc}
     */
    protected long disableImplicitWaitTimeout()
    {
        final long timeout = super.disableImplicitWaitTimeout();

        webDriver.manage().timeouts().implicitlyWait(Duration.ofMillis(0));

        return timeout;
    }

    /**
     * {@inheritDoc}
     */
    protected void enableImplicitWaitTimeout(final long timeout)
    {
        super.enableImplicitWaitTimeout(timeout);

        webDriver.manage().timeouts().implicitlyWait(Duration.ofMillis(timeout));
    }

    /**
     * Checks if the given driver is configured to wait for the page load to complete when navigating to a new page.
     * 
     * @param webDriver
     *            the web driver
     * @return <code>true</code> if the driver will wait, <code>false</code> otherwise
     */
    private static boolean isDriverWaitingForPageLoad(final WebDriver webDriver)
    {
        if (webDriver instanceof HasCapabilities)
        {
            final Capabilities caps = ((HasCapabilities) webDriver).getCapabilities();
            if (caps != null)
            {
                final Object pageLoadStrategy = caps.getCapability(CapabilityType.PAGE_LOAD_STRATEGY);
                if (pageLoadStrategy != null)
                {
                    if ("none".equalsIgnoreCase(pageLoadStrategy.toString()) || "eager".equalsIgnoreCase(pageLoadStrategy.toString()))
                    {
                        return false;
                    }
                }
            }
        }

        // "normal" is the default strategy
        return true;
    }

    /**
     * Executes the passed command and waits for the page load to complete. The method will not wait endlessly, but up
     * to the script timeout only.
     * <p>
     * Waiting for the page load to complete is implemented to support different strategies. If the driver is configured
     * to wait for page loads, the method will simply wait for the driver to return in time. If the driver will not wait
     * for page loads, the method will wait itself for the page load.
     * 
     * @param command
     *            the command to execute
     * @throws PageLoadTimeoutException
     *             if the script timeout was reached before the page load completes
     */
    private void executeCommandAndWait(final Runnable command)
    { 
        if (driverWaitsForPageLoad)
        {
            TestContext.getCurrent().callAndWait(() -> {
                command.run();
                return null;
            });
        }
        else
        {
            command.run();

            // wait for the load event to arrive
            waitForPageToLoad();
        }
    }
}
