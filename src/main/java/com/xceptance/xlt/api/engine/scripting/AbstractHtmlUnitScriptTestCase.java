/*
 * Copyright (c) 2005-2021 Xceptance Software Technologies GmbH
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
package com.xceptance.xlt.api.engine.scripting;

import org.junit.After;
import org.junit.Before;

import com.xceptance.xlt.api.tests.AbstractTestCase;
import com.xceptance.xlt.engine.scripting.TestContext;
import com.xceptance.xlt.engine.scripting.htmlunit.HtmlUnitCommandAdapter;
import com.xceptance.xlt.engine.scripting.htmlunit.HtmlUnitScriptCommands;

/**
 * Base class of all scripted tests that use the Action-based API.
 * 
 * @author Hartmut Arlt (Xceptance Software Technologies GmbH)
 */
public abstract class AbstractHtmlUnitScriptTestCase extends AbstractTestCase
{
    private final HtmlUnitScriptCommands _adapter;

    /**
     * Constructor.
     * 
     * @param baseUrl
     *            the base URL to use for resolution of relative URLs
     */
    public AbstractHtmlUnitScriptTestCase(final String baseUrl)
    {
        _adapter = HtmlUnitCommandAdapter.createAdapter();
        TestContext.getCurrent().setBaseUrl(baseUrl);
    }

    /**
     * Default constructor.
     */
    public AbstractHtmlUnitScriptTestCase()
    {
        this(null);
    }

    /**
     * Performs additional setup tasks for HtmlUnit/action-based exported script test cases. Don't call this method
     * directly, it will be called implicitly by the JUnit framework.
     */
    @Before
    public final void __setUpAbstractHtmlUnitScriptTestCase()
    {
        TestContext.getCurrent().pushScope(this);
        TestContext.getCurrent().setAdapter(_adapter);
    }

    /**
     * Performs additional cleanup tasks for HtmlUnit/action-based exported script test cases. Don't call this method
     * directly, it will be called implicitly by the JUnit framework.
     */
    @After
    public final void __cleanUpAbstractHtmlUnitScriptTestCase()
    {
        TestContext.getCurrent().shutDown();
    }

    /**
     * Resolves the given string.
     * 
     * @param resolvable
     *            the string to be resolved
     * @return the resolved string
     */
    protected String resolve(final String resolvable)
    {
        return TestContext.getCurrent().resolve(resolvable);
    }

    /**
     * Resolves the given test data key.
     * 
     * @param key
     *            the key string containing only the name of a test data field
     * @return resolved string or <code>null</code> if not found
     */
    protected String resolveKey(final String key)
    {
        return TestContext.getCurrent().resolveKey(key);
    }

    /**
     * Returns whether or not the given expression evaluates to <code>true</code>.
     * 
     * @param jsExpression
     *            the JavaScript expression to evaluate
     * @return <code>true</code> if and only if the given JavaScript expression is not blank and evaluates to
     *         <code>true</code>
     */
    protected boolean evaluatesToTrue(final String jsExpression)
    {
        return _adapter.evaluatesToTrue(jsExpression);
    }
}
