/*
 * Copyright (c) 2005-2020 Xceptance Software Technologies GmbH
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
package com.xceptance.xlt.report;

import java.util.List;
import java.util.concurrent.ConcurrentSkipListMap;

import org.apache.commons.vfs2.FileObject;

/**
 * Holds a chunk of result lines together with meta and processing information. Both types of info is needed by the
 * parser threads.
 */
public class LineChunk
{
    /**
     * The data structure to collect the start time and name of an action. Needed to rename client performance timer
     * data.
     */
    private final ConcurrentSkipListMap<Long, String> actionNames;

    /**
     * Indicates whether this chunk contains client performance timer data that needs to be renamed according to the
     * previously collected action names.
     */
    private final boolean adjustTimerNames;

    private final String agentName;

    private final int baseLineNumber;

    /**
     * Indicates whether this chunk contains actions that should be remembered for later processing. Is true only if the
     * lines are regular timer data and there is indeed client performance timer data waiting to be processed.
     */
    private final boolean collectActionNames;

    private final FileObject file;

    private final List<String> lines;

    private final String testCaseName;

    private final String userNumber;

    public LineChunk(final List<String> lines, final int baseLineNumber, final FileObject file, final String agentName,
                     final String testCaseName, final String userNumber, final boolean collectActionNames, final boolean adjustTimerNames,
                     final ConcurrentSkipListMap<Long, String> actionNames)
    {
        this.lines = lines;
        this.baseLineNumber = baseLineNumber;
        this.file = file;
        this.agentName = agentName;
        this.testCaseName = testCaseName;
        this.userNumber = userNumber;
        this.collectActionNames = collectActionNames;
        this.adjustTimerNames = adjustTimerNames;
        this.actionNames = actionNames;
    }

    public ConcurrentSkipListMap<Long, String> getActionNames()
    {
        return actionNames;
    }

    public boolean getAdjustTimerNames()
    {
        return adjustTimerNames;
    }

    public String getAgentName()
    {
        return agentName;
    }

    public int getBaseLineNumber()
    {
        return baseLineNumber;
    }

    public boolean getCollectActionNames()
    {
        return collectActionNames;
    }

    public FileObject getFile()
    {
        return file;
    }

    public List<String> getLines()
    {
        return lines;
    }

    public String getTestCaseName()
    {
        return testCaseName;
    }

    public String getUserNumber()
    {
        return userNumber;
    }
}
