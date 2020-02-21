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
