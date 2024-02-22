/*
 * Copyright (c) 2005-2024 Xceptance Software Technologies GmbH
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

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadPoolExecutor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xceptance.common.io.FileListFileFilter;
import com.xceptance.common.util.zip.ZipUtils;
import com.xceptance.xlt.agentcontroller.AgentController;
import com.xceptance.xlt.util.AgentControllerException;
import com.xceptance.xlt.util.FailedAgentControllerCollection;
import com.xceptance.xlt.util.FileReplicationIndex;
import com.xceptance.xlt.util.FileReplicationUtils;
import com.xceptance.xlt.util.ProgressBar;

public class AgentControllerUpdate
{
    /**
     * The log facility of this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(AgentControllerUpdate.class);

    /**
     * The task used for polling until upload becomes complete.
     */
    private static final Poll.AgentControllerPollingTask UPLOAD_POLL_TASK = new Poll.AgentControllerPollingTask()
    {
        public boolean call(final AgentController agentController) throws Exception
        {
            if (agentController.isUpdateDone())
            {
                agentController.setUpdateAcknowledged();
                LOG.debug("Update done at " + agentController);
                return true;
            }
            return false;
        }
    };

    private final ThreadPoolExecutor uploadExecutor;

    private final ThreadPoolExecutor downloadExecutor;

    private final Collection<AgentController> agentControllers;

    private final File tempDirectory;

    private final FailedAgentControllerCollection failedAgentControllers;

    private Map<FileReplicationIndex, Set<AgentController>> remoteFileIndexes;

    public AgentControllerUpdate(final Collection<AgentController> agentControllers, final ThreadPoolExecutor uploadExecutor,
                                 final ThreadPoolExecutor downloadExecutor, final File tempDirectory)
    {
        this.agentControllers = agentControllers;
        this.uploadExecutor = uploadExecutor;
        this.downloadExecutor = downloadExecutor;
        this.tempDirectory = tempDirectory;

        failedAgentControllers = new FailedAgentControllerCollection();
    }

    /**
     * Prepare file update.
     * 
     * @param progress
     *            progress bar
     */
    /*
     * Progress count: agentControllers.size() + 1
     */
    public void prepare(final ProgressBar progress) throws AgentControllerException
    {
        remoteFileIndexes = getAgentFileIndexes(progress);
    }

    /**
     * Get file indexes.
     * 
     * @param progress
     *            progress bar
     * @throws AgentControllerException
     *             if one of the following reasons
     *             <ul>
     *             <li>mastercontroller is not in relaxed mode and at least one agent controller did not respond</li>
     *             <li>an exception was thrown at agent site</li>
     *             </ul>
     */
    /*
     * Progress count: agentControllers.size() + 1
     */
    private Map<FileReplicationIndex, Set<AgentController>> getAgentFileIndexes(final ProgressBar progress) throws AgentControllerException
    {
        // get each agent controller's file index
        final Map<AgentController, FileReplicationIndex> remoteFileIndexes = getRemoteFileIndexes(progress);

        // summarize file indexes
        final Map<FileReplicationIndex, Set<AgentController>> fileIndexes = summarizeFileIndexes(remoteFileIndexes, progress);

        return fileIndexes;
    }

    /**
     * Get each agent controller's file index
     * 
     * @param progress
     *            progress bar
     * @return
     */
    /*
     * Progress count : agentControllers.size()
     */
    private Map<AgentController, FileReplicationIndex> getRemoteFileIndexes(final ProgressBar progress)
    {
        LOG.info("Get remote file indexes");
        final Map<AgentController, FileReplicationIndex> remoteFileIndexes = new ConcurrentHashMap<AgentController, FileReplicationIndex>();

        final CountDownLatch latch = new CountDownLatch(agentControllers.size());
        for (final AgentController agentController : agentControllers)
        {
            downloadExecutor.execute(new Runnable()
            {
                @Override
                public void run()
                {
                    try
                    {
                        remoteFileIndexes.put(agentController, agentController.getAgentFilesIndex());
                        progress.increaseCount();
                    }
                    catch (final Exception ex)
                    {
                        failedAgentControllers.add(agentController, ex);
                        LOG.error("Failed uploading agent files to " + agentController, ex);
                    }
                    finally
                    {
                        latch.countDown();
                    }
                }
            });
        }

        try
        {
            latch.await();
        }
        catch (final InterruptedException e)
        {
            LOG.error("Waiting for file indexes failed. " + MasterController.getDetailedMessage(e), e);
        }

        return remoteFileIndexes;
    }

    /**
     * Summarize remote file indexes.
     * 
     * @param remoteFileIndexes
     *            remote file indexes
     * @param progress
     *            progress bar
     * @return
     */
    /*
     * Progress count: 1
     */
    private Map<FileReplicationIndex, Set<AgentController>> summarizeFileIndexes(final Map<AgentController, FileReplicationIndex> remoteFileIndexes,
                                                                                 final ProgressBar progress)
    {
        LOG.debug("Map indexes to agent controllers");
        final Map<FileReplicationIndex, Set<AgentController>> fileIndexes = new HashMap<FileReplicationIndex, Set<AgentController>>();

        // reverse current mapping to summon agent controllers with equal file indexes
        for (final Map.Entry<AgentController, FileReplicationIndex> acFileIndex : remoteFileIndexes.entrySet())
        {
            final FileReplicationIndex fileIndex = acFileIndex.getValue();

            Set<AgentController> agentControllers = fileIndexes.get(fileIndex);
            if (agentControllers == null)
            {
                agentControllers = new HashSet<AgentController>();
            }
            agentControllers.add(acFileIndex.getKey());

            fileIndexes.put(fileIndex, agentControllers);
        }

        progress.increaseCount();

        return fileIndexes;
    }

    /**
     * Update agent controllers.
     * 
     * @param workDir
     *            local working directory
     * @param localIndex
     *            local file index
     * @param progress
     *            progress bar
     * @throws AgentControllerException
     *             if one of the following reasons
     *             <ul>
     *             <li>mastercontroller is not in relaxed mode and at least one agent controller did not respond</li>
     *             <li>an exception was thrown at agent site</li>
     *             </ul>
     * @throws IOException
     *             if an I/O error occurs on archiving agent files for update
     */
    // Progress count:
    // file indexes : 5 * agentControllers.size() + 1
    public void update(final File workDir, final FileReplicationIndex localIndex, final ProgressBar progress)
        throws AgentControllerException, IOException
    {
        /*
         * update remote files
         */
        updateRemoteFiles(workDir, localIndex, progress);

        /*
         * Update total agent count
         */
        // get agent count of connected agent controllers
        final int totalAgentCount = getTotalAgentCount();

        // update total number of agents for connected agent controllers
        updateTotalAgentCount(totalAgentCount, progress);
    }

    /**
     * Upload and update files.
     * 
     * @param workDir
     *            local working directory
     * @param localIndex
     *            local file index
     * @param progress
     *            progress bar
     * @throws AgentControllerException
     *             if one of the following reasons
     *             <ul>
     *             <li>mastercontroller is not in relaxed mode and at least one agent controller did not respond</li>
     *             <li>an exception was thrown at agent site</li>
     *             </ul>
     * @throws IOException
     *             if an I/O error occurs on archiving agent files for update
     */
    /*
     * Progress count: 4 * agentControllers.size() + 1
     */
    private void updateRemoteFiles(final File workDir, final FileReplicationIndex localIndex, final ProgressBar progress)
        throws AgentControllerException, IOException
    {
        final Map<FileReplicationIndex, Set<AgentController>> remoteFileIndexes;
        if (this.remoteFileIndexes == null)
        {
            throw new IOException("No remote file index. Please run 'prepare' before 'update'.");
        }
        else
        {
            remoteFileIndexes = this.remoteFileIndexes;
            this.remoteFileIndexes = null;
        }

        /*
         * upload files
         */
        uploadDifferences(workDir, localIndex, remoteFileIndexes, progress);

        /*
         * poll for upload done and start update
         */
        pollForUpdateSuccess(remoteFileIndexes, progress);
    }

    /**
     * Upload the file update.
     * 
     * @param workDir
     *            local working directory
     * @param localIndex
     *            local file index
     * @param remoteFileIndexes
     *            remote file indexes mapped to corresponding agent controllers
     * @param progress
     *            progress bar
     * @throws AgentControllerException
     *             if one of the following reasons
     *             <ul>
     *             <li>mastercontroller is not in relaxed mode and at least one agent controller did not respond</li>
     *             <li>an exception was thrown at agent site</li>
     *             </ul>
     * @throws IOException
     *             if an I/O error occurs on archiving agent files for update
     */
    /*
     * Progress count: 2 * agentControllers.size()
     */
    private void uploadDifferences(final File workDir, final FileReplicationIndex localIndex,
                                   final Map<FileReplicationIndex, Set<AgentController>> remoteFileIndexes, final ProgressBar progress)
        throws AgentControllerException, IOException
    {
        for (final Map.Entry<FileReplicationIndex, Set<AgentController>> indexAgents : remoteFileIndexes.entrySet())
        {
            LOG.debug("Compute file index differences");
            // get the files to be updated or deleted on the agent
            final List<File> filesToBeDeleted = new ArrayList<File>();
            final List<File> filesToBeUpdated = new ArrayList<File>();
            FileReplicationUtils.compareIndexes(localIndex, indexAgents.getKey(), filesToBeUpdated, filesToBeDeleted);

            logFileUpdate(filesToBeUpdated, filesToBeDeleted, indexAgents.getValue());

            // zip differences and upload
            zipAndUpload(workDir, filesToBeUpdated, filesToBeDeleted, indexAgents.getValue(), progress);
        }
    }

    /**
     * Archives the files for upload and trigger update for the agent controllers.
     * 
     * @param workDir
     *            working directory
     * @param filesToBeUpdated
     *            files to be uploaded to agent controller
     * @param filesToBeDeleted
     *            files to be deleted on agent controller
     * @param indexAgents
     *            file indexes and corresponding agent controllers
     * @param progress
     *            progress bar
     * @throws AgentControllerException
     *             if one of the following reasons
     *             <ul>
     *             <li>mastercontroller is not in relaxed mode and at least one agent controller did not respond</li>
     *             <li>an exception was thrown at agent site</li>
     *             </ul>
     * @throws IOException
     *             if an I/O error occurs on archiving agent files for update
     */
    /*
     * Progress count: 2 * agentControllers.size()
     */
    private void zipAndUpload(final File workDir, final List<File> filesToBeUpdated, final List<File> filesToBeDeleted,
                              final Set<AgentController> agentControllersForUpload, final ProgressBar progress)
        throws AgentControllerException, IOException
    {
        // zip the files to be updated and update the agent
        LOG.info("Zip update files");
        final File archiveFile = archiveAgentFiles(filesToBeUpdated, workDir);

        LOG.info("Upload file update");
        final CountDownLatch latch = new CountDownLatch(agentControllersForUpload.size());
        for (final AgentController agentController : agentControllersForUpload)
        {
            uploadExecutor.execute(new Runnable()
            {
                @Override
                public void run()
                {
                    try
                    {
                        // create a unique file name (#2494)
                        final String updateFileName = String.format("agentfiles_%s_%s.zip", agentController.getName(), UUID.randomUUID());

                        // upload
                        LOG.debug(agentController + " uploading");
                        agentController.getFileManager().uploadFile(archiveFile, updateFileName);
                        LOG.debug(agentController + " upload finished");
                        progress.increaseCount();

                        // trigger update
                        LOG.debug(agentController + " updating");
                        agentController.updateAgentFiles(updateFileName, filesToBeDeleted);
                        progress.increaseCount();
                    }
                    catch (final Exception ex)
                    {
                        failedAgentControllers.add(agentController, ex);
                        LOG.error("Failed uploading files to " + agentController, ex);
                    }
                    finally
                    {
                        latch.countDown();
                    }
                }
            });
        }

        try
        {
            latch.await();
        }
        catch (final InterruptedException e)
        {
            LOG.error("Waiting for file upload to complete failed. " + MasterController.getDetailedMessage(e), e);
        }

        LOG.debug("Clean up file update");
        org.apache.commons.io.FileUtils.deleteQuietly(archiveFile);
    }

    /**
     * Poll agent controllers for update success.
     * 
     * @param remoteFileIndexes
     *            remote file index mapped to corresponding agent controllers
     * @param progress
     *            progress bar
     */
    /*
     * Progress count : agentControllers.size()
     */
    private void pollForUpdateSuccess(final Map<FileReplicationIndex, Set<AgentController>> remoteFileIndexes, final ProgressBar progress)
    {
        LOG.info("Wait for update is done");
        Poll.poll(downloadExecutor, UPLOAD_POLL_TASK, getUpdatingAgentControllers(remoteFileIndexes), failedAgentControllers, progress);
    }

    /**
     * Get all updating agent controllers.
     * 
     * @param remoteFileIndexes
     *            agent controllers that received an update.
     * @return updating agent controllers
     */
    private static Collection<AgentController> getUpdatingAgentControllers(final Map<FileReplicationIndex, Set<AgentController>> remoteFileIndexes)
    {
        final Map<String, AgentController> stillUpdating = new HashMap<>();
        for (final Set<AgentController> agentControllers : remoteFileIndexes.values())
        {
            for (final AgentController agentController : agentControllers)
            {
                stillUpdating.put(agentController.getName(), agentController);
            }
        }
        return stillUpdating.values();
    }

    /**
     * Get the failed agent controllers.
     * 
     * @return the failed agent controllers.
     */
    public FailedAgentControllerCollection getFailedAgentControllers()
    {
        return failedAgentControllers;
    }

    /**
     * Log the files to update.
     * 
     * @param filesToBeUpdated
     *            files to be updated
     * @param filesToBeDeleted
     *            files to be deleted
     * @param agentControllers
     *            agent controllers
     */
    private void logFileUpdate(final List<File> filesToBeUpdated, final List<File> filesToBeDeleted,
                               final Set<AgentController> agentControllers)
    {
        if (!LOG.isDebugEnabled())
        {
            return;
        }

        final StringBuilder sb = new StringBuilder();
        for (final AgentController agentController : agentControllers)
        {
            sb.append("\n -> ").append(agentController);
        }
        final String modifiedAgentcontrollers = sb.toString();

        LOG.debug("Files to be deleted on agent controller(s): " + modifiedAgentcontrollers);
        for (final File file : filesToBeDeleted)
        {
            LOG.debug(file.toString());
        }

        LOG.debug("Files to be updated on agent controller(s): " + modifiedAgentcontrollers);
        for (final File file : filesToBeUpdated)
        {
            LOG.debug(file.toString());
        }
    }

    /**
     * Upload total agent count to connected agent controllers.
     * 
     * @param totalAgentCount
     *            total agent count
     * @param progress
     *            progress bar
     * @throws AgentControllerException
     *             if one of the following reasons
     *             <ul>
     *             <li>mastercontroller is not in relaxed mode and at least one agent controller did not respond</li>
     *             <li>an exception was thrown at agent site</li>
     *             </ul>
     */
    /*
     * Progress count : agentControllers.size()
     */
    private void updateTotalAgentCount(final int totalAgentCount, final ProgressBar progress) throws AgentControllerException
    {
        final CountDownLatch latch = new CountDownLatch(agentControllers.size());
        for (final AgentController agentController : agentControllers)
        {
            uploadExecutor.execute(new Runnable()
            {
                @Override
                public void run()
                {
                    try
                    {
                        agentController.setTotalAgentCount(totalAgentCount);
                        progress.increaseCount();
                    }
                    catch (final Exception ex)
                    {
                        failedAgentControllers.add(agentController, ex);
                        LOG.error("Failed updating total agent count at " + agentController, ex);
                    }
                    latch.countDown();
                }
            });
        }

        try
        {
            latch.await();
        }
        catch (final InterruptedException e)
        {
            throw new RuntimeException("Waiting for agent count update failed. " + MasterController.getDetailedMessage(e), e);
        }
    }

    /**
     * Creates a ZIP file from the files contained in the passed list.
     * 
     * @param filesToArchive
     *            the list of files to be zip'ed
     * @param rootDir
     *            the directory with the agent files
     * @return the archive file
     * @throws java.io.IOException
     *             if an I/O error occurs
     */
    private File archiveAgentFiles(final List<File> filesToArchive, final File rootDir) throws IOException
    {
        // make file paths absolute based on the agent files directory
        final File[] files = new File[filesToArchive.size()];
        for (int i = 0; i < files.length; i++)
        {
            files[i] = new File(rootDir, filesToArchive.get(i).getPath()).getCanonicalFile();
        }

        // zip the files
        final File zipFile = File.createTempFile("agentfiles-", ".zip", tempDirectory);
        zipFile.deleteOnExit();

        LOG.debug("Zipping agent files from '" + rootDir + "' to '" + zipFile + "' ...");

        final FileFilter fileListFileFilter = new FileListFileFilter(files);
        ZipUtils.zipDirectory(rootDir, fileListFileFilter, zipFile);

        return zipFile;
    }

    /**
     * Get the total agent count.
     * 
     * @throws AgentControllerException
     *             if one of the following reasons
     *             <ul>
     *             <li>mastercontroller is not in relaxed mode and at least one agent controller did not respond</li>
     *             <li>an exception was thrown at agent site</li>
     *             </ul>
     */
    private int getTotalAgentCount() throws AgentControllerException
    {
        int totalAgentcount = 0;
        for (final AgentController agentcontroller : agentControllers)
        {
            totalAgentcount += agentcontroller.getAgentCount();
        }
        return totalAgentcount;
    }
}
