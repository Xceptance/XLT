/*
 * Copyright (c) 2005-2022 Xceptance Software Technologies GmbH
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

import com.xceptance.xlt.api.actions.AbstractHtmlPageAction;
import com.xceptance.xlt.api.tests.AbstractTestCase;
import com.xceptance.xlt.performance.actions.SimpleURL;

/**
 * Just runs a single url test. Configurable
 *
 * @author  Rene Schwietzke
 * 
 */
public class TSimpleURL extends AbstractTestCase
{
    @Test
    public void test() throws Throwable
    {
        final int iterations	= getProperty("iterations", 10);
        final String url		= getProperty("url");
        final String xpath		= getProperty("xpath");
        final String text		= getProperty("text");
        final String timerName1	= getProperty("timerName", "SimpleUrl").concat(".1");
        final String timerName2 = getProperty("timerName", "SimpleUrl").concat(".2");
        
        AbstractHtmlPageAction lastAction = new SimpleURL(timerName1, url, xpath, text);
        lastAction.run();
        
        for (int i = 0; i < iterations - 1; i++)
        {
            lastAction = new SimpleURL(lastAction, timerName2, url, xpath, text);
            lastAction.run();            
        }
            
    }
}
