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
package com.xceptance.xlt.performance.tests;

import org.junit.Test;

import com.xceptance.xlt.api.actions.AbstractLightWeightPageAction;
import com.xceptance.xlt.api.tests.AbstractTestCase;
import com.xceptance.xlt.performance.actions.LWSimpleURL;

/**
 * Just runs a single url test. Configurable
 *
 * @author  Rene Schwietzke
 * 
 */
public class TLWSimpleURL extends AbstractTestCase
{
    @Test
    public void test() throws Throwable
    {
        final int iterations	= getProperty("iterations", 10);
        final String url		= getProperty("url");
        final String regexp		= getProperty("regexp");
        final String timerName1	= getProperty("timerName", "LWSimpleUrl").concat(".1");
        final String timerName2 = getProperty("timerName", "LWSimpleUrl").concat(".2");
        
        AbstractLightWeightPageAction lastAction = new LWSimpleURL(url, timerName1, regexp);
        lastAction.run();
        
        for (int i = 0; i < iterations - 1; i++)
        {
            lastAction = new LWSimpleURL(lastAction, timerName2, url, regexp);
            lastAction.run();            
        }
            
    }
}
