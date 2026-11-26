/*
 * Copyright (c) 2005-2026 Xceptance Software Technologies GmbH
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
package com.xceptance.xlt.report.mergerules;

import com.xceptance.xlt.api.engine.ActionData;
import com.xceptance.xlt.api.engine.RequestData;
import com.xceptance.xlt.api.util.XltCharBuffer;

/**
 * Adapter class wrapping {@link ActionData} as {@link RequestData} to reuse the existing request merge rules engine
 * and condition evaluators for grouping actions.
 * 
 * @author Antigravity
 */
public class ActionDataRequestWrapper extends RequestData
{
    /**
     * The wrapped ActionData instance.
     */
    private final ActionData actionData;

    /**
     * Constructor.
     * 
     * @param actionData the action data instance to wrap
     */
    public ActionDataRequestWrapper(final ActionData actionData)
    {
        super(actionData.getName());
        this.actionData = actionData;

        // Populate base TimerData / Data attributes from ActionData
        this.setTime(actionData.getTime());
        this.setRunTime(actionData.getRunTime());
        this.setFailed(actionData.hasFailed());
        this.setAgentName(actionData.getAgentName());
        this.setTransactionName(actionData.getTransactionName());

        // Initialize request-specific fields with safe defaults to prevent NPEs in conditions
        this.setUrl(XltCharBuffer.EMPTY);
        this.setContentType(XltCharBuffer.EMPTY);
        this.setHttpMethod(XltCharBuffer.EMPTY);
        this.setResponseCode(200);
    }

    /**
     * Overrides setName to ensure that changes made during merge rule execution (renaming)
     * propagate back to the wrapped ActionData object.
     */
    @Override
    public void setName(final String name)
    {
        super.setName(name);
        actionData.setName(name);
    }

    /**
     * Returns the wrapped ActionData instance.
     * 
     * @return the wrapped action data
     */
    public ActionData getActionData()
    {
        return actionData;
    }
}
