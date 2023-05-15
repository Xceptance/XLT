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
package action.modules;

import com.xceptance.xlt.api.actions.AbstractHtmlPageAction;
import com.xceptance.xlt.api.engine.scripting.AbstractHtmlUnitActionsModule;

import action.modules.selectFrame_actions.open_frame_page;
import action.modules.selectFrame_actions.frame_1_byIndex;
import action.modules.selectFrame_actions.frame_2_byIndex;
import action.modules.selectFrame_actions.frame_1_byDom;
import action.modules.selectFrame_actions.frame_2_byDom;

/**
 * TODO: Add class description
 */
public class selectFrame extends AbstractHtmlUnitActionsModule
{


    /**
     * Constructor.
     */
    public selectFrame()
    {
    }


    /**
     * @{inheritDoc}
     */
    protected AbstractHtmlPageAction execute(final AbstractHtmlPageAction prevAction) throws Throwable
    {
        AbstractHtmlPageAction lastAction = prevAction;
        lastAction = new open_frame_page(lastAction, "testpages/frame.html");
        lastAction.run();

        lastAction = new frame_1_byIndex(lastAction);
        lastAction.run();

        lastAction = new frame_2_byIndex(lastAction);
        lastAction.run();

        lastAction = new frame_1_byDom(lastAction);
        lastAction.run();

        lastAction = new frame_2_byDom(lastAction);
        lastAction.run();


        return lastAction;
    }
}