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
