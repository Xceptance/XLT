package com.xceptance.xlt.report.external.config;

import javax.xml.bind.annotation.XmlAttribute;

/**
 * @author matthias.ullrich
 */
public class Property
{
    private String key;

    private String value = null;

    /**
     * property name
     * 
     * @return property name
     */
    @XmlAttribute(name = "key", required = true)
    public String getKey()
    {
        return key;
    }

    /**
     * property value
     * 
     * @return property value
     */
    @XmlAttribute(name = "value", required = true)
    public String getValue()
    {
        return value;
    }

    /*
     * DO NOT REMOVE METHODS BELOW !
     */

    @SuppressWarnings("unused")
    private void setKey(final String key)
    {
        this.key = key;
    }

    @SuppressWarnings("unused")
    private void setValue(final String value)
    {
        this.value = value;
    }
}
