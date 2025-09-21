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
package com.xceptance.xlt.report.mergerules.url;

import com.xceptance.xlt.api.engine.RequestData;

/**
 * We only supply data here, so that we can use the UrlCondition as a decorator for our replacement text.
 * 
 * @author Rene Schwietzke (Xceptance Software Technologies GmbH)
 */
public class UrlEmptyCondition extends UrlCondition
{
    /**
     * Constructor.
     */
    public UrlEmptyCondition()
    {
        // Contains does not cache, so we just setup decoration here
        super("");
    }

    @Override
    protected boolean apply(final RequestData requestData)
    {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    protected CharSequence getReplacementText(final RequestData requestData, final int capturingGroupIndex)
    {
        // this is our replacement text without conditions
        return requestData.getUrl();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String toString()
    {
        final StringBuilder sb = new StringBuilder("{ type: '");
        sb.append(getTypeCode()).append("', ");
        sb.append("pattern: '").append(getPattern());
        sb.append("'}");

        return sb.toString();
    }

}
