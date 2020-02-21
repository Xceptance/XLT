package com.xceptance.xlt.agent;

import com.xceptance.common.util.ProcessExitCodes;

/**
 * The exit codes used by an agent process.
 */
public interface AgentExitCodes extends ProcessExitCodes
{
    /**
     * Indicates that the agent exited prematurely because the transaction error limit was reached.
     */
    public static final int TOO_MANY_TRANSACTION_ERRORS = 3;
}
