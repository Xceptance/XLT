package com.xceptance.xlt.report.scorecard.builder;

import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class MetricsHelperTest
{
    private final MetricsHelper metrics = new MetricsHelper();

    @Test
    public void testGlobalCountPerHour()
    {
        Assert.assertEquals("/testreport/summary/transactions/countPerHour", metrics.globalCountPerHour("transactions"));
        Assert.assertEquals("/testreport/summary/requests/countPerHour", metrics.globalCountPerHour("requests"));
    }

    @Test
    public void testHttpErrorCount()
    {
        // 5.. regex against responseCode/code
        Assert.assertEquals(
            "sum(//responseCodes/responseCode[matches(code, '^5..$')]/count)",
            metrics.httpErrorCount("5..")
        );
        
        // Exact 404 or 400
        Assert.assertEquals(
            "sum(//responseCodes/responseCode[matches(code, '^404|400$')]/count)",
            metrics.httpErrorCount("404|400")
        );
    }

    @Test
    public void testPerHour()
    {
        Assert.assertEquals(
            "((sum(//some/path)) div (number(/testreport/general/duration) div 3600))",
            metrics.perHour("sum(//some/path)")
        );
    }

    @Test
    public void testRequestP95WithExcludeName()
    {
        Map<String, Object> args = new HashMap<>();
        args.put("excludeName", "^OrderSubmit");
        
        Assert.assertEquals(
            "max(//requests/request[not(matches(name, '^OrderSubmit'))]/percentiles/p95)",
            metrics.requestP95(args)
        );
    }

    @Test
    public void testRequestP95WithExcludeLabel()
    {
        Map<String, Object> args = new HashMap<>();
        args.put("excludeLabel", "cached");
        
        Assert.assertEquals(
            "max(//requests/request[labels != 'cached']/percentiles/p95)",
            metrics.requestP95(args)
        );
    }

    @Test
    public void testRequestP95Combined()
    {
        Map<String, Object> args = new HashMap<>();
        args.put("name", "^Homepage");
        args.put("excludeName", "^Homepage_Static");
        args.put("label", "critical");
        args.put("excludeLabel", "cached");
        
        // Note: Map keys are processed in a specific order in aggregateValue:
        // name, excludeName, label, excludeLabel
        Assert.assertEquals(
            "max(//requests/request[matches(name, '^Homepage') and not(matches(name, '^Homepage_Static')) and labels = 'critical' and labels != 'cached']/percentiles/p95)",
            metrics.requestP95(args)
        );
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEmptyArgsThrowsException()
    {
        metrics.requestP95(new HashMap<>());
    }

    @Test
    public void testLegacyStringMethodsViaReflection() throws Exception
    {
        // To achieve high coverage without massive boilerplate, we dynamically invoke 
        // all methods that take a single String regex (e.g., requestP95(String), actionMean(String)).
        java.lang.reflect.Method[] methods = MetricsHelper.class.getDeclaredMethods();
        for (java.lang.reflect.Method method : methods)
        {
            String name = method.getName();
            // Match methods like requestP50, transactionMean, actionErrors, customTimerMax
            if ((name.startsWith("request") || name.startsWith("transaction") || 
                 name.startsWith("action") || name.startsWith("customTimer")) &&
                 method.getParameterCount() == 1 && 
                 method.getParameterTypes()[0] == String.class)
            {
                // We just want to ensure it successfully generates an XPath without crashing.
                String result = (String) method.invoke(metrics, "^MyRegex$");
                
                // Verify basic structure of the output
                Assert.assertTrue(result.startsWith("max(//"));
                Assert.assertTrue(result.contains("matches(name, '^MyRegex$')"));
            }
        }
    }
}
