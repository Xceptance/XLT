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
import com.xceptance.xlt.report.mergerules.Condition;

/**
 * Filters requests based on their URLs.
 * 
 * @author Rene Schwietzke (Xceptance Software Technologies GmbH)
 */
public class UrlConditionAndText extends Condition
{
    private final String text;
    
    /**
     * Constructor.
     *
     * @param regex
     *            the regular expression to identify matching requests
     * @param text
     *            the text to check first before we executed the expensive regex match
     */
    public UrlConditionAndText(final String regex, final String text)
    {
        // with the change to get split caches per thread, we can afford to do
        // a lookup here and also profit from "does not apply" look ups.
        super(regex, 5000);

        this.text = text;
    }

    @Override
    protected boolean apply(final RequestData requestData)
    {
        // do a simple lookup on the string using the JDK21 search power
        final int pos = requestData.getOriginalUrl().indexOf(this.text);
        
        // if we have a match, check the regex based condition, otherwise return false
        // and abort here, so this is effectively an AND operation
        return pos >= 0 ? super.apply(requestData) : false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected CharSequence getText(final RequestData requestData)
    {
        return requestData.getUrl();
    }
    
    /**
     * We are not empty by design
     * 
     * @return always false
     */
    @Override
    protected boolean isEmpty()
    {
        return false;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected String getTypeCode()
    {
        return "u";
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
        sb.append(", text: '").append(text);
        sb.append("'}");

        return sb.toString();
    }
}
