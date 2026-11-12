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
package com.xceptance.xlt.report.mergerules.url;

import org.apache.commons.lang3.StringUtils;

import com.xceptance.common.util.RegExUtils;
import com.xceptance.xlt.api.engine.RequestData;
import com.xceptance.xlt.report.mergerules.Condition;

/**
 * Filters requests based on their URLs.
 * 
 * @author Jörg Werner (Xceptance Software Technologies GmbH)
 * @author Rene Schwietzke (Xceptance Software Technologies GmbH)
 */
public class UrlCondition extends Condition
{
    /**
     * Constructor.
     *
     * @param regex
     *            the regular expression to identify matching requests
     * @param useFastRegex
     *            whether to use FastRegex (true) or standard JDK Pattern (false)
     */
    public UrlCondition(final String regex, final boolean useFastRegex)
    {
        // with the change to get split caches per thread, we can afford to do
        // a lookup here and also profit from "does not apply" look ups.
        super(regex, 5000, useFastRegex);
    }

    /**
     * Constructor delegating to global {@link RegExUtils#isFastRegexEnabled()}.
     *
     * @param regex
     *            the regular expression to identify matching requests
     */
    public UrlCondition(final String regex)
    {
        super(regex, 5000);
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
     * {@inheritDoc}
     */
    @Override
    protected String getTypeCode()
    {
        return "u";
    }
    
    /**
     * Returns the right condition
     * 
     * @param regex the regular expression to identify matching requests
     * @param text the text to check first before we executed the expensive regex match, if any regex was given
     * @param useFastRegex whether to use FastRegex (true) or standard JDK Pattern (false)
     * 
     * @return the condition that checks urls
     */
    public static Condition build(final String regex, final String text, final boolean useFastRegex)
    {
        if (!StringUtils.isBlank(text) && !StringUtils.isBlank(regex)) 
        {
            return new UrlConditionAndText(regex, text, useFastRegex);
        }
        else if (!StringUtils.isBlank(text))
        {
            return new UrlConditionText(text);
        }
        else if (!StringUtils.isBlank(regex))
        {
            return new UrlCondition(regex, useFastRegex);
        }
        else
        {
            return new UrlEmptyCondition();
        }
    }

    /**
     * Returns the right condition delegating to global {@link RegExUtils#isFastRegexEnabled()}.
     * 
     * @param regex the regular expression to identify matching requests
     * @param text the text to check first before we executed the expensive regex match, if any regex was given
     * 
     * @return the condition that checks urls
     */
    public static Condition build(final String regex, final String text)
    {
        return build(regex, text, RegExUtils.isFastRegexEnabled());
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
