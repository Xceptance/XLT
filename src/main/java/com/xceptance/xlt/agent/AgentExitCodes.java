/*
 * Copyright (c) 2005-2025 Xceptance Software Technologies GmbH
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
