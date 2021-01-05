/*
 * Copyright (c) 2005-2021 Xceptance Software Technologies GmbH
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
package com.xceptance.xlt.performance.actions;

import com.xceptance.common.util.RegExUtils;
import com.xceptance.xlt.api.actions.AbstractLightWeightPageAction;
import com.xceptance.xlt.performance.util.ParameterUtils;

/**
 * This is a simple test class for pulling urls. Fully configurable
 * using properties.
 *
 * @author  Rene Schwietzke
 * 
 */
public class LWSimpleURL extends AbstractLightWeightPageAction
{
    private final String url;
    private final String regexp;
    
    /**
     * @param previousAction
     * @param timerName
     */
    public LWSimpleURL(final String timerName, final String url, final String regexp)
    {
        super(timerName);

        this.url = url;
        this.regexp = regexp;
    }

    /**
     * @param previousAction
     * @param timerName
     */
    public LWSimpleURL(AbstractLightWeightPageAction prevAction, final String timerName, final String url, final String regexp)
    {
        super(prevAction, timerName);

        this.url = url;
        this.regexp = regexp;
    }
    
    /* (non-Javadoc)
     * @see com.xceptance.xlt.api.actions.AbstractAction#preValidate()
     */
    @Override
    public void preValidate() throws Exception
    {
    }

    /* (non-Javadoc)
     * @see com.xceptance.xlt.api.actions.AbstractAction#execute()
     */
    @Override
    protected void execute() throws Exception
    {
        // replace a random value position if needed
    	loadPage(ParameterUtils.replaceDynamicParameters(url));
    }

    /* (non-Javadoc)
     * @see com.xceptance.xlt.api.actions.AbstractAction#postValidate()
     */
    @Override
    protected void postValidate() throws Exception
    {
        // validate response code
        final String page = getContent();

        RegExUtils.getMatchingCount(page, regexp);
    }

}
