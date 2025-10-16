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
package util.xlt.actions;

import com.xceptance.xlt.api.actions.AbstractAction;

/**
 * A dummy specialization of {@link AbstractAction} that does nothing, to be used by unit tests
 * 
 * @author Deniz Altin
 */
public class TestAction extends AbstractAction
{
    public TestAction(AbstractAction previousAction, String timerName)
    {
        super(previousAction, timerName);
    }

    public TestAction(String timerName)
    {
        this(null, timerName);
    }

    public TestAction()
    {
        this(null, null);
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

    /**
     * First call {@link #preValidateSafe()}; if it returns {@code true}, call {@link #run()}, otherwise don't.
     * <p>
     * This is a commonly used pattern.
     * 
     * @return {@code false} if and only if {@link #preValidateSafe()} does
     * @throws Throwable
     *             if {@link #run()} does
     */
    public boolean runIfPossible() throws Throwable
    {
        if (preValidateSafe())
        {
            run();
            return true;
        }

        return false;
    }
}
