package com.xceptance.xlt.report.scorecard;

import java.util.HashMap;
import java.util.Map;

import com.xceptance.xlt.api.util.XltProperties;

/**
 * Context helper for Groovy scripts.
 */
public class ScriptContext
{
    private final XltProperties serviceProps = XltProperties.getInstance();

    private final Map<String, Object> reportProps;

    public ScriptContext(Map<String, Object> reportProps)
    {
        this.reportProps = reportProps != null ? reportProps : new HashMap<>();
    }

    /**
     * Access to XLT properties.
     */
    public XltProperties getXltProperties()
    {
        return serviceProps;
    }

    /**
     * Helper to get a property with default.
     */
    public String getProperty(String key, String defaultValue)
    {
        return serviceProps.getProperty(key, defaultValue);
    }

    /**
     * Access to report properties/context values.
     */
    public Object get(String key)
    {
        return reportProps.get(key);
    }
}
