package com.xceptance.xlt.engine.scripting.webdriver;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;

import com.xceptance.common.lang.ThrowableUtils;
import com.xceptance.xlt.api.util.XltProperties;
import com.xceptance.xlt.common.XltConstants;

/**
 * Common utility methods that come in handy when using web drivers.
 */
public final class WebDriverUtils
{
    private static final boolean CHECK_ELEMENT_CLICKABLE;

    static
    {
        CHECK_ELEMENT_CLICKABLE = XltProperties.getInstance().getProperty(XltConstants.XLT_PACKAGE_PATH + ".scripting.check4Clickable",
                                                                          false);
    }

    /**
     * Replaces <code>window.alert()</code> and <code>window.confirm()</code> with non-interactive versions that assume
     * that the OK buttons was pressed, but only if the passed web driver is JS-enabled.
     * 
     * @param webDriver
     *            the web driver to use
     */
    public static void assumeOkOnAlertOrConfirm(final WebDriver webDriver)
    {
        // simply replace the alert/confirm functions
        final String script = "window.alert = function(msg){}; window.confirm = function(msg){return true}; window.onbeforeunload = function(e){};";
        executeJavaScriptIfPossible(webDriver, script);
    }

    /**
     * Executes the given JavaScript snippet with the passed web driver.
     * 
     * @param webDriver
     *            the web driver to use
     * @param script
     *            the JavaScript snippet
     * @param arguments
     *            the script's arguments
     * @return the script's result
     * @throws UnsupportedOperationException
     *             if the web driver is not JS-enabled or support is turned off
     * @throws WebDriverException
     *             if there was an error when executing the JavaScript snippet
     */
    public static Object executeJavaScript(final WebDriver webDriver, final String script, final Object... arguments)
    {
        if (webDriver instanceof JavascriptExecutor)
        {
            try
            {
                final JavascriptExecutor jsWebDriver = (JavascriptExecutor) webDriver;

                return jsWebDriver.executeScript(script, arguments);
            }
            catch (final Throwable t)
            {
                // add some context info to the raw exception message
                ThrowableUtils.prefixMessage(t, "Failed to evaluate JavaScript: ");
                throw t;
            }
        }
        else
        {
            throw new UnsupportedOperationException(String.format("WebDriver '%s' is not able to execute JavaScript",
                                                                  webDriver.getClass().getName()));
        }
    }

    /**
     * Executes the given JavaScript snippet with the passed web driver. If the web driver is not JS-enabled, the script
     * is not executed and <code>null</code> is returned.
     * 
     * @param webDriver
     *            the web driver to use
     * @param script
     *            the JavaScript snippet
     * @param arguments
     *            the script's arguments
     * @return the script's result, or <code>null</code>
     * @throws WebDriverException
     *             if there was an error when executing the JavaScript snippet
     */
    public static Object executeJavaScriptIfPossible(final WebDriver webDriver, final String script, final Object... arguments)
    {
        try
        {
            return executeJavaScript(webDriver, script, arguments);
        }
        catch (final UnsupportedOperationException uoe)
        {
            // web driver is not capable of executing JavaScript or support is turned off
            return null;
        }
    }

    /**
     * Returns the name of the current window as stored in <code>window.name</code>.
     * 
     * @param webDriver
     *            the web driver to use
     * @return the window's name
     * @throws WebDriverException
     *             if the web driver is not JS-enabled
     */
    public static String getCurrentWindowName(final WebDriver webDriver)
    {
        return (String) executeJavaScript(webDriver, "return window.name");
    }

    /**
     * Sets the given attribute of the given element to the given value.
     * 
     * @param webDriver
     *            the web driver to used
     * @param element
     *            the element whose attribute should be set
     * @param attributeName
     *            the name of the attribute
     * @param attributeValue
     *            the new value of the attribute
     */
    public static void setAttribute(final WebDriver webDriver, final WebElement element, final String attributeName,
                                    final String attributeValue)
    {
        final String script = String.format("arguments[0].%s=%s;", attributeName, attributeValue);
        executeJavaScriptIfPossible(webDriver, script, element);
    }

    /**
     * Fires the click event at the given element.
     * 
     * @param webDriver
     *            the web driver to use
     * @param element
     *            the element to fire the event at
     */
    public static void fireClickEvent(final WebDriver webDriver, final WebElement element)
    {
        fireMouseDownEvent(webDriver, element);
        fireMouseUpEvent(webDriver, element);

        fireMouseEvent(webDriver, element, "click");
    }

    /**
     * Fires the mousedown event at the given element.
     * 
     * @param webDriver
     *            the web driver to use
     * @param element
     *            the element to fire the event at
     */
    public static void fireMouseDownEvent(final WebDriver webDriver, final WebElement element)
    {
        fireMouseEvent(webDriver, element, "mousedown");
    }

    /**
     * Fires the mouseup event at the given element.
     * 
     * @param webDriver
     *            the web driver to use
     * @param element
     *            the element to fire the event at
     */
    public static void fireMouseUpEvent(final WebDriver webDriver, final WebElement element)
    {
        fireMouseEvent(webDriver, element, "mouseup");
    }

    /**
     * Fires the change event at the given element.
     * 
     * @param webDriver
     *            the web driver to use
     * @param element
     *            the element to fire the event at
     */
    public static void fireChangeEvent(final WebDriver webDriver, final WebElement element)
    {
        final String script = "var doc = arguments[0].ownerDocument;" + "if(doc){" + "  var changeEvent = doc.createEvent('HTMLEvents');" +
                              "  changeEvent.initEvent('change', true, true);" + "  arguments[0].dispatchEvent(changeEvent);" + "}";
        executeJavaScriptIfPossible(webDriver, script, element);
    }

    /**
     * Fires the keydown event at the given element.
     * 
     * @param webDriver
     *            the web driver to use
     * @param element
     *            the element to fire the event at
     * @param charCode
     *            the character code
     */
    public static void fireKeyDownEvent(final WebDriver webDriver, final WebElement element, final int charCode)
    {
        fireKeyEvent(webDriver, element, "keydown", charCode);
    }

    /**
     * Fires the keypress event at the given element.
     * 
     * @param webDriver
     *            the web driver to use
     * @param element
     *            the element to fire the event at
     * @param charCode
     *            the character code
     */
    public static void fireKeyPressEvent(final WebDriver webDriver, final WebElement element, final int charCode)
    {
        fireKeyEvent(webDriver, element, "keypress", charCode);
    }

    /**
     * Fires the keyup event at the given element.
     * 
     * @param webDriver
     *            the web driver to use
     * @param element
     *            the element to fire the event at
     * @param charCode
     *            the character code
     */
    public static void fireKeyUpEvent(final WebDriver webDriver, final WebElement element, final int charCode)
    {
        fireKeyEvent(webDriver, element, "keyup", charCode);
    }

    /**
     * Returns the value of the given element's effective style property.
     * 
     * @param webDriver
     *            the web driver to use
     * @param element
     *            the element whose effective style should be computed
     * @param propertyName
     *            the style property whose value should be computed
     * @return value of the given element's effective style property
     */
    public static String getEffectiveStyle(final WebDriver webDriver, final WebElement element, final String propertyName)
    {
        final String script = "function getStyle(element, style){" + "  var value = element.style && element.style[style];" +
                              "  if (!value) {" + "    var doc = element.ownerDocument;" +
                              "    if (doc && doc.defaultView && doc.defaultView.getComputedStyle) {" +
                              "      var css = doc.defaultView.getComputedStyle(element, null);" +
                              "      value = css ? css.getPropertyValue(style) : null;" + "    }" + "  }" +
                              "  return value == 'auto' ? null : value;" + "};" + "function getEffectiveStyle(element, style) {" +
                              "  var effectiveStyle = getStyle(element, style);" +
                              "  if('inherit' === effectiveStyle && element.parentNode) {" +
                              "    return getEffectiveStyle(element.parentNode, style);" + "  }" + "  return effectiveStyle;" + "};" +
                              "return getEffectiveStyle(arguments[0],arguments[1])";
        return (String) executeJavaScript(webDriver, script, element, propertyName);
    }

    /**
     * Returns whether or not the given element is a clickable (is visible and no other element would receive the click
     * event)
     * 
     * @param webDriver
     *            the web driver to use
     * @param element
     *            the element in question
     * @return <code>true</code> if and only if the given element is clickable, <code>false otherwise</code
     */
    public static boolean isClickable(final WebDriver webDriver, final WebElement element)
    {
        return isClickable(webDriver, element, true);
    }

    /**
     * Returns whether or not the given element is a clickable (is visible and no other element would receive the click
     * event).
     * 
     * @param webDriver
     *            the web driver to use
     * @param element
     *            the element in question
     * @param checkVisibility
     *            pass {@code false} if you know that the given element is visible and want to skip the (potentially
     *            expensive) visibility check
     * @return <code>true</code> if and only if the given element is clickable, <code>false otherwise</code
     */
    public static boolean isClickable(final WebDriver webDriver, final WebElement element, final boolean checkVisibility)
    {
        if (!element.isDisplayed())
        {
            return false;
        }

        if (!CHECK_ELEMENT_CLICKABLE || (webDriver instanceof org.openqa.selenium.htmlunit.HtmlUnitDriver) ||
            (webDriver instanceof com.xceptance.xlt.engine.xltdriver.HtmlUnitDriver))
        {
            return true;
        }

        final String script = "var e = arguments[0], rectum = e && e.getBoundingClientRect();" + "try { " +
                              "return e.ownerDocument.elementFromPoint(rectum.left + rectum.width/2, rectum.top+rectum.height/2) === e;" +
                              "} catch(ex) { return false }";
        return (Boolean) executeJavaScript(webDriver, script, element);
    }

    /**
     * Returns whether or not the given element is editable (input/textarea element that is visible and neither readonly
     * nor disabled).
     * 
     * @param webDriver
     *            the web driver to use
     * @param element
     *            the element in question
     * @return <code>true</code> if and only if the given element is editable, <code>false</code> otherwise
     */
    public static boolean isEditable(final WebDriver webDriver, final WebElement element)
    {
        return isEditable(webDriver, element, true);
    }

    /**
     * Returns whether or not the given element is editable (input/textarea element that is visible and neither readonly
     * nor disabled).
     * 
     * @param webDriver
     *            the web driver to use
     * @param element
     *            the element in question
     * @param checkVisibility
     *            pass {@code false} if you know that the given element is visible and want to skip the (potentially
     *            expensive) visibility check
     * @return <code>true</code> if and only if the given element is editable, <code>false</code> otherwise
     */
    public static boolean isEditable(final WebDriver webDriver, final WebElement element, final boolean checkVisibility)
    {
        if (checkVisibility && !element.isDisplayed())
        {
            return false;
        }

        final String script = "function isDisabled(e) {" + "  if(e && 'nodeType' in e && [1,9].indexOf(e.nodeType) > -1) {" +
                              "    switch (e.localName) {" + "      case \"option\":\n" + "      case \"optgroup\":\n" +
                              "        if (e.disabled) {\n" + "          return true;\n" + "        }\n" +
                              "        var parent = e.parentNode;\n" +
                              "        while(parent && parent.nodeType === 1 && ['optgroup','select'].indexOf(parent.localName) < 0) { parent = parent.parentNode; }\n" +
                              "        return isDisabled(parent);\n" + "      case \"button\":\n" + "      case \"input\":\n" +
                              "      case \"select\":\n" + "      case \"textarea\":\n" + "        return e.disabled;\n" +
                              "      default:\n" + "        return false;\n" + "     }" + "  }" + "  return false;" + "}\n" +
                              "var el = arguments[0];" +
                              "if(!el || !('nodeType' in el) || [1,9].indexOf(el.nodeType) < 0) { return false; }" +
                              "var tagName = el.localName, type = el.type;" +
                              "if(['input','textarea'].indexOf(tagName) < 0 || el.readOnly || isDisabled(el)) { return false; }" +
                              "return ('textarea' === tagName || /^color|date(time-local)?|email|file|month|number|password|range|search|tel|text|time|url|week$/.test(type));";
        final Boolean result = (Boolean) executeJavaScriptIfPossible(webDriver, script, element);

        return Boolean.TRUE.equals(result);
    }

    /**
     * Fires an key event of the given type at the given element.
     * 
     * @param webDriver
     *            the web driver to use
     * @param element
     *            the element to fire the event at
     * @param eventType
     *            the type of the key event ('keypress', 'keydown' or 'keyup')
     * @param charCode
     *            the character code
     */
    private static void fireKeyEvent(final WebDriver webDriver, final WebElement element, final String eventType, final int charCode)
    {
        final String script = "var doc = arguments[0].ownerDocument;" + "if(doc){" + "  var event;" + "  try {" +
                              "    event = doc.createEvent('KeyEvents');" + "    event.initKeyEvent('" + eventType +
                              "', true, true, doc.defaultView, false, false, false, false, 0, " + charCode + ");" + "  } catch(e) {" +
                              "    event = new KeyboardEvent('" + eventType +
                              "', { bubbles: true, cancelable: true, view: doc.defaultView, charCode: " + charCode + "});" + "  }" +
                              "  arguments[0].dispatchEvent(event);" + "}";
        executeJavaScriptIfPossible(webDriver, script, element);
    }

    /**
     * Fires a mouse event of the given type at the given element.
     * 
     * @param webDriver
     *            the web driver to use
     * @param element
     *            the element to fire the event at
     * @param eventType
     *            the type of the mouse event ('click', 'mousedown' or 'mouseup')
     */
    private static void fireMouseEvent(final WebDriver webDriver, final WebElement element, final String eventType)
    {
        final String script = "var doc = arguments[0].ownerDocument;" + "if(doc){" + "  var event;" + "  try {" +
                              "    event = doc.createEvent('MouseEvents');" + "    event.initMouseEvent('" + eventType +
                              "', true, true, doc.defaultView, 0, 0, 0, 0, 0, false, false, false, false, 0, null);" + "  } catch(e) {" +
                              "    event = new MouseEvent('" + eventType +
                              "',{ bubbles: true, cancelable: true, view: doc.defaultView });" + "  }" +
                              "  arguments[0].dispatchEvent(event);" + "}";
        executeJavaScript(webDriver, script, element);
    }

    /**
     * Constructor.
     */
    private WebDriverUtils()
    {
    }
}
