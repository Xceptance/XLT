package com.xceptance.xlt.engine;

import net.sourceforge.htmlunit.corejs.javascript.debug.Debugger;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.javascript.DebugFrameImpl;
import com.gargoylesoftware.htmlunit.javascript.DebuggerImpl;
import com.gargoylesoftware.htmlunit.javascript.JavaScriptEngine;

/**
 * An implementation of Rhino's {@link Debugger} interface which can attach itself to a {@link WebClient} object to
 * receive JS debug events.
 */
public class XltDebugger extends DebuggerImpl
{
    /**
     * The logger to which debug messages will be printed.
     */
    private static final Logger debugFrameLogger = Logger.getLogger(DebugFrameImpl.class);

    /**
     * The original log level. Used to restore the log level.
     */
    // private static final Level defaultDebugFrameLogLevel = debugFrameLogger.getLevel();

    /**
     * The web client to which to attach the debugger if enabled.
     */
    private final WebClient webClient;

    /**
     * Whether the debugger is enabled.
     */
    private boolean enabled;

    public XltDebugger(final WebClient webClient)
    {
        this.webClient = webClient;
    }

    public boolean isEnabled()
    {
        return enabled;
    }

    public void setEnabled(final boolean enabled)
    {
        this.enabled = enabled;

        if (enabled)
        {
            // register this debugger with the web client
            ((JavaScriptEngine) webClient.getJavaScriptEngine()).getContextFactory().setDebugger(this);

            // switch the logger to level TRACE, otherwise we won't see anything
            debugFrameLogger.setLevel(Level.TRACE);
        }
        else
        {
            // unbind this debugger from the web client
            ((JavaScriptEngine) webClient.getJavaScriptEngine()).getContextFactory().setDebugger(null);

            // don't reset the log level as this might shut up the debugger of parallel users
            // debugFrameLogger.setLevel(defaultDebugFrameLogLevel);
        }
    }
}
