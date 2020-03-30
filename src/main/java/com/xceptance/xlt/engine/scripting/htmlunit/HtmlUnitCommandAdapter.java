/*
 * Copyright (c) 2005-2020 Xceptance Software Technologies GmbH
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
package com.xceptance.xlt.engine.scripting.htmlunit;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.regex.Matcher;

import org.apache.commons.lang3.StringUtils;

import com.gargoylesoftware.htmlunit.CookieManager;
import com.gargoylesoftware.htmlunit.DialogWindow;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.TopLevelWindow;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebWindow;
import com.gargoylesoftware.htmlunit.WebWindowEvent;
import com.gargoylesoftware.htmlunit.WebWindowListener;
import com.gargoylesoftware.htmlunit.html.BaseFrameElement;
import com.gargoylesoftware.htmlunit.html.DomNode;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlFileInput;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlHiddenInput;
import com.gargoylesoftware.htmlunit.html.HtmlInput;
import com.gargoylesoftware.htmlunit.html.HtmlOption;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSelect;
import com.gargoylesoftware.htmlunit.html.HtmlTextArea;
import com.gargoylesoftware.htmlunit.html.SubmittableElement;
import com.gargoylesoftware.htmlunit.html.impl.SelectableTextInput;
import com.gargoylesoftware.htmlunit.javascript.host.Element;
import com.gargoylesoftware.htmlunit.javascript.host.Window;
import com.gargoylesoftware.htmlunit.javascript.host.css.ComputedCSSStyleDeclaration;
import com.gargoylesoftware.htmlunit.javascript.host.event.MouseEvent;
import com.gargoylesoftware.htmlunit.util.Cookie;
import com.gargoylesoftware.htmlunit.util.UrlUtils;
import com.xceptance.xlt.api.util.HtmlPageUtils;
import com.xceptance.xlt.api.util.XltException;
import com.xceptance.xlt.engine.scripting.CookieConstants;
import com.xceptance.xlt.engine.scripting.TestContext;
import com.xceptance.xlt.engine.scripting.util.AbstractCommandAdapter;
import com.xceptance.xlt.engine.scripting.util.CommandsInvocationHandler;
import com.xceptance.xlt.engine.scripting.util.Condition;
import com.xceptance.xlt.engine.scripting.util.ReplayUtils;

import net.sourceforge.htmlunit.corejs.javascript.Function;
import net.sourceforge.htmlunit.corejs.javascript.ScriptableObject;

/**
 * Command adapter for HtmlUnit.
 * 
 * @author Hartmut Arlt (Xceptance Software Technologies GmbH)
 */
public final class HtmlUnitCommandAdapter extends AbstractCommandAdapter implements HtmlUnitScriptCommands
{
    private static final Integer ZERO = Integer.valueOf(0);

    /**
     * Flag which indicates if this adapter has been initialized.
     */
    private boolean initialized = false;

    /**
     * Finder used to locate HtmlUnit elements.
     */
    private final HtmlUnitFinder finder = new HtmlUnitFinder();

    /**
     * {@inheritDoc}
     */
    @Override
    public HtmlPage addSelection(final String selectLocator, final String optionLocator)
    {
        checkElementLocator(selectLocator);

        final HtmlPage page = getCurrentPage();
        final HtmlElement element = finder.findElement(page, selectLocator);

        checkIsTrue("Element '" + selectLocator + "' is not a HTML select element", element instanceof HtmlSelect);
        final HtmlSelect selectElement = (HtmlSelect) element;
        checkIsTrue("Select '" + selectLocator + "' does not support multiple selection", selectElement.isMultipleSelectEnabled());
        checkIsTrue("Select '" + selectLocator + "' is disabled", !selectElement.isDisabled());

        final List<HtmlOption> options = finder.findOptions(selectElement, optionLocator);

        Page p = selectElement.getPage();
        for (final HtmlOption o : options)
        {
            if (!o.isDisabled())
            {
                p = o.setSelected(true);
            }
        }

        return (HtmlPage) p;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HtmlPage check(final String elementLocator) throws IOException
    {
        checkElementLocator(elementLocator);

        final HtmlElement element = finder.findElement(getCurrentPage(), elementLocator);
        checkIsTrue("Element '" + elementLocator + "' is not a HTML input element", element instanceof HtmlInput);

        final HtmlInput input = (HtmlInput) element;
        final String typeAttribute = input.getTypeAttribute();
        checkIsTrue("Check is only allowed on radio/checkbox input elements",
                    typeAttribute.equals("radio") || typeAttribute.equals("checkbox"));
        checkIsTrue("Radio/checkbox '" + elementLocator + "' is disabled", !input.isDisabled());

        if (!input.isChecked())
        {
            return (HtmlPage) input.click();
        }
        return (HtmlPage) input.getPage();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HtmlPage checkAndWait(final String elementLocator) throws IOException
    {
        return performActionAndWaitForPageLoad(new Callable<HtmlPage>()
        {
            @Override
            public HtmlPage call() throws Exception
            {
                return check(elementLocator);
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HtmlPage click(final String elementLocator) throws IOException
    {
        checkElementLocator(elementLocator);

        final HtmlElement e = finder.findElement(getCurrentPage(), elementLocator);
        return (HtmlPage) e.click();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HtmlPage clickAndWait(final String elementLocator) throws IOException
    {
        return performActionAndWaitForPageLoad(new Callable<HtmlPage>()
        {
            @Override
            public HtmlPage call() throws Exception
            {
                return click(elementLocator);
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close()
    {
        final WebWindow w = getCurrentWindow();
        if (w != null && w instanceof TopLevelWindow)
        {
            ((TopLevelWindow) w).close();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HtmlPage contextMenu(final String elementLocator)
    {
        checkElementLocator(elementLocator);

        final HtmlElement element = finder.findElement(getCurrentPage(), elementLocator);
        checkIsTrue("Cannot interact with invisible elements", HtmlUnitElementUtils.isVisible(element));

        return (HtmlPage) element.rightClick();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HtmlPage contextMenuAt(final String elementLocator, final String coordinates)
    {
        final int coords[] = ReplayUtils.parseCoordinates(coordinates);
        checkIsTrue("Invalid coordinates: " + coordinates, coords != null);

        return contextMenuAt(elementLocator, coords[0], coords[1]);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HtmlPage contextMenuAt(final String elementLocator, final int coordX, final int coordY)
    {
        checkElementLocator(elementLocator);

        HtmlPage page = getCurrentPage();

        final HtmlElement element = finder.findElement(page, elementLocator);
        checkIsTrue("Cannot interact with invisible elements", HtmlUnitElementUtils.isVisible(element));

        page = HtmlUnitElementUtils.fireMouseEvent(element, "mousedown", coordX, coordY, MouseEvent.BUTTON_RIGHT);
        page = HtmlUnitElementUtils.fireMouseEvent(element, "mouseup", coordX, coordY, MouseEvent.BUTTON_RIGHT);
        page = HtmlUnitElementUtils.fireMouseEvent(element, "contextmenu", coordX, coordY, MouseEvent.BUTTON_RIGHT);

        return page;
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

        final String domain = getCurrentPage().getWebResponse().getWebRequest().getUrl().getHost();

        final WebClient webClient = getWebClient();
        final Cookie qookie = new Cookie(domain, cookieName, cookieValue, path, maxAge, false);
        webClient.getCookieManager().addCookie(qookie);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteAllVisibleCookies()
    {
        final WebClient webClient = getWebClient();
        final CookieManager cookieMgr = webClient.getCookieManager();
        for (final Cookie c : webClient.getCookies(getCurrentUrl()))
        {
            cookieMgr.removeCookie(c);
        }
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

        final WebClient webClient = getWebClient();
        final CookieManager cookieMgr = webClient.getCookieManager();
        for (final Cookie cookie : webClient.getCookies(getCurrentUrl()))
        {
            if (name.equals(cookie.getName()))
            {
                cookieMgr.removeCookie(cookie);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HtmlPage doubleClick(final String elementLocator) throws IOException
    {
        checkElementLocator(elementLocator);

        final HtmlElement element = finder.findElement(getCurrentPage(), elementLocator);
        return (HtmlPage) element.dblClick();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HtmlPage doubleClickAndWait(final String elementLocator) throws IOException
    {
        return performActionAndWaitForPageLoad(new Callable<HtmlPage>()
        {
            @Override
            public HtmlPage call() throws Exception
            {
                return doubleClick(elementLocator);
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HtmlPage mouseDown(final String elementLocator)
    {
        checkElementLocator(elementLocator);

        final HtmlElement element = finder.findElement(getCurrentPage(), elementLocator);
        checkIsTrue("Cannot interact with invisible elements", HtmlUnitElementUtils.isVisible(element));

        return (HtmlPage) element.mouseDown();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HtmlPage mouseDownAt(final String elementLocator, final String coordinates)
    {
        final int[] coords = ReplayUtils.parseCoordinates(coordinates);
        checkIsTrue("Invalid coordinates: " + coordinates, coords != null);

        return mouseDownAt(elementLocator, coords[0], coords[1]);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HtmlPage mouseDownAt(final String elementLocator, final int coordX, final int coordY)
    {
        checkElementLocator(elementLocator);

        final HtmlElement element = finder.findElement(getCurrentPage(), elementLocator);
        checkIsTrue("Cannot interact with invisible elements", HtmlUnitElementUtils.isVisible(element));

        return HtmlUnitElementUtils.fireMouseEvent(element, MouseEvent.TYPE_MOUSE_DOWN, coordX, coordY);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HtmlPage mouseMove(final String elementLocator)
    {
        checkElementLocator(elementLocator);

        final HtmlElement element = finder.findElement(getCurrentPage(), elementLocator);
        checkIsTrue("Cannot interact with invisible elements", HtmlUnitElementUtils.isVisible(element));

        return (HtmlPage) element.mouseMove();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HtmlPage mouseMoveAt(final String elementLocator, final String coordinates)
    {
        final int coords[] = ReplayUtils.parseCoordinates(coordinates);
        checkIsTrue("Invalid coordinates: " + coordinates, coords != null);

        return mouseMoveAt(elementLocator, coords[0], coords[1]);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HtmlPage mouseMoveAt(final String elementLocator, final int coordX, final int coordY)
    {
        checkElementLocator(elementLocator);

        final HtmlElement element = finder.findElement(getCurrentPage(), elementLocator);
        checkIsTrue("Cannot interact with invisible elements", HtmlUnitElementUtils.isVisible(element));

        return HtmlUnitElementUtils.fireMouseEvent(element, MouseEvent.TYPE_MOUSE_MOVE, coordX, coordY);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HtmlPage mouseOut(final String elementLocator)
    {
        checkElementLocator(elementLocator);

        final HtmlElement element = finder.findElement(getCurrentPage(), elementLocator);
        checkIsTrue("Cannot interact with invisible elements", HtmlUnitElementUtils.isVisible(element));

        return (HtmlPage) element.mouseOut();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HtmlPage mouseOver(final String elementLocator)
    {
        checkElementLocator(elementLocator);

        final HtmlElement element = finder.findElement(getCurrentPage(), elementLocator);
        checkIsTrue("Cannot interact with invisible elements", HtmlUnitElementUtils.isVisible(element));

        return (HtmlPage) element.mouseOver();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HtmlPage mouseUp(final String elementLocator)
    {
        checkElementLocator(elementLocator);

        final HtmlElement element = finder.findElement(getCurrentPage(), elementLocator);
        checkIsTrue("Cannot interact with invisible elements", HtmlUnitElementUtils.isVisible(element));

        return (HtmlPage) element.mouseUp();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HtmlPage mouseUpAt(final String elementLocator, final String coordinates)
    {
        final int coords[] = ReplayUtils.parseCoordinates(coordinates);
        checkIsTrue("Invalid coordinates: " + coordinates, coords != null);

        return mouseUpAt(elementLocator, coords[0], coords[1]);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HtmlPage mouseUpAt(final String elementLocator, final int coordX, final int coordY)
    {
        checkElementLocator(elementLocator);

        final HtmlElement element = finder.findElement(getCurrentPage(), elementLocator);
        checkIsTrue("Cannot interact with invisible elements", HtmlUnitElementUtils.isVisible(element));

        return HtmlUnitElementUtils.fireMouseEvent(element, MouseEvent.TYPE_MOUSE_UP, coordX, coordY);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HtmlPage open(final String url) throws Exception
    {
        initIfNecessary();

        return performActionAndWaitForPageLoad(new Callable<HtmlPage>()
        {
            @Override
            public HtmlPage call() throws Exception
            {
                final String baseUrl = TestContext.getCurrent().getBaseUrl();
                final String urlString = baseUrl != null ? UrlUtils.resolveUrl(baseUrl, url) : url;
                final URL rewrittenUrl = rewriteUrl(urlString);
                getWebClient().getPage(rewrittenUrl);

                return getCurrentPage();
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HtmlPage open(final URL url) throws Exception
    {
        return open(url.toString());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HtmlPage pause(final long waitingTime)
    {
        final WebWindow w = getCurrentPage().getEnclosingWindow();
        try
        {
            Thread.sleep(waitingTime);
        }
        catch (final InterruptedException ie)
        {
        }

        return (HtmlPage) w.getEnclosedPage();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HtmlPage pause(final String waitingTime)
    {
        return pause(Long.parseLong(waitingTime));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HtmlPage removeSelection(final String selectLocator, final String optionLocator)
    {
        checkElementLocator(selectLocator);

        final HtmlElement element = finder.findElement(getCurrentPage(), selectLocator);

        checkIsTrue("Element '" + selectLocator + "' is not a HTML select element", element instanceof HtmlSelect);
        final HtmlSelect selectElement = (HtmlSelect) element;
        checkIsTrue("Select '" + selectLocator + "' does not support multiple selection", selectElement.isMultipleSelectEnabled());
        checkIsTrue("Select '" + selectLocator + "' is disabled", !selectElement.isDisabled());

        final List<HtmlOption> options = finder.findOptions(selectElement, optionLocator);

        Page p = selectElement.getPage();
        for (final HtmlOption o : options)
        {
            if (!o.isDisabled())
            {
                p = o.setSelected(false);
            }
        }

        return (HtmlPage) p;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HtmlPage select(final String selectLocator, final String optionLocator)
    {
        checkElementLocator(selectLocator);

        final HtmlElement element = finder.findElement(getCurrentPage(), selectLocator);

        checkIsTrue("Element '" + selectLocator + "' is not a HTML select element", element instanceof HtmlSelect);
        final HtmlSelect selectElement = (HtmlSelect) element;
        checkIsTrue("Select '" + selectLocator + "' is disabled", !selectElement.isDisabled());

        Page p = selectElement.getPage();
        if (selectElement.isMultipleSelectEnabled())
        {
            for (final HtmlOption o : selectElement.getSelectedOptions())
            {
                if (!o.isDisabled())
                {
                    o.setSelected(false);
                }
            }

            final List<HtmlOption> options = finder.findOptions(selectElement, optionLocator);
            for (final HtmlOption o : options)
            {
                if (!o.isDisabled())
                {
                    p = o.setSelected(true);
                }
            }
        }
        else
        {
            final HtmlOption option = finder.findOption(selectElement, optionLocator);
            if (!option.isDisabled())
            {
                option.setSelected(true);
            }
        }

        return (HtmlPage) p;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HtmlPage selectAndWait(final String selectLocator, final String optionLocator)
    {
        return performActionAndWaitForPageLoad(new Callable<HtmlPage>()
        {
            @Override
            public HtmlPage call() throws Exception
            {
                return select(selectLocator, optionLocator);
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HtmlPage selectFrame(final String frameLocator)
    {
        checkIsTrue("Frame locator is empty", StringUtils.isNotBlank(frameLocator));

        final WebClient webClient = getWebClient();
        if (frameLocator.startsWith("index="))
        {
            checkIsTrue("Invalid frame locator: " + frameLocator, FRAME_INDEX_PATTERN.matcher(frameLocator).matches());

            final String frameID = StringUtils.substring(frameLocator, 6);

            final WebWindow frameWindow = getFrameWindowByIndex(webClient, frameID);
            webClient.setCurrentWindow(frameWindow);
        }
        else if (FRAME_NAME_LOCATOR_PATTERN.matcher(frameLocator).matches())
        {
            final String frameID = StringUtils.substring(frameLocator, 4);

            final Matcher m = FRAME_NAME_PATTERN.matcher(frameID);
            while (m.find())
            {
                final WebWindow frameWindow = getFrameWindowByNameOrID(webClient, m.group(2));
                webClient.setCurrentWindow(frameWindow);
            }
        }
        else if (frameLocator.equals("relative=top"))
        {
            final List<TopLevelWindow> topLevelWindows = webClient.getTopLevelWindows();
            if (topLevelWindows.size() == 1)
            {
                webClient.setCurrentWindow(topLevelWindows.get(0));
            }
            else
            {
                webClient.setCurrentWindow(webClient.getCurrentWindow().getTopWindow());
            }
        }
        else if (frameLocator.equals("relative=parent"))
        {
            webClient.setCurrentWindow(webClient.getCurrentWindow().getParentWindow());
        }
        else
        {
            final WebWindow frameWindow = getFrameWindowByNameOrID(webClient, frameLocator);
            webClient.setCurrentWindow(frameWindow);
        }

        setCurrentWindow(webClient.getCurrentWindow());

        return getCurrentPage();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HtmlPage selectWindow()
    {
        return selectWindow(null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HtmlPage selectWindow(String windowLocator)
    {
        if (StringUtils.isBlank(windowLocator) || windowLocator.equals("null"))
        {
            windowLocator = "";
        }

        final WebClient webClient = getWebClient();
        final WebWindow window = finder.findWindow(webClient, windowLocator);
        webClient.setCurrentWindow(window);

        setCurrentWindow(window);
        return (HtmlPage) window.getEnclosedPage();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HtmlPage submit(final String formLocator) throws Exception
    {
        checkElementLocator(formLocator);

        final HtmlPage page = getCurrentPage();
        final HtmlElement element = finder.findElement(page, formLocator);

        checkIsTrue("Element '" + formLocator + "' is not a HTML form element", element instanceof HtmlForm);
        final HtmlForm formElement = (HtmlForm) element;

        final Method m = HtmlForm.class.getDeclaredMethod("submit", SubmittableElement.class);
        m.setAccessible(true);

        final Page p = (Page) m.invoke(formElement, (Object) null);
        final WebWindow w = p.getEnclosingWindow();
        page.getWebClient().getJavaScriptEngine().processPostponedActions();
        return (HtmlPage) w.getEnclosedPage();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HtmlPage submitAndWait(final String formLocator)
    {
        return performActionAndWaitForPageLoad(new Callable<HtmlPage>()
        {
            @Override
            public HtmlPage call() throws Exception
            {
                return submit(formLocator);
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HtmlPage type(final String elementLocator, final String text) throws IOException
    {
        checkElementLocator(elementLocator);
        checkIsTrue("Text is null", text != null);

        final HtmlElement element = finder.findElement(getCurrentPage(), elementLocator);

        if (element instanceof SelectableTextInput)
        {
            // select any existing text
            ((SelectableTextInput) element).select();
        }

        if (text.length() > 0)
        {
            // typing text will automatically erase any selected text
            element.type(text);
        }
        else
        {
            // no text to type
            if (element instanceof SelectableTextInput)
            {
                // erase any selected text by typing a space and immediately erasing it again
                element.type(" \b");
            }
        }

        if (element instanceof HtmlHiddenInput)
        {
            ((HtmlHiddenInput) element).setValueAttribute(text);
        }
        else if (element instanceof HtmlFileInput)
        {
            ((HtmlFileInput) element).setValueAttribute(text);
        }

        return getCurrentPage();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HtmlPage typeAndWait(final String elementLocator, final String text)
    {
        return performActionAndWaitForPageLoad(new Callable<HtmlPage>()
        {
            @Override
            public HtmlPage call() throws Exception
            {
                return type(elementLocator, text + "\n");
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HtmlPage uncheck(final String elementLocator) throws IOException
    {
        checkElementLocator(elementLocator);
        final HtmlElement element = finder.findElement(getCurrentPage(), elementLocator);

        checkIsTrue("Element '" + elementLocator + "' is not a HTML input element", element instanceof HtmlInput);
        final HtmlInput input = (HtmlInput) element;
        final String typeAttribute = input.getTypeAttribute();
        checkIsTrue("Only checkboxes/radio buttons can be unchecked", typeAttribute.equals("radio") || typeAttribute.equals("checkbox"));
        checkIsTrue("Checkbox '" + elementLocator + "' is disabled", !input.isDisabled());

        if (input.isChecked())
        {
            return (HtmlPage) input.click();
        }

        return (HtmlPage) input.getPage();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HtmlPage uncheckAndWait(final String elementLocator) throws IOException
    {
        return performActionAndWaitForPageLoad(new Callable<HtmlPage>()
        {
            @Override
            public HtmlPage call() throws Exception
            {
                return uncheck(elementLocator);
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HtmlPage waitForAttribute(final String attributeLocator, final String textPattern)
    {
        waitForCondition(attributeMatches(attributeLocator, textPattern, true));
        return getCurrentPage();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HtmlPage waitForAttribute(final String elementLocator, final String attributeName, final String textPattern)
    {
        waitForCondition(attributeMatches(elementLocator, attributeName, textPattern, true));
        return getCurrentPage();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HtmlPage waitForChecked(final String elementLocator)
    {
        waitForCondition(elementChecked(elementLocator, true));
        return getCurrentPage();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HtmlPage waitForClass(final String elementLocator, final String clazzString)
    {
        waitForCondition(classMatches(elementLocator, clazzString, true));
        return getCurrentPage();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HtmlPage waitForElementCount(final String elementLocator, final int count)
    {
        executeRunnable(count == 0, () -> waitForCondition(elementCountEqual(elementLocator, count, true)));
        return getCurrentPage();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HtmlPage waitForElementCount(final String elementLocator, final String count)
    {
        return waitForElementCount(elementLocator, Integer.parseInt(count));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HtmlPage waitForElementPresent(final String elementLocator)
    {
        waitForCondition(elementPresent(elementLocator, true));
        return getCurrentPage();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HtmlPage waitForEval(final String expression, final String textPattern)
    {
        waitForCondition(evalMatches(expression, textPattern, true));

        return getCurrentPage();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HtmlPage waitForNotAttribute(final String attributeLocator, final String textPattern)
    {
        waitForCondition(attributeMatches(attributeLocator, textPattern, false));
        return getCurrentPage();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HtmlPage waitForNotAttribute(final String elementLocator, final String attributeName, final String textPattern)
    {
        waitForCondition(attributeMatches(elementLocator, attributeName, textPattern, false));
        return getCurrentPage();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HtmlPage waitForNotChecked(final String elementLocator)
    {
        waitForCondition(elementChecked(elementLocator, false));
        return getCurrentPage();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HtmlPage waitForNotClass(final String elementLocator, final String clazzString)
    {
        waitForCondition(classMatches(elementLocator, clazzString, false));
        return getCurrentPage();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HtmlPage waitForNotElementCount(final String elementLocator, final int count)
    {
        executeRunnable(count != 0, () -> waitForCondition(elementCountEqual(elementLocator, count, false)));
        return getCurrentPage();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HtmlPage waitForNotElementCount(final String elementLocator, final String count)
    {
        return waitForNotElementCount(elementLocator, Integer.parseInt(count));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HtmlPage waitForNotElementPresent(final String elementLocator)
    {
        executeRunnable(true, () -> waitForCondition(elementPresent(elementLocator, false)));
        return getCurrentPage();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HtmlPage waitForNotEval(final String expression, final String textPattern)
    {
        waitForCondition(evalMatches(expression, textPattern, false));
        return getCurrentPage();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HtmlPage waitForNotStyle(final String elementLocator, final String styleText)
    {
        waitForCondition(styleMatches(elementLocator, styleText, false));
        return getCurrentPage();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HtmlPage waitForNotText(final String elementLocator, final String textPattern)
    {
        waitForCondition(textMatches(elementLocator, textPattern, false));
        return getCurrentPage();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HtmlPage waitForNotTextPresent(final String textPattern)
    {
        waitForCondition(pageTextMatches(textPattern, false));
        return getCurrentPage();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HtmlPage waitForNotTitle(final String titlePattern)
    {
        waitForCondition(titleMatches(titlePattern, false));
        return getCurrentPage();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HtmlPage waitForNotXpathCount(final String xpath, final int count)
    {
        executeRunnable(count != 0, () -> waitForCondition(xpathCountEqual(xpath, count, false)));
        return getCurrentPage();
    }

    /**
     * Waits for the number of elements matching the given XPath expression change to a different value than the given
     * one.
     * 
     * @param xpath
     *            the XPath expression
     * @param count
     *            the number of elements currently matching the given XPath expression
     */
    public HtmlPage waitForNotXpathCount(final String xpath, final String count)
    {
        return waitForNotXpathCount(xpath, Integer.parseInt(count));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HtmlPage waitForPageToLoad()
    {
        return performActionAndWaitForPageLoad(new Callable<HtmlPage>()
        {
            public HtmlPage call()
            {
                return getCurrentPage();
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HtmlPage waitForPopUp()
    {
        return waitForPopUp(null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HtmlPage waitForPopUp(final String windowID)
    {
        return waitForPopUp(windowID, TestContext.getCurrent().getTimeout());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HtmlPage waitForPopUp(final String windowID, final long maxWaitingTime)
    {
        final WebClient webClient = getWebClient();

        waitForCondition(new Condition("WINDOW PRESENT")
        {
            @Override
            protected boolean evaluate()
            {
                final boolean found = windowID == null ? webClient.getTopLevelWindows().size() > 1
                                                       : finder.findWindow(webClient, windowID) != null;
                setReason((found ? "At least one" : "No such") + " window found");

                return found;
            }
        }, maxWaitingTime);

        return getCurrentPage();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HtmlPage waitForPopUp(final String windowID, final String maxWaitingTime)
    {
        return waitForPopUp(windowID, Long.parseLong(maxWaitingTime));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HtmlPage waitForStyle(final String elementLocator, final String styleText)
    {
        waitForCondition(styleMatches(elementLocator, styleText, true));
        return getCurrentPage();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HtmlPage waitForText(final String elementLocator, final String textPattern)
    {
        waitForCondition(textMatches(elementLocator, textPattern, true));
        return getCurrentPage();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HtmlPage waitForTextPresent(final String textPattern)
    {
        waitForCondition(pageTextMatches(textPattern, true));
        return getCurrentPage();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HtmlPage waitForValue(final String elementLocator, final String valuePattern)
    {
        waitForCondition(valueMatches(elementLocator, valuePattern, true));
        return getCurrentPage();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HtmlPage waitForNotValue(final String elementLocator, final String valuePattern)
    {
        waitForCondition(valueMatches(elementLocator, valuePattern, false));
        return getCurrentPage();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HtmlPage waitForTitle(final String titlePattern)
    {
        waitForCondition(titleMatches(titlePattern, true));
        return getCurrentPage();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HtmlPage waitForXpathCount(final String xpath, final int count)
    {
        executeRunnable(count == 0, () -> waitForCondition(xpathCountEqual(xpath, count, true)));
        return getCurrentPage();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HtmlPage waitForXpathCount(final String xpath, final String count)
    {
        return waitForXpathCount(xpath, Integer.parseInt(count));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HtmlPage waitForSelectedId(final String selectLocator, final String idPattern)
    {
        waitForCondition(idSelected(selectLocator, idPattern, true));
        return getCurrentPage();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HtmlPage waitForNotSelectedId(final String selectLocator, final String idPattern)
    {
        waitForCondition(idSelected(selectLocator, idPattern, false));
        return getCurrentPage();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HtmlPage waitForSelectedIndex(final String selectLocator, final String indexPattern)
    {
        waitForCondition(indexSelected(selectLocator, indexPattern, true));
        return getCurrentPage();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HtmlPage waitForNotSelectedIndex(final String selectLocator, final String indexPattern)
    {
        waitForCondition(indexSelected(selectLocator, indexPattern, false));
        return getCurrentPage();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HtmlPage waitForSelectedLabel(final String selectLocator, final String labelPattern)
    {
        waitForCondition(labelSelected(selectLocator, labelPattern, true));
        return getCurrentPage();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HtmlPage waitForNotSelectedLabel(final String selectLocator, final String labelPattern)
    {
        waitForCondition(labelSelected(selectLocator, labelPattern, false));
        return getCurrentPage();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HtmlPage waitForSelectedValue(final String selectLocator, final String valuePattern)
    {
        waitForCondition(valueSelected(selectLocator, valuePattern, true));
        return getCurrentPage();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HtmlPage waitForNotSelectedValue(final String selectLocator, final String valuePattern)
    {
        waitForCondition(valueSelected(selectLocator, valuePattern, false));
        return getCurrentPage();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HtmlPage waitForVisible(final String elementLocator)
    {
        waitForCondition(elementVisible(elementLocator, true));
        return getCurrentPage();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HtmlPage waitForNotVisible(final String elementLocator)
    {
        waitForCondition(elementVisible(elementLocator, false));
        return getCurrentPage();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String _getText(final String elementLocator)
    {
        final String text;
        final HtmlElement e = finder.findElement(getCurrentPage(), elementLocator, false);
        if (e instanceof HtmlInput)
        {
            final String type = e.getAttribute("type");
            if (type.equals("radio") || type.equals("checkbox"))
            {
                text = "";
            }
            else
            {
                text = e.getAttribute("value");
            }
        }
        else
        {
            text = getElementText(e);
        }
        return text;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String _getValue(final String elementLocator)
    {
        final HtmlElement e = finder.findElement(getCurrentPage(), elementLocator, false);
        if (e instanceof HtmlTextArea || (e instanceof HtmlOption && !e.hasAttribute("value")))
        {
            return getElementText(e);
        }

        Object val = ((ScriptableObject) e.getScriptableObject()).get("value");
        if (val instanceof String)
        {
            return (String) val;
        }

        return e.getAttribute("value");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getPageText()
    {
        final HtmlPage page = getCurrentPage();
        return getElementText(page.getDocumentElement());
    }

    /**
     * @return
     */
    @Override
    public String getTitle()
    {
        return getCurrentPage().getTitleText().trim();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected int _getXpathCount(final String xpath)
    {
        return HtmlPageUtils.countElementsByXPath(getCurrentPage(), xpath);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean _isElementPresent(final String elementLocator)
    {
        return finder.isElementPresent(getCurrentPage(), elementLocator);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String _evaluate(final String expression)
    {
        if (!getWebClient().getOptions().isJavaScriptEnabled())
        {
            return null;
        }

        final String script = "(function(){ return function(){ var r = null; try { r = this.eval(arguments[0]) } catch (e){ } return String(r) }})()";
        final HtmlPage page = getCurrentPage();
        final Function jsFunction = (Function) page.executeJavaScript(script).getJavaScriptResult();
        // Notice: Casting to and from JS types is not necessary here since we can only handle strings.
        final Object[] args = new Object[]
            {
                expression
            };
        final Window w = (Window) page.getEnclosingWindow().getScriptableObject();
        return (String) page.executeJavaScriptFunction(jsFunction, w, args, null).getJavaScriptResult();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected List<String> getSelectedIds(final String elementLocator)
    {
        final HtmlElement selectElement = finder.findElement(getCurrentPage(), elementLocator);
        checkIsEqual(String.format("Element '%s' is not a HTML select element", elementLocator), "select", selectElement.getTagName());

        final HtmlSelect select = (HtmlSelect) selectElement;
        checkIsTrue(String.format("Select element '%s' does not contain any option", elementLocator), select.getOptionSize() > 0);

        final List<String> ids = new ArrayList<String>();
        for (final HtmlOption selectedOption : select.getSelectedOptions())
        {
            ids.add(selectedOption.getId());
        }

        if (ids.isEmpty())
        {
            ids.add(select.getOption(0).getId());
        }

        return ids;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected List<Integer> getSelectedIndices(final String elementLocator)
    {
        final HtmlElement selectElement = finder.findElement(getCurrentPage(), elementLocator);
        checkIsEqual(String.format("Element '%s' is not a HTML select element", elementLocator), "select", selectElement.getTagName());

        final HtmlSelect select = (HtmlSelect) selectElement;
        checkIsTrue(String.format("Select element '%s' does not contain any option", elementLocator), select.getOptionSize() > 0);

        final List<Integer> indices = new ArrayList<Integer>();
        final List<HtmlOption> options = select.getOptions();
        for (int i = 0; i < options.size(); i++)
        {
            if (options.get(i).isSelected())
            {
                indices.add(Integer.valueOf(i));
            }
        }

        if (indices.isEmpty())
        {
            indices.add(ZERO);
        }

        return indices;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected List<String> getSelectedLabels(final String elementLocator)
    {
        final HtmlElement selectElement = finder.findElement(getCurrentPage(), elementLocator);
        checkIsEqual(String.format("Element '%s' is not a HTML select element", elementLocator), "select", selectElement.getTagName());

        final HtmlSelect select = (HtmlSelect) selectElement;
        checkIsTrue(String.format("Select element '%s' does not contain any option", elementLocator), select.getOptionSize() > 0);

        final List<String> labels = new ArrayList<String>();
        for (final HtmlOption option : select.getSelectedOptions())
        {
            labels.add(HtmlUnitElementUtils.computeText(option));
        }

        if (labels.isEmpty())
        {
            labels.add(HtmlUnitElementUtils.computeText(select.getOption(0)));
        }

        return labels;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected List<String> getSelectedValues(final String elementLocator)
    {
        final HtmlElement selectElement = finder.findElement(getCurrentPage(), elementLocator);
        checkIsEqual(String.format("Element '%s' is not a HTML select element", elementLocator), "select", selectElement.getTagName());

        final HtmlSelect select = (HtmlSelect) selectElement;
        checkIsTrue(String.format("Select element '%s' does not contain any option", elementLocator), select.getOptionSize() > 0);

        final List<String> values = new ArrayList<String>();
        for (final HtmlOption option : select.getSelectedOptions())
        {
            values.add(option.getValueAttribute());
        }

        if (values.isEmpty())
        {
            values.add(select.getOption(0).getValueAttribute());
        }

        return values;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean _isChecked(final String elementLocator)
    {
        final HtmlElement checkboxOrRadio = finder.findElement(getCurrentPage(), elementLocator);
        checkIsEqual(String.format("Element '%s' is not a HTML input element", elementLocator), "input", checkboxOrRadio.getTagName());

        final HtmlInput inputElement = (HtmlInput) checkboxOrRadio;
        final String inputType = inputElement.getTypeAttribute();
        checkIsTrue(String.format("Input '%s' is neither a checkbox nor a radio button", elementLocator),
                    "radio".equals(inputType) || "checkbox".equals(inputType));

        return inputElement.isChecked();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean _isEnabled(String elementLocator)
    {
        return !finder.findElement(getCurrentPage(), elementLocator).hasAttribute("disabled");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean _isVisible(final String elementLocator)
    {
        return HtmlUnitElementUtils.isVisible(finder.findElement(getCurrentPage(), elementLocator));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String _getAttribute(final String elementLocator, final String attributeName)
    {
        final HtmlElement element = finder.findElement(getCurrentPage(), elementLocator);
        if (element.hasAttribute(attributeName))
        {
            return element.getAttribute(attributeName);
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String _getEffectiveStyle(final String elementLocator, final String propertyName)
    {
        checkElementLocator(elementLocator);
        checkIsTrue("CSS property name is blank", StringUtils.isNotBlank(propertyName));

        // get current page
        final HtmlPage page = getCurrentPage();
        // locate element
        final HtmlElement element = finder.findElement(page, elementLocator);
        // get actual style
        final ComputedCSSStyleDeclaration style = ((Window) page.getEnclosingWindow()
                                                                .getScriptableObject()).getComputedStyle((Element) element.getScriptableObject(),
                                                                                                         null);
        // get its value
        return StringUtils.defaultIfEmpty(style.getPropertyValue(propertyName), null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected int _getElementCount(final String elementLocator)
    {
        return finder.findElements(getCurrentPage(), elementLocator).size();
    }

    protected URL getCurrentUrl()
    {
        return getCurrentPage().getUrl();
    }

    private WeakReference<WebWindow> _currWindow;

    private synchronized void setCurrentWindow(final WebWindow w)
    {
        _currWindow = new WeakReference<WebWindow>(w);
    }

    private synchronized WebWindow getCurrentWindow()
    {
        return _currWindow.get();
    }

    /**
     * Returns the current page.
     * 
     * @return the current page
     */
    private HtmlPage getCurrentPage()
    {
        final WebWindow current = getCurrentWindow();
        if (current == null)
        {
            setCurrentWindow(getWebClient().getCurrentWindow());
        }

        final Page page = getCurrentWindow().getEnclosedPage();
        if (page instanceof HtmlPage)
        {
            return (HtmlPage) page;
        }
        else
        {
            if (page == null)
            {
                throw new XltException("There is no current page.");
            }
            else
            {
                throw new XltException("The current page is not an HTML page.");
            }
        }
    }

    /**
     * Returns the text of the passed element.
     * 
     * @return the element text
     */
    private String getElementText(final HtmlElement element)
    {
        return HtmlUnitElementUtils.computeText(element);
    }

    /**
     * Returns the frame window with the passed index.
     * 
     * @return the frame window
     */
    private WebWindow getFrameWindowByIndex(final WebClient webClient, final String index)
    {
        try
        {
            final int idx = Integer.parseInt(index);
            final HtmlPage page = getCurrentPage();

            return page.getFrames().get(idx);
        }
        catch (final Exception e)
        {
            throw new NoSuchWindowException("No frame window found with index: " + index, e);
        }
    }

    /**
     * Returns the frame window with the passed name or ID.
     * 
     * @return the frame window
     */
    private WebWindow getFrameWindowByNameOrID(final WebClient webClient, final String nameOrID)
    {
        final HtmlPage page = getCurrentPage();
        final HtmlElement e = finder.findElement(page, nameOrID);
        if (e instanceof BaseFrameElement)
        {
            return ((BaseFrameElement) e).getEnclosedWindow();
        }

        throw new NoSuchWindowException("No frame window found with name or ID: " + nameOrID);
    }

    /**
     * Returns the web client.
     * 
     * @return the web client
     */
    private WebClient getWebClient()
    {
        final WebClient webClient = TestContext.getCurrent().getWebClient();

        if (webClient == null)
        {
            throw new XltException("There is no current web client.");
        }

        return webClient;
    }

    /**
     * Calls the given action and waits for a page load.
     * 
     * @param action
     *            the action to call
     * @return resulting HtmlPage after page load
     */
    private HtmlPage performActionAndWaitForPageLoad(final Callable<HtmlPage> action)
    {
        final WebClient webClient = getWebClient();
        return TestContext.getCurrent().callAndWait(new WaitForPageLoadAction(action, webClient));
    }

    private synchronized void initIfNecessary()
    {
        if (!initialized)
        {
            final WebClient wc = getWebClient();
            setCurrentWindow(wc.getCurrentWindow());
            wc.addWebWindowListener(new WebWindowListener()
            {

                @Override
                public void webWindowOpened(final WebWindowEvent event)
                {
                }

                @Override
                public void webWindowContentChanged(final WebWindowEvent event)
                {
                }

                @Override
                public void webWindowClosed(final WebWindowEvent event)
                {
                    WebWindow curr = getCurrentWindow();
                    do
                    {
                        // Instance equality is okay in this case
                        if (curr == event.getWebWindow())
                        {
                            setCurrentWindow(getCurrentWindow().getTopWindow());
                            return;
                        }
                        curr = curr.getParentWindow();
                    }
                    while (curr != getCurrentWindow().getTopWindow());
                }
            });
            initialized = true;
        }
    }

    // ========================================================================
    // Helper classes (primarily used for page load detection)
    // ========================================================================

    private static class WindowContentChangedListener implements WebWindowListener
    {
        private boolean contentChanged;

        /**
         * {@inheritDoc}
         */
        @Override
        public void webWindowOpened(WebWindowEvent event)
        {
            // Empty

        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void webWindowContentChanged(WebWindowEvent event)
        {
            final WebWindow window = event.getWebWindow();
            // only top-level (and dialog) windows are of interest
            if (window instanceof TopLevelWindow || window instanceof DialogWindow)
            {
                final Page oldPage = event.getOldPage();
                final Page newPage = event.getNewPage();

                // about:blank is loaded first for all windows -> skip this dummy transition
                if ((oldPage == null || !oldPage.getUrl().toString().equals(WebClient.ABOUT_BLANK)) && newPage.isHtmlPage())
                {
                    // check for absence of 'refresh' response header
                    if (newPage.getWebResponse().getResponseHeaderValue("refresh") == null)
                    {
                        contentChanged = true;
                    }
                }
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void webWindowClosed(WebWindowEvent event)
        {
            // Empty
        }

    }

    private static class WaitForPageLoadAction implements Callable<HtmlPage>
    {

        private final Callable<HtmlPage> action;

        private final WebClient webClient;

        private WaitForPageLoadAction(final Callable<HtmlPage> loadAction, final WebClient webClient)
        {
            action = loadAction;
            this.webClient = webClient;
        }

        public HtmlPage call() throws Exception
        {
            final WindowContentChangedListener windowListener = new WindowContentChangedListener();
            try
            {
                webClient.addWebWindowListener(windowListener);

                final HtmlPage page = action.call();
                while (!windowListener.contentChanged)
                {
                    Thread.sleep(500L);
                }

                while (!DomNode.READY_STATE_COMPLETE.equals(page.getReadyState()))
                {
                    Thread.sleep(500);
                }

                return page;

            }
            finally
            {
                webClient.removeWebWindowListener(windowListener);
            }

        }
    }

    /**
     * @return
     */
    public static HtmlUnitScriptCommands createAdapter()
    {
        return (HtmlUnitScriptCommands) Proxy.newProxyInstance(HtmlUnitCommandAdapter.class.getClassLoader(), new Class<?>[]
            {
                HtmlUnitScriptCommands.class
            }, new CommandsInvocationHandler<HtmlUnitScriptCommands>(new HtmlUnitCommandAdapter(), AbstractCommandAdapter.LOGGER));
    }
}
