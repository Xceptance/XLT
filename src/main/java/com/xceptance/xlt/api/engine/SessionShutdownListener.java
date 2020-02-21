package com.xceptance.xlt.api.engine;

/**
 * <p>
 * By implementing the {@link SessionShutdownListener} interface custom code gets the chance to be notified by the
 * framework when the current session is about to be terminated (cleared). This is usually the point in time when one
 * run of a test case has been finished. Now any resources held by custom code should be released.
 * </p>
 * <p>
 * In order to be called, the listener must be registered via
 * {@link Session#addShutdownListener(SessionShutdownListener)}. Note that all registered shutdown listeners are cleared
 * when the session is cleared, so the listener must be re-registered with every new test case session.
 * </p>
 * 
 * @author Jörg Werner (Xceptance Software Technologies GmbH)
 */
public interface SessionShutdownListener
{
    /**
     * Called from the framework when the session for the current thread is to be cleared.
     */
    public void shutdown();
}
