/*
 * Copyright (c) 2005-2021 Xceptance Software Technologies GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
