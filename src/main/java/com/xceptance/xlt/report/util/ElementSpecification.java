package com.xceptance.xlt.report.util;

/**
 *
 */
public class ElementSpecification
{
    public String idElementTagName;

    public String rootElementXpath;

    /**
     * Constructor.
     * 
     * @param rootElementXpath
     * @param idElementTagName
     */
    public ElementSpecification(final String rootElementXpath, final String idElementTagName)
    {
        this.rootElementXpath = rootElementXpath;
        this.idElementTagName = idElementTagName;
    }
}
