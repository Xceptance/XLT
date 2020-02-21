package com.xceptance.xlt.api.engine;

import java.util.Map;

import com.xceptance.xlt.api.actions.AbstractAction;
import com.xceptance.xlt.engine.SessionImpl;

/**
 * The {@link Session} object is the runtime context during one run of a certain test case.
 * <p>
 * Multiple threads running the same test case will get different sessions. A session is the anchor that holds the data
 * recorded during that very test run.
 * </p>
 * <p style="color:red">
 * ATTENTION: A session can be reused across different test runs if and only if it is cleared before its reuse.
 * </p>
 * 
 * @author Jörg Werner (Xceptance Software Technologies GmbH)
 */
public abstract class Session
{
    /**
     * Returns the thread-specific Session instance. If no such instance exists yet, it will be created.
     * 
     * @return the Session instance for the current thread
     */
    public static Session getCurrent()
    {
        return SessionImpl.getCurrent();
    }

    /**
     * Logs an event.
     * 
     * @param eventName
     *            name of the event
     * @param eventMessage
     *            the event message
     */
    public static final void logEvent(final String eventName, final String eventMessage)
    {
        getCurrent().getDataManager().logEvent(eventName, eventMessage);
    }

    /**
     * Registers the passed shutdown listener to be called on session termination.
     * 
     * @param listener
     *            the shutdown listener
     */
    public abstract void addShutdownListener(SessionShutdownListener listener);

    /**
     * Clears the session. All state is removed. This includes the request history as well.
     */
    public abstract void clear();

    /**
     * Returns the number of the currently running test user. This value ranges from 0...(n-1), where n denotes the
     * total number of configured test users, independent of their respective user type.
     * 
     * @return the test user's absolute instance number
     */
    public abstract int getAbsoluteUserNumber();

    /**
     * Returns the ID of the current agent.
     * 
     * @return the agent's ID
     */
    public abstract String getAgentID();

    /**
     * Returns the number (or index) of the current agent. This value ranges from 0...(n-1), where n denotes the total
     * number of configured agents.
     * 
     * @return the agent's instance number
     */
    public abstract int getAgentNumber();

    /**
     * Returns the session's ID.
     * 
     * @return the session ID
     */
    public abstract String getID();

    /**
     * Returns the session's data manager.
     * 
     * @return the data manager
     */
    public abstract DataManager getDataManager();

    /**
     * Returns the total count of agents that take part in a load test.
     * 
     * @return the total count
     */
    public abstract int getTotalAgentCount();

    /**
     * Returns the total count of test users running during a test. This includes all users of all types.
     * 
     * @return the total count of users
     */
    public abstract int getTotalUserCount();

    /**
     * Returns the total count of the test users with the same type as the current user, for example "35".
     * 
     * @return the total count
     */
    public abstract int getUserCount();

    /**
     * Returns the ID of the currently running test user, for example "TAddToCart-27".
     * 
     * @return the test user's ID
     */
    public abstract String getUserID();

    /**
     * Returns the name of the currently running test user, for example "TAddToCart".
     * 
     * @return the test user's name
     */
    public abstract String getUserName();

    /**
     * Returns the instance number of the currently running test user, for example "27". This value ranges from
     * 0...(n-1), where n denotes the total number of configured test users with the same type as the current test user.
     * 
     * @return the test user's instance number
     */
    public abstract int getUserNumber();

    /**
     * Returns the session's failure status.
     * 
     * @return whether or not the session has failed
     */
    public abstract boolean hasFailed();

    /**
     * Indicates whether the current test session is executed in the context of a functional test or a load test.
     * 
     * @return <code>true</code> if we are in the middle of a load test, <code>false</code> otherwise
     */
    public abstract boolean isLoadTest();

    /**
     * Unregisters the passed shutdown listener.
     * 
     * @param listener
     *            the shutdown listener
     */
    public abstract void removeShutdownListener(SessionShutdownListener listener);

    /**
     * Sets the session's failure status.
     * 
     * @param value
     *            whether or not the session has failed
     */
    public abstract void setFailed(boolean value);

    /**
     * Sets the session's ID.
     * 
     * @param id
     *            the new session ID
     */
    public abstract void setID(String id);

    /**
     * Returns the network data manager.
     * 
     * @return network data manager
     */
    public abstract NetworkDataManager getNetworkDataManager();

    /**
     * @deprecated As of XLT 4.6.0, use {@link #getCurrentActionName()} instead.
     */
    @Deprecated
    public abstract String getWebDriverActionName();

    /**
     * @deprecated As of XLT 4.6.0, use {@link #startAction(String)} instead.
     */
    @Deprecated
    public abstract void setWebDriverActionName(final String webDriverActionName);

    /**
     * Tells the framework to start a new action with the given name. If there is still a pending (i.e. unfinished)
     * action, then this action will be finished before the new action is started.
     * <p>
     * Note that calling this method is not necessary for test cases that automatically manage the action life cycle.
     * This includes test cases that are built with action classes, but also interpreted or exported script test cases.
     * You would need to call this method for plain WebDriver-based or plain HtmlUnit-based test cases, though.
     * 
     * @param actionName
     *            the name of the new action
     * @see #stopAction()
     * @see AbstractAction
     */
    public abstract void startAction(final String actionName);

    /**
     * Tells the framework to finish the current action. If there is no pending action, calling this method has no
     * effect.
     * <p>
     * Finishing an action includes logging the action's run time and result. Whether the action is logged as successful
     * or failed depends on the session's failed state.
     * 
     * @see #setFailed(boolean)
     * @see #startAction(String)
     */
    public abstract void stopAction();

    /**
     * Returns the name of the current action as specified when the action was started. When called between two actions
     * (i.e. after finishing the previous action, but before starting a new one), the returned action name will be
     * <code>null</code>.
     * 
     * @return the name of the current action, or <code>null</code> if there is none
     * @see #startAction(String)
     */
    public abstract String getCurrentActionName();

    /**
     * Returns this session's value log, a storage for session-specific test parameters and result data. Any value you
     * add to this log will later be available in the result browser. Note that the log will be cleared with each new
     * iteration.
     * <p>
     * This feature is intended to aid in error analysis. The data in the result browser may help you to reconstruct and
     * rerun a failed test case iteration without having to dig into log files. Simply add any value of special interest
     * and you will have it at hand in the result browser. This is especially useful if your test case uses random or
     * randomly chosen test parameters.
     * <p>
     * Data is stored as name/value pairs. Even though the log accepts any {@link Object} as the value, the value will
     * later be converted to a string using {@link Object#toString()} for proper display in the result browser. So make
     * sure your value classes implement this method appropriately.
     * 
     * @return the values keyed by their names
     */
    public abstract Map<String, Object> getValueLog();
}
