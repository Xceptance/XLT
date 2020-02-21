package com.xceptance.xlt.engine.scripting.webdriver;

import java.util.List;
import java.util.regex.Pattern;

import org.openqa.selenium.InvalidSelectorException;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.NoSuchWindowException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * Finder used to find different items, like {@link WebElement}s or window handles.
 * 
 * @author Hartmut Arlt (Xceptance Software Technologies GmbH)
 */
final class WebDriverFinder
{
    /**
     * Pattern used to parse lookup strategies.
     */
    final static Pattern STRATEGY_PATTERN = Pattern.compile("^(\\p{Alpha}+)=(.*)");

    /**
     * Element finder.
     */
    private final ElementFinder finder = new ElementFinder(false);

    /**
     * HTML option finder.
     */
    private final OptionFinder optionFinder = new OptionFinder();

    /**
     * Element finder restricted to visible elements.
     */
    private final ElementFinder visibleFinder = new ElementFinder(true);

    /**
     * Window finder.
     */
    private final WindowFinder windowFinder = new WindowFinder();

    /**
     * Lookup an element for the given locator on the given page.
     * 
     * @param webDriver
     *            the web driver to use
     * @param locator
     *            the element locator
     * @return the first found element
     * @throws InvalidSelectorException
     *             if given element locator is invalid
     * @throws NoSuchElementException
     *             if no element was found
     */
    public WebElement findElement(final WebDriver webDriver, final String locator)
    {
        return findElement(webDriver, locator, false);
    }

    /**
     * Lookup an element for the given locator on the given page.
     * 
     * @param webDriver
     *            the web driver to use
     * @param locator
     *            the element locator
     * @param visibleElementsOnly
     *            flag which indicates if lookup should be restricted to visible elements
     * @return the first found element
     * @throws InvalidSelectorException
     *             if given element locator is invalid
     * @throws NoSuchElementException
     *             if no element was found
     */
    public WebElement findElement(final WebDriver webDriver, final String locator, final boolean visibleElementsOnly)
    {
        return getFinder(visibleElementsOnly).find(webDriver, locator);
    }

    /**
     * Lookup an option of the given HTML select element that match the given option locator.
     * 
     * @param select
     *            the HTML select element
     * @param optionLocator
     *            the option locator
     * @return the first found option matching the given option locator
     * @throws InvalidSelectorException
     *             if given option locator is invalid
     * @throws NoSuchElementException
     *             if no option was found
     */
    public WebElement findOption(final WebElement select, final String optionLocator)
    {
        return optionFinder.findOption(select, optionLocator);
    }

    /**
     * Lookup all options of the given HTML select elements that match the given option locator.
     * 
     * @param select
     *            the HTML select element
     * @param optionLocator
     *            the option locator
     * @return list of matched options
     * @throws InvalidSelectorException
     *             if given option locator is invalid
     * @throws NoSuchElementException
     *             if no option was found
     */
    public List<WebElement> findOptions(final WebElement select, final String optionLocator)
    {
        return optionFinder.findOptions(select, optionLocator);
    }

    /**
     * Finds a window specified by the given window locator.
     * 
     * @param webDriver
     *            the web driver to use
     * @param windowLocator
     *            the window locator
     * @param switchBack
     *            whether or not to switch back to the current window
     * @return the window handle
     * @throws InvalidSelectorException
     *             if given window locator is invalid
     * @throws NoSuchWindowException
     *             if no window was found
     */
    public String findWindow(final WebDriver webDriver, final String windowLocator, final boolean switchBack)
    {
        return windowFinder.find(webDriver, windowLocator, switchBack);
    }

    /**
     * Finds a window specified by the given window locator.
     * 
     * @param webDriver
     *            the web driver to use
     * @param windowLocator
     *            the window locator
     * @return the window handle
     * @throws InvalidSelectorException
     *             if given window locator is invalid
     * @throws NoSuchWindowException
     *             if no window was found
     */
    public String findWindow(final WebDriver webDriver, final String windowLocator)
    {
        return windowFinder.find(webDriver, windowLocator, true);
    }

    /**
     * Returns whether or not there exists at least one element identifiable via the given locator.
     * 
     * @param page
     *            the HTML page to be search on
     * @param locator
     *            the element locator
     * @return <code>true</code> if there exists at least one element for the given element locator, <code>false</code>
     *         otherwise
     */
    public boolean isElementPresent(final WebDriver webDriver, final String locator)
    {
        try
        {
            findElement(webDriver, locator, false);
        }
        catch (final InvalidSelectorException e)
        {
            throw e;
        }
        catch (final NoSuchElementException e)
        {
            return false;
        }
        return true;
    }

    /**
     * Returns the element finder to be used.
     * 
     * @param visibleElementsOnly
     *            flag which indicates if element lookup should be restricted to visible elements
     * @return element finder
     */
    private ElementFinder getFinder(final boolean visibleElementsOnly)
    {
        return visibleElementsOnly ? visibleFinder : finder;
    }

    /**
     * Returns all found elements for the given element locator.
     * 
     * @param webDriver
     *            the webdriver instance
     * @param elementLocator
     *            the element locator
     * @return all found elements that match the given element locator
     */
    public List<WebElement> findElements(WebDriver webDriver, String elementLocator)
    {
        return findElements(webDriver, elementLocator, false);
    }

    /**
     * Returns all found elements for the given element locator.
     * 
     * @param webDriver
     *            the webdriver instance
     * @param elementLocator
     *            the element locator
     * @param visibleElementsOnly
     *            if <code>true</code> result set is restricted to visible elements
     * @return all found elements that match the given element locator
     */
    public List<WebElement> findElements(WebDriver webDriver, String elementLocator, final boolean visibleElementsOnly)
    {
        return getFinder(visibleElementsOnly).findAll(webDriver, elementLocator);
    }
}
