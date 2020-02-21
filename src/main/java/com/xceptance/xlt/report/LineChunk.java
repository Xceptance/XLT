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
