/*
 * Copyright (c) 2005-2025 Xceptance Software Technologies GmbH
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
package com.xceptance.xlt.engine.scripting.util;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xceptance.common.util.RegExUtils;
import com.xceptance.xlt.api.util.XltException;
import com.xceptance.xlt.api.util.XltProperties;
import com.xceptance.xlt.engine.PageStatistics;
import com.xceptance.xlt.engine.TimeoutException;
import com.xceptance.xlt.engine.scripting.ScriptException;
import com.xceptance.xlt.engine.scripting.TestContext;
import com.xceptance.xlt.engine.scripting.util.ReplayUtils.AttributeLocatorInfo;
import com.xceptance.xlt.engine.util.TimerUtils;
import com.xceptance.xlt.engine.util.URLInfo;
import com.xceptance.xlt.engine.util.UrlUtils;

/**
 * Base class for command adapters. Primarily used to share common code.
 *
 * @author Hartmut Arlt (Xceptance Software Technologies GmbH)
 */
public abstract class AbstractCommandAdapter implements CommonScriptCommands
{
    protected static final Pattern FRAME_INDEX_PATTERN = Pattern.compile("^index=\\d+$");

    protected static final Pattern FRAME_NAME_LOCATOR_PATTERN;

    protected static final Pattern FRAME_NAME_PATTERN;

    protected static final Pattern VARIABLE_PATTERN = Pattern.compile("[A-Za-z][A-Za-z0-9_]*");

    protected static final Pattern ATT_LOCATOR_PATTERN = Pattern.compile("\\S+@[^\\d\\s]\\S*$");

    static
    {
        final String framePatternString = "frames\\[(['\"])([^'\"]+)\\1\\](\\.frames\\[(['\"])[^'\"]+\\4\\])*";
        FRAME_NAME_LOCATOR_PATTERN = Pattern.compile("^dom=" + framePatternString + "$");
        FRAME_NAME_PATTERN = Pattern.compile(framePatternString + "?");
    }

    public static final Logger LOGGER = LoggerFactory.getLogger(AbstractCommandAdapter.class);

    /**
     * {@inheritDoc}
     */
    @Override
    public void assertAttribute(final String attributeLocator, final String textPattern)
    {
        assureCondition(attributeMatches(attributeLocator, textPattern, true));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void assertAttribute(final String elementLocator, final String attributeName, final String textPattern)
    {
        assureCondition(attributeMatches(elementLocator, attributeName, textPattern, true));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void assertClass(final String elementLocator, final String clazzString)
    {
        assureCondition(classMatches(elementLocator, clazzString, true));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void assertElementCount(final String elementLocator, final int count)
    {
        executeRunnable(count == 0, () -> assureCondition(elementCountEqual(elementLocator, count, true)));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void assertElementCount(final String elementLocator, final String count)
    {
        assertElementCount(elementLocator, Integer.parseInt(count));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void assertElementPresent(final String elementLocator)
    {
        assureCondition(elementPresent(elementLocator, true));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void assertEval(final String expression, final String textPattern)
    {
        assureCondition(evalMatches(expression, textPattern, true));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void assertLoadTime(final long maxLoadTime)
    {
        Assert.assertFalse("Page load time exceeded", PageStatistics.getPageStatistics().getLoadTime() > maxLoadTime);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void assertLoadTime(final String maxLoadTime)
    {
        assertLoadTime(Long.parseLong(maxLoadTime));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void assertNotAttribute(final String attributeLocator, final String textPattern)
    {
        assureCondition(attributeMatches(attributeLocator, textPattern, false));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void assertNotAttribute(final String elementLocator, final String attributeName, final String textPattern)
    {
        assureCondition(attributeMatches(elementLocator, attributeName, textPattern, false));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void assertNotClass(final String elementLocator, final String clazzString)
    {
        assureCondition(classMatches(elementLocator, clazzString, false));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void assertNotElementCount(final String elementLocator, final int count)
    {
        executeRunnable(count != 0, () -> assureCondition(elementCountEqual(elementLocator, count, false)));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void assertNotElementCount(final String elementLocator, final String count)
    {
        assertNotElementCount(elementLocator, Integer.parseInt(count));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void assertNotElementPresent(final String elementLocator)
    {
        executeRunnable(true, () -> assureCondition(elementPresent(elementLocator, false)));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void assertNotEval(final String expression, final String textPattern)
    {
        assureCondition(evalMatches(expression, textPattern, false));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void assertNotText(final String elementLocator, final String textPattern)
    {
        assureCondition(textMatches(elementLocator, textPattern, false));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void assertNotTextPresent(final String textPattern)
    {
        assureCondition(pageTextMatches(textPattern, false));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void assertNotTitle(final String titlePattern)
    {
        assureCondition(titleMatches(titlePattern, false));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void assertNotXpathCount(final String xpath, final int count)
    {
        executeRunnable(count != 0, () -> assureCondition(xpathCountEqual(xpath, count, false)));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void assertNotXpathCount(final String xpath, final String count)
    {
        assertNotXpathCount(xpath, Integer.parseInt(count));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void assertPageSize(final long maxPageSize)
    {
        Assert.assertFalse("Page size exceeded", PageStatistics.getPageStatistics().getTotalBytes() > maxPageSize);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void assertPageSize(final String maxPageSize)
    {
        assertPageSize(Long.parseLong(maxPageSize));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasText(final String elementLocator, final String textPattern)
    {
        return textMatches(elementLocator, textPattern, true).isTrue();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasValue(final String elementLocator, final String valuePattern)
    {
        return valueMatches(elementLocator, valuePattern, true).isTrue();
    }

    /**
     * {@inheritDoc}
     */
    @Override

    public void assertText(final String elementLocator, final String textPattern)
    {
        assureCondition(textMatches(elementLocator, textPattern, true));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void assertValue(final String elementLocator, final String valuePattern)
    {
        assureCondition(valueMatches(elementLocator, valuePattern, true));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void assertNotValue(final String elementLocator, final String valuePattern)
    {
        assureCondition(valueMatches(elementLocator, valuePattern, false));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isTextPresent(final String textPattern)
    {
        return pageTextMatches(textPattern, true).isTrue();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void assertTextPresent(final String textPattern)
    {
        assureCondition(pageTextMatches(textPattern, true));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasTitle(final String title)
    {
        return titleMatches(title, true).isTrue();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void assertTitle(final String title)
    {
        assureCondition(titleMatches(title, true));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void assertXpathCount(final String xpath, final int count)
    {
        executeRunnable(count == 0, () -> assureCondition(xpathCountEqual(xpath, count, true)));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void assertXpathCount(final String xpath, final String count)
    {
        assertXpathCount(xpath, Integer.parseInt(count));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void echo(final String message)
    {
        checkIsTrue("Invalid message: " + message, message != null);

        if (AbstractCommandAdapter.LOGGER.isInfoEnabled())
        {
            AbstractCommandAdapter.LOGGER.info("echo : " + message);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setTimeout(final long timeout)
    {
        TestContext.getCurrent().setTimeout(timeout);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setTimeout(final String timeout)
    {
        setTimeout(Long.parseLong(timeout));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void store(final String text, final String variableName)
    {
        checkVariable(variableName);
        checkIsTrue("Cannot store null reference", text != null);

        TestContext.getCurrent().storeValue(variableName, text);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void storeElementCount(final String elementLocator, final String variableName)
    {
        checkVariable(variableName);

        final int nbElements = getElementCount(elementLocator);
        TestContext.getCurrent().storeValue(variableName, Integer.toString(nbElements));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void storeEval(final String expression, final String variable)
    {
        checkVariable(variable);

        final String result = evaluate(expression);
        checkIsTrue("Failed to evaluate expression '" + expression + "'", result != null);

        TestContext.getCurrent().storeValue(variable, result);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void storeText(final String locator, final String variable)
    {
        checkVariable(variable);

        final String elementText = getText(locator);
        TestContext.getCurrent().storeValue(variable, elementText);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void storeTitle(final String variable)
    {
        checkVariable(variable);

        final String title = getTitle();
        TestContext.getCurrent().storeValue(variable, title);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void storeValue(final String elementLocator, final String variableName)
    {
        checkVariable(variableName);

        final String elementText = getValue(elementLocator);
        TestContext.getCurrent().storeValue(variableName, elementText);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void storeXpathCount(final String xpath, final String variable)
    {
        checkVariable(variable);

        final int nbElements = getXpathCount(xpath);
        TestContext.getCurrent().storeValue(variable, Integer.toString(nbElements));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void storeAttribute(final String attributeLocator, final String variable)
    {
        checkVariable(variable);

        final String attributeValue = getAttribute(attributeLocator);
        TestContext.getCurrent().storeValue(variable, attributeValue);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void storeAttribute(final String elementLocator, final String attributeName, final String variable)
    {
        checkVariable(variable);

        final String attributeValue = getAttribute(elementLocator, attributeName);
        TestContext.getCurrent().storeValue(variable, attributeValue);
    }

    /**
     * Checks the given attribute name for correctness.
     *
     * @param attributeName
     *            the name of the attribute to be checked
     */
    protected void checkAttributeName(final String attributeName)
    {
        checkIsTrue("Attribute name must not be blank", StringUtils.isNotBlank(attributeName));
    }

    /**
     * Checks the given variable name for correctness.
     *
     * @param variable
     *            the variable name to be checked
     */
    protected void checkVariable(final String variable)
    {
        checkIsTrue("Name of variable '" + variable + "' is invalid", RegExUtils.isMatching(variable, VARIABLE_PATTERN));
    }

    /**
     * Checks the given element attribute locator for correctness.
     *
     * @param attributeLocator
     *            the element attribute locator to be checked
     */
    protected void checkAttributeLocator(final String attributeLocator)
    {
        checkIsTrue("Attribute locator '" + attributeLocator + "' is invalid",
                    RegExUtils.isMatching(attributeLocator, ATT_LOCATOR_PATTERN));
    }

    /**
     * Checks the given element locator string for correctness.
     *
     * @param elementLocator
     *            the element locator string to be checked
     */
    protected void checkElementLocator(final String elementLocator)
    {
        checkIsTrue("Element locator is null", elementLocator != null);
    }

    /**
     * Checks the given objects for equality and throws an exception with the given message otherwise.
     *
     * @param message
     *            the exception message
     * @param expected
     *            the exception value
     * @param actual
     *            the actual value
     */
    protected void checkIsEqual(final String message, final Object expected, final Object actual)
    {
        if (actual != expected)
        {
            if (actual == null || !actual.equals(expected))
            {
                throw new XltException(message);
            }
        }
    }

    /**
     * Checks that the given flag is true and throw an exception with the given message otherwise.
     *
     * @param message
     *            the exception message
     * @param flag
     *            the flag to check
     */
    protected void checkIsTrue(final String message, final boolean flag)
    {
        if (!flag)
        {
            throw new XltException(message);
        }
    }

    /**
     * Rewrites parts of the passed URL with values specified in the XLT configuration.
     *
     * @param urlString
     *            the URL to rewrite
     * @return the rewritten URL
     */
    protected URL rewriteUrl(final String urlString)
    {
        final XltProperties props = XltProperties.getInstance();

        // get the actual values from the configuration if available
        final String protocol = props.getProperty("startUrl.protocol", "");

        final String userInfo = props.getProperty("startUrl.userInfo", "");
        final String host = props.getProperty("startUrl.host", "");
        final int port = props.getProperty("startUrl.port", -1);

        final String path = props.getProperty("startUrl.path", "");
        final String query = props.getProperty("startUrl.query", "");
        final String fragment = props.getProperty("startUrl.fragment", "");

        final URLInfo urlInfo = URLInfo.builder().proto(protocol).userInfo(userInfo).host(host).port(port).path(path).query(query)
                                       .fragment(fragment).build();

        try
        {
            return UrlUtils.rewriteUrl(urlString, urlInfo);
        }
        catch (final Exception e)
        {
            throw new ScriptException("Failed to rewrite URL", e);
        }
    }

    /**
     * Waits for the passed condition to be satisfied.
     *
     * @param condition
     *            the condition
     * @throws TimeoutException
     *             if the configured timeout has expired
     */
    protected void waitForCondition(final Condition condition)
    {
        waitForCondition(condition, TestContext.getCurrent().getTimeout());
    }

    /**
     * Waits for the passed condition to be satisfied.
     *
     * @param condition
     *            the condition
     * @param maxWaitingTime
     *            Maximum time to wait. If set lower than <code>0</code> the default timeout is used.
     * @throws TimeoutException
     *             if the configured timeout has expired
     */
    protected void waitForCondition(final Condition condition, final long maxWaitingTime)
    {
        final long timeout = maxWaitingTime < 0 ? TestContext.getCurrent().getTimeout() : maxWaitingTime;
        final long startTime = TimerUtils.get().getStartTime();
        do
        {
            try
            {
                if (condition.isTrue())
                {
                    return;
                }
            }
            catch (final Exception e)
            {
                // check if the exception (or one of its causes) is an InterruptedException
                for (Throwable t = e; t != null; t = t.getCause())
                {
                    if (t instanceof InterruptedException)
                    {
                        // leave loop immediately
                        throw new ScriptException("Interrupted while waiting for condition", e);
                    }
                }

                // ignore any other exception for now
            }

            try
            {
                Thread.sleep(500);
            }
            catch (final InterruptedException ie)
            {
                throw new ScriptException("Interrupted while waiting for condition", ie);
            }
        }
        while (TimerUtils.get().getElapsedTime(startTime) < timeout);

        throw new TimeoutException("Timed out while waiting for condition: " + condition.getReason());
    }

    /**
     * Returns the IDs of all selected options for the given select element locator.
     *
     * @param elementLocator
     *            the select locator
     * @return IDs of selected options
     */
    protected abstract List<String> getSelectedIds(final String elementLocator);

    /**
     * Returns the indices of all selected options for the given select element locator.
     *
     * @param elementLocator
     *            the select locator
     * @return indices of selected options
     */
    protected abstract List<Integer> getSelectedIndices(final String elementLocator);

    /**
     * Returns the labels of all selected options for the given select element locator.
     *
     * @param elementLocator
     *            the select locator
     * @return labels of selected options
     */
    protected abstract List<String> getSelectedLabels(final String elementLocator);

    /**
     * Returns the values of all selected options for the given select element locator.
     *
     * @param elementLocator
     *            the select locator
     * @return values of selected options
     */
    protected abstract List<String> getSelectedValues(final String elementLocator);

    /**
     * Checks that the ID of at least one selected option of the given select element matches the given pattern.
     *
     * @param selectLocator
     *            the select element locator
     * @param idPattern
     *            ID pattern that must match
     * @return <code>true</code> if the ID of at least one selected option of the given select element matches the given
     *         pattern, <code>false</code> otherwise
     */
    public boolean isSelectedId(final String selectLocator, final String idPattern)
    {
        return idSelected(selectLocator, idPattern, true).isTrue();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void assertSelectedId(final String selectLocator, final String idPattern)
    {
        assureCondition(idSelected(selectLocator, idPattern, true));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void assertNotSelectedId(final String selectLocator, final String idPattern)
    {
        assureCondition(idSelected(selectLocator, idPattern, false));
    }

    /**
     * Checks that the option at the given index is selected.
     *
     * @param selectLocator
     *            the select element locator
     * @param indexPattern
     *            the option index pattern
     * @return <code>true</code> if the option at the given index is selected, <code>false</code> otherwise
     */
    public boolean isSelectedIndex(final String selectLocator, final String indexPattern)
    {
        return indexSelected(selectLocator, indexPattern, true).isTrue();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void assertSelectedIndex(final String selectLocator, final String indexPattern)
    {
        assureCondition(indexSelected(selectLocator, indexPattern, true));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void assertNotSelectedIndex(final String selectLocator, final String indexPattern)
    {
        assureCondition(indexSelected(selectLocator, indexPattern, false));
    }

    /**
     * Checks that the label of at least one selected option of the given select element matches the given pattern.
     *
     * @param selectLocator
     *            the select element locator
     * @param labelPattern
     *            the label pattern that must match
     * @return <code>true</code> if the label of at least one selected option of the given select element matches the
     *         given pattern, <code>false</code> otherwise
     */
    public boolean isSelectedLabel(final String selectLocator, final String labelPattern)
    {
        return labelSelected(selectLocator, labelPattern, true).isTrue();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void assertSelectedLabel(final String selectLocator, final String labelPattern)
    {
        assureCondition(labelSelected(selectLocator, labelPattern, true));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void assertNotSelectedLabel(final String selectLocator, final String labelPattern)
    {
        assureCondition(labelSelected(selectLocator, labelPattern, false));
    }

    /**
     * Checks that the value of at least one selected option of the given select element matches the given pattern.
     *
     * @param selectLocator
     *            the select element locator
     * @param valuePattern
     *            the value pattern that must match
     * @return <code>true</code> if the value of at least one selected option of the given select element matches the
     *         given pattern, <code>false</code> otherwise
     */
    public boolean isSelectedValue(final String selectLocator, final String valuePattern)
    {
        return valueSelected(selectLocator, valuePattern, true).isTrue();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void assertSelectedValue(final String selectLocator, final String valuePattern)
    {
        assureCondition(valueSelected(selectLocator, valuePattern, true));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void assertNotSelectedValue(final String selectLocator, final String valuePattern)
    {
        assureCondition(valueSelected(selectLocator, valuePattern, false));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void assertVisible(final String elementLocator)
    {
        assureCondition(elementVisible(elementLocator, true));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void assertNotVisible(final String elementLocator)
    {
        assureCondition(elementVisible(elementLocator, false));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void assertChecked(final String elementLocator)
    {
        assureCondition(elementChecked(elementLocator, true));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void assertNotChecked(final String elementLocator)
    {
        assureCondition(elementChecked(elementLocator, false));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void assertStyle(final String elementLocator, final String style)
    {
        assureCondition(styleMatches(elementLocator, style, true));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void assertNotStyle(final String elementLocator, final String style)
    {
        assureCondition(styleMatches(elementLocator, style, false));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isEvalMatching(final String expression, final String textPattern)
    {
        return evalMatches(expression, textPattern, true).isTrue();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasAttribute(final String attributeLocator, final String textPattern)
    {
        return attributeMatches(attributeLocator, textPattern, true).isTrue();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasAttribute(final String elementLocator, final String attributeName, final String textPattern)
    {
        return attributeMatches(elementLocator, attributeName, textPattern, true).isTrue();
    }

    /**
     * Returns the effective style of the element identified by the given element locator.
     *
     * @param elementLocator
     *            the element locator
     * @param propertyName
     *            the CSS property name whose effective value should be computed
     * @return effective value of given CSS property value of the given element or <code>null</code> in case there is no
     *         such CSS property set
     */
    protected abstract String _getEffectiveStyle(final String elementLocator, final String propertyName);

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasStyle(final String elementLocator, final String style)
    {
        return styleMatches(elementLocator, style, true).isTrue();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasNotStyle(final String elementLocator, final String style)
    {
        return styleMatches(elementLocator, style, false).isTrue();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasClass(final String elementLocator, final String clazz)
    {
        return classMatches(elementLocator, clazz, true).isTrue();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasNotClass(final String elementLocator, final String clazz)
    {
        return classMatches(elementLocator, clazz, false).isTrue();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String evaluate(String jsExpression)
    {
        checkIsTrue("Expression is null", jsExpression != null);

        return _evaluate(jsExpression);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean evaluatesToTrue(final String jsExpression)
    {
        if (StringUtils.isBlank(jsExpression))
        {
            return false;
        }

        final String result = evaluate("!!(".concat(jsExpression).concat(")"));
        checkIsTrue("Failed to evaluate expression: " + jsExpression, result != null);

        return Boolean.valueOf(result);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getAttribute(final String attributeLocator)
    {
        checkAttributeLocator(attributeLocator);

        return _getAttribute(attributeLocator);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getAttribute(final String elementLocator, final String attributeName)
    {
        checkElementLocator(elementLocator);
        checkAttributeName(attributeName);

        return _getAttribute(elementLocator, attributeName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getElementCount(String elementLocator)
    {
        checkElementLocator(elementLocator);

        return _getElementCount(elementLocator);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getText(String elementLocator)
    {
        checkElementLocator(elementLocator);

        return _getText(elementLocator);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getValue(String elementLocator)
    {
        checkElementLocator(elementLocator);

        return _getValue(elementLocator);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getXpathCount(String xpath)
    {
        checkIsTrue("XPath expression is null", xpath != null);

        return _getXpathCount(xpath);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isChecked(String elementLocator)
    {
        checkElementLocator(elementLocator);

        return _isChecked(elementLocator);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isElementPresent(String elementLocator)
    {
        checkElementLocator(elementLocator);

        return _isElementPresent(elementLocator);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isEnabled(String elementLocator)
    {
        checkElementLocator(elementLocator);

        return _isEnabled(elementLocator);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isVisible(String elementLocator)
    {
        checkElementLocator(elementLocator);

        return _isVisible(elementLocator);
    }

    protected String _getAttribute(final String attributeLocator)
    {
        final AttributeLocatorInfo info = ReplayUtils.parseAttributeLocator(attributeLocator);
        return _getAttribute(info.getElementLocator(), info.getAttributeName());
    }

    protected abstract String _getAttribute(final String elementLocator, final String attributeName);

    protected abstract String _getText(final String elementLocator);

    protected abstract String _getValue(final String elementLocator);

    protected abstract String _evaluate(final String expression);

    protected abstract int _getElementCount(final String elementLocator);

    protected abstract int _getXpathCount(final String xpathExpression);

    protected abstract boolean _isChecked(final String elementLocator);

    protected abstract boolean _isElementPresent(final String elementLocator);

    protected abstract boolean _isEnabled(final String elementLocator);

    protected abstract boolean _isVisible(final String elementLocator);

    /*
     * === Conditions ===
     */

    protected Condition attributeMatches(final String attributeLocator, final String textPattern, final boolean positiveMatch)
    {
        checkAttributeLocator(attributeLocator);
        checkIsTrue("Text pattern is null", textPattern != null);

        return new Condition(positiveMatch ? "ATTRIBUTE MATCH" : "ATTRIBUTE NO-MATCH")
        {

            @Override
            protected boolean evaluate()
            {
                final String attValue = _getAttribute(attributeLocator);
                if (attValue == null)
                {
                    throw new NoSuchElementAttributeException("Couldn't find element attribute: " + attributeLocator);
                }

                final boolean matches = TextMatchingUtils.isAMatch(attValue, textPattern, true, false);
                final String format = "Attribute value '%s' " + (matches ? "matches" : "does not match");
                setReason(String.format(format, attValue));

                return positiveMatch ? matches : !matches;
            }
        };

    }

    protected Condition attributeMatches(final String elementLocator, final String attributeName, final String textPattern,
                                         final boolean positiveMatch)
    {
        checkElementLocator(elementLocator);
        checkIsTrue("Attribute name must not be blank", StringUtils.isNotBlank(attributeName));
        checkIsTrue("Text pattern is null", textPattern != null);

        return new Condition(positiveMatch ? "ATTRIBUTE MATCH" : "ATTRIBUTE NO-MATCH")
        {

            @Override
            protected boolean evaluate()
            {
                final String attValue = _getAttribute(elementLocator, attributeName);
                if (attValue == null)
                {
                    throw new NoSuchElementAttributeException("Element [" + elementLocator + "] does not have such an attribute: " +
                                                              attributeName);
                }

                final boolean matches = TextMatchingUtils.isAMatch(attValue, textPattern, true, false);
                final String format = "Attribute value '%s' " + (matches ? "matches" : "does not match");
                setReason(String.format(format, attValue));

                return positiveMatch ? matches : !matches;
            }
        };

    }

    protected Condition styleMatches(final String elementLocator, final String style, final boolean positiveMatch)
    {
        checkElementLocator(elementLocator);
        checkIsTrue("Style is null", StringUtils.isNotBlank(style));

        return new Condition(positiveMatch ? "STYLE MATCH" : "STYLE NO-MATCH")
        {

            private final Map<String, String> cssProps = ReplayUtils.parseStyleString(style);

            @Override
            protected boolean evaluate()
            {

                final List<Object> props = new ArrayList<>();

                boolean match = true;
                for (final Map.Entry<String, String> entry : cssProps.entrySet())
                {
                    final String propName = entry.getKey();
                    final String expValue = entry.getValue();
                    final String cssValue = _getEffectiveStyle(elementLocator, entry.getKey());

                    if (expValue.equals(cssValue))
                    {
                        if (!positiveMatch)
                        {
                            props.add(propName);
                        }
                    }
                    else
                    {
                        match = false;
                        if (positiveMatch)
                        {
                            props.add(new StylePropertyResultInfo(propName, cssValue, expValue));
                        }
                    }
                }

                {
                    final StringBuilder sb = new StringBuilder(256);
                    sb.append("Actual style '").append(style).append("' ");
                    if (match)
                    {
                        sb.append("matches");
                    }
                    else
                    {
                        sb.append("does not match");
                    }
                    if (!props.isEmpty())
                    {
                        sb.append(" (");

                        if (positiveMatch)
                        {
                            sb.append("non-");
                        }

                        sb.append("matching properties: ");
                        sb.append(StringUtils.join(props, ", "));
                        sb.append(')');
                    }

                    setReason(sb.toString());
                }

                return props.isEmpty();
            }

        };
    }

    protected Condition pageTextMatches(final String textPattern, final boolean positiveMatch)
    {
        checkIsTrue("Text pattern is null", textPattern != null);

        return new Condition(positiveMatch ? "PAGE TEXT MATCH" : "PAGE TEXT NO-MATCH")
        {
            /**
             * {@inheritDoc}
             */
            @Override
            protected boolean evaluate()
            {
                final boolean match = TextMatchingUtils.isAMatch(getPageText(), textPattern, false, true);
                setReason("Page text " + (match ? "matches" : "does not match"));
                return positiveMatch ? match : !match;

            }
        };
    }

    protected Condition textMatches(final String elementLocator, final String textPattern, final boolean positiveMatch)
    {
        checkElementLocator(elementLocator);
        checkIsTrue("Text pattern is null", textPattern != null);

        return new Condition(positiveMatch ? "ELEMENT TEXT MATCH" : "ELEMENT TEXT NO-MATCH")
        {

            @Override
            protected boolean evaluate()
            {
                final String text = _getText(elementLocator);
                final boolean match = TextMatchingUtils.isAMatch(text, textPattern, true, true);
                final String format = "Element text '%s' " + (match ? "matches" : "does not match");
                setReason(String.format(format, text));

                return positiveMatch ? match : !match;
            }
        };
    }

    protected Condition titleMatches(final String titlePattern, final boolean positiveMatch)
    {
        checkIsTrue("Title pattern is null", titlePattern != null);

        return new Condition(positiveMatch ? "TITLE MATCH" : "TITLE NO-MATCH")
        {
            @Override
            protected boolean evaluate()
            {
                final String pageTitle = getTitle();
                final boolean match = TextMatchingUtils.isAMatch(pageTitle, titlePattern, true, true);
                final String format = "Page title '%s' " + (match ? "matches" : "does not match");
                setReason(String.format(format, pageTitle));

                return positiveMatch ? match : !match;
            }
        };
    }

    protected Condition valueMatches(final String elementLocator, final String valuePattern, final boolean positiveMatch)
    {
        checkElementLocator(elementLocator);
        checkIsTrue("Value pattern is null", valuePattern != null);

        return new Condition(positiveMatch ? "ELEMENT VALUE MATCH" : "ELEMENT VALUE NO-MATCH")
        {

            @Override
            protected boolean evaluate()
            {

                final String elementValue = _getValue(elementLocator);
                final boolean match = TextMatchingUtils.isAMatch(elementValue, valuePattern, true, true);
                final String format = "Element's value '%s' " + (match ? "matches" : "does not match");
                setReason(String.format(format, elementValue));

                return positiveMatch ? match : !match;
            }
        };
    }

    protected Condition classMatches(final String elementLocator, final String classString, final boolean positiveMatch)
    {
        checkElementLocator(elementLocator);
        checkIsTrue("Class string is null", classString != null);

        return new Condition(positiveMatch ? "CLASS MATCH" : "CLASS NO-MATCH")
        {
            private final String[] clazzes = classString.split("\\s+");

            /**
             * {@inheritDoc}
             */
            @Override
            protected boolean evaluate()
            {
                final String clazzAttribute = StringUtils.defaultString(_getAttribute(elementLocator, "class"));
                final Set<String> classesSet = new HashSet<String>(Arrays.asList(clazzAttribute.split("\\s+")));

                final List<String> classList = new ArrayList<>();
                boolean match = true;

                for (final String s : clazzes)
                {
                    if (!StringUtils.isEmpty(s))
                    {
                        if (classesSet.contains(s))
                        {
                            if (!positiveMatch)
                            {
                                classList.add(s);
                            }
                        }
                        else
                        {
                            match = false;
                            if (positiveMatch)
                            {
                                classList.add(s);
                            }
                        }
                    }
                }

                final String format = "Element's class attribute '%s' " + (match ? "matches" : "does not match") + " (" +
                                      (positiveMatch ? "missing" : "found") + " classes: %s)";
                setReason(String.format(format, clazzAttribute, StringUtils.defaultIfBlank(StringUtils.join(classList, ", "), "none")));

                return classList.isEmpty();
            }
        };
    }

    protected Condition evalMatches(final String expression, final String textPattern, final boolean positiveMatch)
    {
        checkIsTrue("Expression to evaluate is null", expression != null);
        checkIsTrue("Text pattern is null", textPattern != null);

        return new Condition(positiveMatch ? "EVAL MATCH" : "EVAL NO-MATCH")
        {

            @Override
            protected boolean evaluate()
            {
                final String text = _evaluate(expression);
                final boolean match = TextMatchingUtils.isAMatch(text, textPattern, true, false);
                final String format = "Result of evaluation '%s' " + (match ? "matches" : "does not match");
                setReason(String.format(format, text));

                return positiveMatch ? match : !match;
            }

        };
    }

    protected Condition elementChecked(final String elementLocator, final boolean positiveMatch)
    {
        checkElementLocator(elementLocator);

        return new Condition(positiveMatch ? "ELEMENT CHECKED" : "ELEMENT UNCHECKED")
        {

            @Override
            protected boolean evaluate()
            {
                final boolean checked = _isChecked(elementLocator);
                setReason("Checkbox/radio element is " + (checked ? "checked" : "not checked"));
                return positiveMatch ? checked : !checked;
            }

        };
    }

    protected Condition elementPresent(final String elementLocator, final boolean positive)
    {
        checkElementLocator(elementLocator);

        return new Condition(positive ? "ELEMENT PRESENT" : "ELEMENT ABSENT")
        {

            @Override
            protected boolean evaluate()
            {
                final boolean found = _isElementPresent(elementLocator);
                setReason(found ? "Element found" : "Element not found");

                return positive ? found : !found;
            }

        };
    }

    protected Condition elementVisible(final String elementLocator, final boolean positiveMatch)
    {
        checkElementLocator(elementLocator);

        return new Condition(positiveMatch ? "ELEMENT VISIBLE" : "ELEMENT INVISIBLE")
        {
            /**
             * {@inheritDoc}
             */
            @Override
            protected boolean evaluate()
            {
                final boolean visible = _isVisible(elementLocator);
                setReason("Element is " + (visible ? "visible" : "invisible"));

                return positiveMatch ? visible : !visible;
            }

        };
    }

    protected Condition elementCountEqual(final String elementLocator, final int count, final boolean positive)
    {
        checkElementLocator(elementLocator);
        checkIsTrue("Element count is negative", count > -1);

        return new Condition(positive ? "ELEMENT COUNT EQUAL" : "ELEMENT COUNT DIFFERENT")
        {

            /**
             * {@inheritDoc}
             */
            @Override
            protected boolean evaluate()
            {
                final int nbElements = _getElementCount(elementLocator);
                final boolean isEqual = count == nbElements;
                if (isEqual)
                {
                    setReason(String.format("Number of matching elements is equal to '%d'", count));
                }
                else
                {
                    setReason(String.format("Invalid number of matching elements, expected <%d> but was <%d>", count, nbElements));
                }

                return positive ? isEqual : !isEqual;
            }

        };
    }

    protected Condition xpathCountEqual(final String xpath, final int count, final boolean positive)
    {
        checkIsTrue("XPath expression is null", xpath != null);
        checkIsTrue("Element count is negative", count > -1);

        return new Condition(positive ? "XPATH COUNT EQUAL" : "XPATH COUNT DIFFERENT")
        {
            /**
             * {@inheritDoc}
             */
            @Override
            protected boolean evaluate()
            {
                final int nbElements = _getXpathCount(xpath);
                final boolean equal = count == nbElements;
                if (equal)
                {
                    setReason(String.format("Number of matching elements is equal to '%d'", count));
                }
                else
                {
                    setReason(String.format("Invalid number of matching elements, expected <%d> but was <%d>", count, nbElements));
                }

                return positive ? equal : !equal;
            }
        };
    }

    protected Condition idSelected(final String selectLocator, final String idPattern, final boolean positiveMatch)
    {
        checkIsTrue("Select locator is null", selectLocator != null);
        checkIsTrue("ID pattern is null", idPattern != null);

        return new Condition(positiveMatch ? "SELECTED ID MATCH" : "SELECTED ID NO-MATCH")
        {

            /**
             * {@inheritDoc}
             */
            @Override
            protected boolean evaluate()
            {
                boolean match = false;
                for (final String id : getSelectedIds(selectLocator))
                {
                    if (TextMatchingUtils.isAMatch(id, idPattern, true, false))
                    {
                        match = true;
                        break;
                    }

                }
                setReason((match ? "At least one" : "No") + " selected option found whose ID matches the specified pattern");

                return positiveMatch ? match : !match;
            }
        };
    }

    protected Condition indexSelected(final String selectLocator, final String indexPattern, final boolean positiveMatch)
    {
        checkIsTrue("Select locator is null", selectLocator != null);
        checkIsTrue("Index pattern is null", indexPattern != null);

        return new Condition(positiveMatch ? "SELECTED INDEX MATCH" : "SELECTED INDEX NO-MATCH")
        {

            /**
             * {@inheritDoc}
             */
            @Override
            protected boolean evaluate()
            {
                boolean match = false;
                for (final Integer idx : getSelectedIndices(selectLocator))
                {
                    if (TextMatchingUtils.isAMatch(idx.toString(), indexPattern, true, false))
                    {
                        match = true;
                        break;
                    }
                }
                setReason((match ? "At least one" : "No") + " selected option found whose index matches the specified pattern");

                return positiveMatch ? match : !match;
            }
        };
    }

    protected Condition labelSelected(final String selectLocator, final String labelPattern, final boolean positiveMatch)
    {

        checkIsTrue("Select locator is null", selectLocator != null);
        checkIsTrue("Label pattern is null", labelPattern != null);

        return new Condition(positiveMatch ? "SELECTED LABEL MATCH" : "SELECTED LABEL NO-MATCH")
        {
            /**
             * {@inheritDoc}
             */
            @Override
            protected boolean evaluate()
            {
                boolean match = false;
                for (final String label : getSelectedLabels(selectLocator))
                {
                    if (TextMatchingUtils.isAMatch(label, labelPattern, true, true))
                    {
                        match = true;
                        break;
                    }
                }
                setReason((match ? "At least one" : "No") + " selected option found whose label matches the specified pattern");

                return positiveMatch ? match : !match;
            }
        };

    }

    protected Condition valueSelected(final String selectLocator, final String valuePattern, final boolean positiveMatch)
    {
        checkIsTrue("Select locator is null", selectLocator != null);
        checkIsTrue("Value pattern is null", valuePattern != null);

        return new Condition(positiveMatch ? "SELECTED VALUE MATCH" : "SELECTED VALUE NO-MATCH")
        {
            /**
             * {@inheritDoc}
             */
            @Override
            protected boolean evaluate()
            {
                boolean match = false;
                for (final String value : getSelectedValues(selectLocator))
                {
                    if (TextMatchingUtils.isAMatch(value, valuePattern, true, false))
                    {
                        match = true;
                        break;
                    }
                }
                setReason((match ? "At least one" : "No") + " selected option found whose value attribute matches the specified pattern");

                return positiveMatch ? match : !match;
            }
        };

    }

    /**
     * Deactivates the implicit wait timeout, but returns its value for later restore.
     *
     * @return the timeout value
     */
    protected long disableImplicitWaitTimeout()
    {
        final TestContext testContext = TestContext.getCurrent();

        final long timeout = testContext.getImplicitTimeout();
        testContext.setImplicitTimeout(0);

        return timeout;
    }

    /**
     * Restores the implicit wait timeout.
     *
     * @param timeout
     *            the timeout value to restore
     */
    protected void enableImplicitWaitTimeout(final long timeout)
    {
        TestContext.getCurrent().setImplicitTimeout(timeout);
    }

    /**
     * Executes the passed {@link Runnable} instance with an implicit wait timeout enabled or disabled during the
     * execution of the runnable.
     *
     * @param disableImplicitWait
     *            whether implicit wait timeout is to be disabled
     * @param runnable
     *            the runnable to execute
     */
    protected void executeRunnable(final boolean disableImplicitWait, final Runnable runnable)
    {
        long timeout = 0;

        if (disableImplicitWait)
        {
            timeout = disableImplicitWaitTimeout();
        }

        try
        {
            runnable.run();
        }
        finally
        {
            if (disableImplicitWait)
            {
                enableImplicitWaitTimeout(timeout);
            }
        }
    }

    // ___________PRIVATE________

    private void assureCondition(final Condition condition)
    {
        if (!condition.isTrue())
        {
            Assert.fail(condition.getReason());
        }
    }

    private static class StylePropertyResultInfo
    {
        private final String propertyName;

        private final String actualValue;

        private final String expectedValue;

        private StylePropertyResultInfo(final String propertyName, final String actualValue, final String expectedValue)
        {
            this.propertyName = propertyName;
            this.actualValue = actualValue;
            this.expectedValue = expectedValue;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String toString()
        {
            return String.format("'%s' (expected '%s' but was '%s')", propertyName, expectedValue, actualValue);
        }
    }
}
