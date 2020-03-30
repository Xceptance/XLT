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
package action.modules;

import com.xceptance.xlt.api.actions.AbstractHtmlPageAction;
import com.xceptance.xlt.api.engine.scripting.AbstractHtmlUnitActionsModule;

import action.modules.Open_ExamplePage;
import action.modules.selectIframe_actions.iframe_1_byIndex;
import action.modules.selectIframe_actions.iframe_2_byIndex;
import action.modules.selectIframe_actions.iframe_1_byDom;
import action.modules.selectIframe_actions.iframe_3_byDom;
import action.modules.selectIframe_actions.iframe_1_2_byDomCascade;
import action.modules.selectIframe_actions.iframe_23_byIndexCascade;
import action.modules.selectIframe_actions.frame_1_byXpath;
import action.modules.selectIframe_actions.frame_2_byXpath;
import action.modules.selectIframe_actions.frame_1_byName;
import action.modules.selectIframe_actions.frame_3_byName;
import action.modules.selectIframe_actions.frame_1_byID;
import action.modules.selectIframe_actions.frame_2_byID;
import action.modules.selectIframe_actions.frame_1_byID0;
import action.modules.selectIframe_actions.frame_2_byID0;
import action.modules.selectIframe_actions.frame_3_byID;
import action.modules.selectIframe_actions.top_frame;

/**
 * TODO: Add class description
 */
public class selectIframe extends AbstractHtmlUnitActionsModule
{


    /**
     * Constructor.
     */
    public selectIframe()
    {
    }


    /**
     * @{inheritDoc}
     */
    protected AbstractHtmlPageAction execute(final AbstractHtmlPageAction prevAction) throws Throwable
    {
        AbstractHtmlPageAction lastAction = prevAction;
        final Open_ExamplePage open_ExamplePage = new Open_ExamplePage();
        lastAction = open_ExamplePage.run(lastAction);

        lastAction = new iframe_1_byIndex(lastAction);
        lastAction.run();

        lastAction = new iframe_2_byIndex(lastAction);
        lastAction.run();

        lastAction = new iframe_1_byDom(lastAction);
        lastAction.run();

        lastAction = new iframe_3_byDom(lastAction);
        lastAction.run();

        lastAction = new iframe_1_2_byDomCascade(lastAction);
        lastAction.run();

        lastAction = new iframe_23_byIndexCascade(lastAction);
        lastAction.run();

        lastAction = new frame_1_byXpath(lastAction);
        lastAction.run();

        lastAction = new frame_2_byXpath(lastAction);
        lastAction.run();

        lastAction = new frame_1_byName(lastAction);
        lastAction.run();

        lastAction = new frame_3_byName(lastAction);
        lastAction.run();

        lastAction = new frame_1_byID(lastAction);
        lastAction.run();

        lastAction = new frame_2_byID(lastAction);
        lastAction.run();

        lastAction = new frame_1_byID0(lastAction);
        lastAction.run();

        lastAction = new frame_2_byID0(lastAction);
        lastAction.run();

        lastAction = new frame_3_byID(lastAction);
        lastAction.run();

        lastAction = new top_frame(lastAction);
        lastAction.run();


        return lastAction;
    }
}