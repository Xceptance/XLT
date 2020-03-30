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
package com.xceptance.xlt.api.engine.scripting;

import org.junit.Assert;
import org.junit.Test;

import com.xceptance.xlt.api.actions.AbstractHtmlPageAction;
import com.xceptance.xlt.engine.scripting.TestContext;

/**
 * @author Sebastian Oerding
 */
public class AbstractHtmlUnitScriptActionTest
{
    @Test
    public void testConstructors()
    {
        final String timerName = "myTimerName";
        AbstractHtmlUnitScriptAction ahusa = new ConcreteHtmlUnitScriptActionTest(timerName);

        Assert.assertEquals("Wrong timerName! it should be \"" + timerName + "\" but is \"" + ahusa.getTimerName() + "\"!", timerName,
                            ahusa.getTimerName());
        Assert.assertNotNull("There should be a freshly created web client", ahusa.getWebClient());
        Assert.assertSame("Web client mismatch of action and current test context", ahusa.getWebClient(), TestContext.getCurrent()
                                                                                                                     .getWebClient());

        ahusa.closeWebClient(); // just closing to avoid a memory leak

        final AbstractHtmlUnitScriptAction dummy = new ConcreteHtmlUnitScriptActionTest(timerName);
        ahusa = new ConcreteHtmlUnitScriptActionTest(dummy, timerName);

        Assert.assertEquals("Wrong timerName! it should be \"" + timerName + "\" but is \"" + ahusa.getTimerName() + "\"!", timerName,
                            ahusa.getTimerName());
        Assert.assertEquals("Web client mismatch. Forwarding of web client failed in constructor.", dummy.getWebClient(),
                            ahusa.getWebClient());
        Assert.assertEquals("Previous action mismatch. Assignment of previous action failed in constructor.", dummy,
                            ahusa.getPreviousAction());
    }

    private class ConcreteHtmlUnitScriptActionTest extends AbstractHtmlUnitScriptAction
    {
        private ConcreteHtmlUnitScriptActionTest(final AbstractHtmlPageAction previousAction, final String timerName)
        {
            super(previousAction, timerName);
        }

        private ConcreteHtmlUnitScriptActionTest(final String timerName)
        {
            super(timerName);
        }

        @Override
        public void preValidate() throws Exception
        {
        }

        @Override
        protected void execute() throws Exception
        {
        }

        @Override
        protected void postValidate() throws Exception
        {
        }
    }
}
