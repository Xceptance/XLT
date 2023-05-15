/*
 * Copyright (c) 2005-2023 Xceptance Software Technologies GmbH
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
package test.com.xceptance.xlt.engine.scripting.htmlunit;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.xceptance.xlt.api.actions.AbstractHtmlPageAction;
import com.xceptance.xlt.api.engine.scripting.AbstractHtmlUnitScriptTestCase;
import com.xceptance.xlt.engine.scripting.TestContext;

import test.com.xceptance.xlt.engine.scripting.htmlunit._2850_ImplictWaitCanBeEvalTest_actions.TestAction;

/**
 * Checks that certain commands return immediately when searching for non-existing elements.
 */
public class _2850_ImplictWaitCanBeEvalTest extends AbstractHtmlUnitScriptTestCase
{
    @BeforeClass
    public static void beforeClass()
    {
        TestContext.getCurrent().setImplicitTimeout(2000);
    }

    public _2850_ImplictWaitCanBeEvalTest()
    {
        super("http://localhost:8080");
    }

    @Test
    public void test() throws Throwable
    {
        AbstractHtmlPageAction lastAction = null;

        lastAction = new TestAction(lastAction, "testpages/examplePage_1.html");
        lastAction.run();
    }

    @AfterClass
    public static void afterClass()
    {
        final TestContext testContext = TestContext.getCurrent();
        testContext.setImplicitTimeout(testContext.getDefaultImplicitTimeout());
    }
}
