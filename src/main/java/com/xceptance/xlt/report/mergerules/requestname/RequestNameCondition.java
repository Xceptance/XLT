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
package com.xceptance.xlt.report.mergerules.requestname;

import org.apache.commons.lang3.StringUtils;

import com.xceptance.xlt.api.engine.RequestData;
import com.xceptance.xlt.report.mergerules.Condition;

/**
 * Filters requests based on their name.
 */
public class RequestNameCondition extends Condition
{
    /**
     * Constructor.
     *
     * @param regex
     *            the regular expression to identify matching requests
     * @param exclude
     *            whether or not this is an exclusion rule
     */
    public RequestNameCondition(final String regex)
    {
        super(regex, 600);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected CharSequence getText(final RequestData requestData)
    {
        return requestData.getName();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected String getTypeCode()
    {
        return "n";
    }
    
    /**
     * Returns the right condition
     * 
     * @param regex the regular expression to identify matching requests
     * 
     * @return the condition
     */
    public static Condition build(final String regex)
    {
        if (!StringUtils.isBlank(regex)) 
        {
            return new RequestNameCondition(regex);
        }
        else
        {
            return new RequestNameEmptyCondition();
        }
    }
}
