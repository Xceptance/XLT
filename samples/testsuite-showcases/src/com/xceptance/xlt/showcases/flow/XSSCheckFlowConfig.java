/**
 * 
 */
package com.xceptance.xlt.showcases.flow;

import java.util.LinkedList;
import java.util.List;

/**
 * Configuration for the {@link XSSCheckFlow}.
 */
public class XSSCheckFlowConfig
{
    /**
     * Attack strings for xss check
     */
    private List<String> xssAttackStrings = new LinkedList<String>();

    /**
     * Runtime of xss check
     */
    private int runtime = 5;

    /**
     * @return the xssAttackStrings
     */
    public List<String> getXssAttackStrings()
    {
        return xssAttackStrings;
    }

    /**
     * @param xssAttackStrings
     *            the xssAttackStrings to set
     */
    public void setXssAttackStrings(final List<String> xssAttackStrings)
    {
        this.xssAttackStrings = xssAttackStrings;
    }

    /**
     * @return the runtime
     */
    public int getRuntime()
    {
        return runtime;
    }

    /**
     * @param runtime
     *            the runtime to set
     */
    public void setRuntime(final int runtime)
    {
        this.runtime = runtime;
    }
}
