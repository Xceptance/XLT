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
package com.xceptance.xlt.report.mergerules;

import org.apache.commons.lang3.StringUtils;

import com.xceptance.xlt.api.engine.RequestData;
import com.xceptance.xlt.report.mergerules.RequestProcessingRule.UrlPrecheckText;

/**
 * Filters requests based on their URLs.
 */
public class UrlRequestFilter extends AbstractPatternRequestFilter
{
    private final String urlPrecheckText;
    
    /**
     * Constructor.
     *
     * @param regex
     *            the regular expression to identify matching requests
     */
    public UrlRequestFilter(final String regex, final UrlPrecheckText urlPrecheckText)
    {
        this(regex, false, urlPrecheckText);
    }

    /**
     * Constructor.
     *
     * @param regex
     *            the regular expression to identify matching requests
     * @param exclude
     *            whether or not this is an exclusion rule
     */
    public UrlRequestFilter(final String regex, final boolean exclude, final UrlPrecheckText urlPrecheckText)
    {
        // with the change to get split caches per thread, we can afford to do
        // a lookup here and also profit from "does not apply" look ups.
        super("u", regex, exclude, 5000);

        if (StringUtils.isNotBlank(urlPrecheckText.value()))
        {
            this.urlPrecheckText = urlPrecheckText.value();
        }
        else
        {
            this.urlPrecheckText = null;
        }
    }

    @Override
    public boolean appliesTo(final RequestData requestData)
    {
        // should we run a cheapish initial check first?
        if (this.urlPrecheckText != null)
        {
            // do a simple lookup
            final int pos = requestData.getOriginalUrl().indexOf(urlPrecheckText);
            
            // if we have a match
            if (pos >= 0)
            {
                // do the real check to be sure
                return super.appliesTo(requestData);
            }
            else
            {
                // precheck failed so this is a non-match, turn it for exclude rules
                return false ^ this.isExclude;
            }
        }           
        else
        {
            return super.appliesTo(requestData);
        }
        
//        // should we run a cheapish initial check first?
//        if (this.urlPrecheckText != null && !this.isExclude)
//        {
//            // do a simple lookup
//            final var r = XltCharBuffer.contains(requestData.getUrl(), urlPrecheckText, urlPrecheckTextShiftTable);
//            
//            // if we have a match
//            if (r == true)
//            {
//                // do the real check to be sure and get us the data
//                return super.appliesTo(requestData);
//            }
//            else
//            {
//                // precheck failed and we are not excluding, so this is a non-match
//                return false;
//            }
//        }           
//        else
//        {
//            return super.appliesTo(requestData);
//        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected CharSequence getText(final RequestData requestData)
    {
        return requestData.getUrl();
    }
}
