package com.xceptance.xlt.engine.httprequest;

import com.xceptance.xlt.api.actions.AbstractWebAction;
import com.xceptance.xlt.engine.SessionImpl;

/**
 * The base class for actions that use {@link HttpRequest} and {@link HttpResponse}.
 */
public abstract class AbstractHttpAction extends AbstractWebAction
{
    /**
     * Creates a new {@link AbstractHttpAction} object and gives it the passed timer name. This constructor is typically
     * used for an intermediate action in a sequence of actions, i.e. it has a previous action.
     *
     * @param previousAction
     *            the action that preceded the current action
     * @param timerName
     *            the name of the timer that is associated with this action
     */
    public AbstractHttpAction(final AbstractWebAction previousAction, final String timerName)
    {
        super(previousAction, timerName);

        // pass the current WebClient to HttpRequest
        HttpRequest.setDefaultWebClient(getWebClient());
    }

    /**
     * Creates a new {@link AbstractHttpAction} object and gives it a default timer name, which is the simple name of
     * this class. This constructor is typically used for an intermediate action in a sequence of actions, i.e. it has a
     * previous action.
     *
     * @param previousAction
     *            the action that preceded the current action
     */
    public AbstractHttpAction(final AbstractWebAction previousAction)
    {
        this(previousAction, null);
    }

    /**
     * Creates a new {@link AbstractHttpAction} object and gives it the passed timer name. This constructor is typically
     * used for the first action in a sequence of actions, i.e. it has no previous action.
     *
     * @param timerName
     *            the name of the timer that is associated with this action
     */
    public AbstractHttpAction(final String timerName)
    {
        this(null, timerName);
    }

    /**
     * Creates a new {@link AbstractHttpAction} object and gives it a default timer name, which is the simple name of
     * this class. This constructor is typically used for the first action in a sequence of actions, i.e. it has no
     * previous action.
     */
    public AbstractHttpAction()
    {
        this(null, null);
    }

    /**
     * {@inheritDoc}
     * <p>
     * This is actually a no-op. Override if necessary.
     */
    @Override
    public void preValidate() throws Exception
    {
        // empty
    }

    /**
     * {@inheritDoc}
     * <p>
     * This is actually a no-op. Override if necessary.
     */
    @Override
    protected void postValidate() throws Exception
    {
        // empty
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void run() throws Throwable
    {
        try
        {
            super.run();
        }
        finally
        {
            // add an empty "page" as the result of this action
            SessionImpl.getCurrent().getRequestHistory().add(getTimerName());
        }
    }
}
