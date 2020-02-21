package com.xceptance.xlt.mastercontroller;

import com.xceptance.xlt.agentcontroller.TestUserStatus;

/**
 * Represents the status of all test users of the same type.
 * 
 * @author Jörg Werner (Xceptance Software Technologies GmbH)
 */
public class TestUserTypeStatus extends TestUserStatus
{
    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = -2846660875719786673L;

    private int runningUsers;

    private int totalUsers;

    /**
     * Returns the value of the 'runningUsers' attribute.
     * 
     * @return the value of runningUsers
     */
    public int getRunningUsers()
    {
        return runningUsers;
    }

    /**
     * Returns the value of the 'totalUsers' attribute.
     * 
     * @return the value of totalUsers
     */
    public int getTotalUsers()
    {
        return totalUsers;
    }

    /**
     * Sets the new value of the 'runningUsers' attribute.
     * 
     * @param runningUsers
     *            the new runningUsers value
     */
    public void setRunningUsers(final int runningUsers)
    {
        this.runningUsers = runningUsers;
    }

    /**
     * Sets the new value of the 'totalUsers' attribute.
     * 
     * @param totalUsers
     *            the new totalUsers value
     */
    public void setTotalUsers(final int totalUsers)
    {
        this.totalUsers = totalUsers;
    }
}
