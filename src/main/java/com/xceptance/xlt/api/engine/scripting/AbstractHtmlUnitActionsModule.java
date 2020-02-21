package com.xceptance.xlt.api.engine.scripting;

import com.xceptance.xlt.api.actions.AbstractHtmlPageAction;
import com.xceptance.xlt.engine.scripting.TestContext;
import com.xceptance.xlt.engine.scripting.util.AbstractCommandAdapter;

/**
 * Base class of all flow modules.
 * 
 * @author Hartmut Arlt (Xceptance Software Technologies GmbH)
 */
public abstract class AbstractHtmlUnitActionsModule extends AbstractHtmlUnitScriptModule
{

    /**
     * Executes the flow steps.
     * 
     * @param prevAction
     *            the action to start from
     * @return last performed action
     */
    protected abstract AbstractHtmlPageAction execute(final AbstractHtmlPageAction prevAction) throws Throwable;

    /**
     * Runs the flow.
     * 
     * @param prevAction
     *            the action to start from
     * @return last performed action
     */
    public AbstractHtmlPageAction run(final AbstractHtmlPageAction prevAction) throws Throwable
    {
        TestContext.getCurrent().pushScope(this);
        final String name = getClass().getCanonicalName();
        try
        {
            AbstractCommandAdapter.LOGGER.info("Calling module: " + name);
            return execute(prevAction);
        }
        finally
        {
            AbstractCommandAdapter.LOGGER.info("Returned from module: " + name);
            TestContext.getCurrent().popScope();
        }
    }
}
