/*
 * Copyright (c) 2005-2026 Xceptance Software Technologies GmbH
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
 * Holds a chunk of result lines together with meta and processing information. Both types of info
 * are needed by the parser threads.
 * <p>
 * Each chunk carries raw {@code byte[]} lines read from a single timer CSV file, along with the
 * contextual metadata (agent name, test case, user number) needed for downstream processing.
 */
public class DataChunk
{
    /**
     * The data structure to collect the start time and name of an action. Needed to rename client
     * performance timer data.
     */
    private final ConcurrentSkipListMap<Long, String> actionNames;

    /**
     * Indicates whether this chunk contains client performance timer data that needs to be renamed
     * according to the previously collected action names.
     */
    private final boolean adjustTimerNames;

    /** The name of the agent the test user was run on. */
    private final String agentName;

    /** The 1-based line number of the first line in this chunk within the source file. */
    private final int baseLineNumber;

    /**
     * Indicates whether this chunk contains actions that should be remembered for later processing.
     * Is true only if the lines are regular timer data and there is indeed client performance timer
     * data waiting to be processed.
     */
    private final boolean collectActionNames;

    /** The source file this chunk was read from. */
    private final FileObject file;

    /**
     * The raw byte lines of this chunk. Each element is a single CSV line as read by
     * {@link com.xceptance.common.io.ByteBufferedLineReader}.
     */
    private final List<byte[]> byteLines;

    /** The name of the test case the test user was executing. */
    private final String testCaseName;

    /** The instance number of the test user. */
    private final String userNumber;

    /**
     * Creates a new DataChunk carrying raw byte lines from a timer CSV file.
     *
     * @param byteLines
     *            the raw byte lines
     * @param baseLineNumber
     *            the 1-based line number of the first line in the source file
     * @param file
     *            the source file
     * @param agentName
     *            the agent name
     * @param testCaseName
     *            the test case name
     * @param userNumber
     *            the user instance number
     * @param collectActionNames
     *            whether to collect action names for client performance timer renaming
     * @param adjustTimerNames
     *            whether to adjust timer names based on previously collected action names
     * @param actionNames
     *            shared map for action name collection/lookup
     */
    public DataChunk(final List<byte[]> byteLines, final int unused, final int baseLineNumber, final FileObject file,
                     final String agentName, final String testCaseName, final String userNumber,
                     final boolean collectActionNames, final boolean adjustTimerNames,
                     final ConcurrentSkipListMap<Long, String> actionNames)
    {
        this.byteLines = byteLines;
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

    public String getTestCaseName()
    {
        return testCaseName;
    }

    public String getUserNumber()
    {
        return userNumber;
    }

    public List<byte[]> getByteLines()
    {
        return byteLines;
    }
}
