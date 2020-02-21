package com.xceptance.xlt.agentcontroller;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.NameFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.xceptance.common.io.FileUtils;
import com.xceptance.common.util.zip.ZipUtils;
import com.xceptance.xlt.agent.AgentInfo;
import com.xceptance.xlt.util.FileReplicationIndex;
import com.xceptance.xlt.util.FileReplicationUtils;

/**
 * The AgentManagerImpl class is the server-side implementation of the AgentManager interface, i.e. it runs on the agent
 * controller.
 * 
 * @author Jörg Werner (Xceptance Software Technologies GmbH)
 */
public class AgentManagerImpl implements AgentManager, AgentListener
{
    /**
     * Logger
     */
    private static final Log log = LogFactory.getLog(AgentManagerImpl.class);

    /**
     * A file filter that ignores result browser directories (directories named "output").
     */
    private static final IOFileFilter NO_RESULTBROWSER_FILTER = FileFilterUtils.notFileFilter(FileFilterUtils.makeDirectoryOnly(new NameFileFilter(
                                                                                                                                                   "output")));

    /**
     * A file filter that ignores agent log files.
     */
    private static final IOFileFilter NO_AGENTLOG_FILTER = FileFilterUtils.notFileFilter(FileFilterUtils.makeFileOnly(new WildcardFileFilter(
                                                                                                                                             "agent*.log*")));

    /**
     * A file filter that ignores both agent log files and result browser directories.
     */
    private static final IOFileFilter NO_AGENTLOG_NO_RESULTBROWSER_FILTER = FileFilterUtils.and(NO_RESULTBROWSER_FILTER, NO_AGENTLOG_FILTER);

    /**
     * agent
     */
    private AgentImpl agent;

    /**
     * agent information object
     */
    private final AgentInfo agentInfo;

    /**
     * command line
     */
    private final String[] commandLine;

    private final AgentListener agentListener;

    private final AtomicInteger unexpectedAgentExitCode = new AtomicInteger(0);

    /**
     * Creates and initializes agent manager.
     * 
     * @param agentInfo
     *            agent information object
     * @param commandLine
     *            command line
     */
    public AgentManagerImpl(final AgentInfo agentInfo, final String[] commandLine, final AgentListener agentListener)
    {
        this.agentInfo = agentInfo;
        this.commandLine = commandLine;
        this.agentListener = agentListener;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setupEnvironment() throws IOException
    {
        org.apache.commons.io.FileUtils.forceMkdir(agentInfo.getAgentDirectory());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AgentInfo getAgentInfo()
    {
        return agentInfo;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AgentImpl getAgent()
    {
        return agent;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String[] getCommandLine()
    {
        return commandLine;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void startAgent(final List<TestUserConfiguration> loadProfile) throws Exception
    {
        // reset exit code
        unexpectedAgentExitCode.set(0);
        agent = new AgentImpl(getAgentInfo().getAgentID(), getCommandLine(), getAgentInfo().getResultsDirectory(), loadProfile, this,
                              getAgentInfo().getAgentDirectory());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean archiveAgentResults(final TestResultAmount testResultAmount, final File zipFile)
    {
        return archiveAgentResults(getAgentInfo().getResultsDirectory(), testResultAmount, zipFile, agentInfo.getAgentID());
    }

    /**
     * archives the files in given directory
     * 
     * @param directory
     *            directory to archive
     * @param testResultAmount
     *            what amount to archive
     * @param zipFile
     *            archive name
     * @param marker
     *            just for log entries
     * @return <code>true</code> if archiving was successful, <code>false</code> otherwise
     */
    public static boolean archiveAgentResults(final File directory, final TestResultAmount testResultAmount, final File zipFile,
                                              final String marker)
    {
        boolean wasSuccessful = false;
        try
        {
            log.info(marker + ": Prepare zipping");

            // choose a file filter depending on the download mode
            final IOFileFilter fileFilter;
            switch (testResultAmount)
            {
                case MEASUREMENTS_AND_RESULTBROWSER:
                    fileFilter = NO_AGENTLOG_FILTER;
                    break;
                case MEASUREMENTS_ONLY:
                    fileFilter = NO_AGENTLOG_NO_RESULTBROWSER_FILTER;
                    break;
                default:
                    fileFilter = null;
                    break;
            }

            if (directory.exists())
            {
                log.debug(marker + ": Zip agent results '" + directory + "' to '" + zipFile + "' ...");
                ZipUtils.zipDirectory(directory, fileFilter, zipFile);
                log.debug(marker + ": Zip finished.");

                wasSuccessful = true;
            }
        }
        catch (final Exception ex)
        {
            throw new RuntimeException(ex);
        }

        return wasSuccessful;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FileReplicationIndex getAgentFilesIndex()
    {
        // report all files except svn files
        if (getAgentInfo().getAgentDirectory().exists())
        {
            return FileReplicationUtils.getIndex(getAgentInfo().getAgentDirectory(), FileFilterUtils.makeSVNAware(null));
        }
        else
        {
            return null;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeResultsDirectory()
    {
        try
        {
            final File agentResultsDir = getAgentInfo().getResultsDirectory();
            // remove the results directory before
            if (agentResultsDir.exists())
            {
                if (!FileUtils.deleteDirectoryRelaxed(agentResultsDir))
                {
                    // directory is not deleted
                    log.warn(agentInfo.getAgentID() + ": Unable to remove " + agentResultsDir.getAbsoluteFile());
                }
            }
        }
        catch (final Exception ex)
        {
            throw new RuntimeException(ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setAgentStatus(final AgentStatus status)
    {
        final Agent agent = getAgent();
        if (agent != null)
        {
            agent.setStatus(status);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isAgentRunning()
    {
        return agent != null ? agent.isRunning() : false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<TestUserConfiguration> getAgentLoadProfile()
    {
        return agent != null ? agent.getLoadProfile() : null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AgentStatus getAgentStatus()
    {
        AgentStatus agentStatus = agent != null ? agent.getStatus() : null;

        final int unexpectedExitCode = unexpectedAgentExitCode.get();
        if (unexpectedExitCode != 0)
        {
            if (agentStatus == null)
            {
                agentStatus = new AgentStatus();
                agentStatus.setAgentID(getAgentInfo().getAgentID());
                agentStatus.setHostName(commandLine[3]);
            }

            agentStatus.setErrorExitCode(unexpectedExitCode);
        }

        return agentStatus;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void resetAgentStatus()
    {
        if (agent != null && !agent.isRunning())
        {
            agent.setStatus(null);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void stopAgent()
    {
        if (isAgentRunning())
        {
            log.debug(agentInfo.getAgentID() + ": Stopping ...");
            agent.stop();
            log.debug(agentInfo.getAgentID() + ": Stopped");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateAgentFiles(final File sourceDir)
    {
        try
        {
            // get file indexes
            final FileReplicationIndex srcIndex = FileReplicationUtils.getIndex(sourceDir, FileFilterUtils.makeSVNAware(null));
            final FileReplicationIndex dstIndex = getAgentInfo().getAgentDirectory().exists()
                                                                                             ? FileReplicationUtils.getIndex(getAgentInfo().getAgentDirectory(),
                                                                                                                             FileFilterUtils.makeSVNAware(null))
                                                                                             : new FileReplicationIndex();

            // get the files to be updated or deleted on the target
            final List<File> filesToBeDeleted = new ArrayList<File>();
            final List<File> filesToBeUpdated = new ArrayList<File>();
            FileReplicationUtils.compareIndexes(srcIndex, dstIndex, filesToBeUpdated, filesToBeDeleted);

            // cleanup obsolete files
            log.info(agentInfo.getAgentID() + ": Deleting obsolete agent files");
            for (final File file : filesToBeDeleted)
            {
                final String relativePath = file.getPath().replace('\\', '/');
                final File absoluteFile = new File(getAgentInfo().getAgentDirectory(), relativePath);
                log.debug(agentInfo.getAgentID() + ": Deleting file '" + absoluteFile + "' ...");
                try
                {
                    com.xceptance.common.io.FileUtils.deleteDirectoryRelaxed(absoluteFile);
                }
                catch (final IllegalArgumentException e)
                {
                    log.debug(e);
                }
            }

            // update
            log.info(agentInfo.getAgentID() + ": Installing new and updated files");
            for (final File file : filesToBeUpdated)
            {
                final String relativePath = file.getPath().replace('\\', '/');

                final File srcFile = new File(sourceDir, relativePath);
                final File dstFile = new File(getAgentInfo().getAgentDirectory(), relativePath);

                log.debug(agentInfo.getAgentID() + ": Installing file '" + dstFile + "' ...");
                if (srcFile.isDirectory())
                {
                    org.apache.commons.io.FileUtils.forceMkdir(dstFile);
                }
                else
                {
                    org.apache.commons.io.FileUtils.copyFile(srcFile, dstFile);
                }
            }
        }
        catch (final Exception ex)
        {
            throw new RuntimeException(ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void agentStopped(final String agentID)
    {
        log.debug(agentInfo.getAgentID() + ": Agent stopped.");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void agentExitedUnexpectedly(final String agentID, final int exitCode)
    {
        unexpectedAgentExitCode.set(exitCode);
        log.debug(agentInfo.getAgentID() + ": Agent has unclear exit code: " + exitCode);
        agentListener.agentExitedUnexpectedly(agentID, exitCode);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() throws IOException
    {
        stopAgent();
        FileUtils.deleteDirectoryRelaxed(agentInfo.getAgentDirectory());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setTotalAgentCount(final int totalAgentCount)
    {
        // update agent info
        agentInfo.setTotalAgentCount(totalAgentCount);

        // update command line
        if (commandLine.length > 5)
        {
            commandLine[5] = String.valueOf(totalAgentCount);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setAgentNumber(final int agentNumber)
    {
        // update agent info
        agentInfo.setAgentNumber(agentNumber);

        // update command line
        if (commandLine.length > 4)
        {
            commandLine[4] = String.valueOf(agentNumber);
        }
    }
}
