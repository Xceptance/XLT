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
 * We filter just by a contains
 */
public class UrlConditionText extends Condition
{
    private final String text;
    
    /**
     * Constructor.
     *
     * @param text
     *            the text to check first before we executed the expensive regex match
     */
    public UrlConditionText(final String text)
    {
        // Contains does not cache, so we just setup decoration here
        super("", 4);

        this.text = text;
    }

    @Override
    protected boolean apply(final RequestData requestData)
    {
        // do a simple lookup
        final int pos = requestData.getOriginalUrl().indexOf(this.text);
        
        // if we have a match, check the regex based condition, otherwise return false
        // and abort here
        return pos >= 0 ? true : false;
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
     * We are not empty despite being without a regex
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
        sb.append("text: '").append(text);
        sb.append("'}");

        return sb.toString();
    }

}
