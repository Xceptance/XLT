package com.xceptance.xlt.engine;

import com.xceptance.xlt.common.XltConstants;

/**
 * (Future) main entry point into the XLT framework.
 */
public class XltEngine
{
    /**
     * The {@link XltEngine} singleton instance.
     */
    private static final XltEngine instance = new XltEngine();

    /**
     * Returns the one and only {@link XltEngine} instance.
     */
    public static XltEngine getInstance()
    {
        return instance;
    }

    /**
     * Whether or not XLT is run in "dev mode".
     */
    private final boolean devMode;

    /**
     * Constructor.
     */
    private XltEngine()
    {
        // TODO: This is rather hack-ish.
        devMode = (System.getenv("XLT_HOME") == null && System.getProperty(XltConstants.XLT_PACKAGE_PATH + ".home") == null);
    }

    /**
     * Returns whether or not XLT is run in "dev mode".
     */
    public boolean isDevMode()
    {
        return devMode;
    }
}
