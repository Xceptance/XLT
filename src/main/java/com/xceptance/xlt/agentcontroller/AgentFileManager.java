/*
 * Copyright (c) 2005-2022 Xceptance Software Technologies GmbH
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
package com.xceptance.xlt.agentcontroller;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

import org.apache.commons.io.filefilter.FileFilterUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xceptance.common.io.FileUtils;
import com.xceptance.common.util.zip.ZipUtils;
import com.xceptance.xlt.util.FileReplicationIndex;
import com.xceptance.xlt.util.FileReplicationUtils;

public class AgentFileManager
{
    private static final Logger log = LoggerFactory.getLogger(AgentFileManager.class);

    private final File directory;

    public AgentFileManager(final File directory)
    {
        this.directory = directory;
    }

    /**
     * Updates the local agent files. New or changed files are contained in passed ZIP file. Files that are obsolete and
     * can be deleted are named in the given file list.
     * 
     * @param zipFile
     *            the ZIP file with the agent files
     * @param filesToBeDeleted
     *            the list of obsolete files
     * @param agentManagers
     *            Agent managers to be updated
     */
    public void updateAgentFiles(final File zipFile, final List<File> filesToBeDeleted, final Collection<AgentManager> agentManagers)
    {
        try
        {
            org.apache.commons.io.FileUtils.forceMkdir(directory);

            // cleanup obsolete files
            log.info("Deleting obsolete agent files");
            for (final File file : filesToBeDeleted)
            {
                final String relativePath = file.getPath().replace('\\', '/');
                final File absoluteFile = new File(directory, relativePath);

                log.debug("Deleting file '" + absoluteFile + "' ...");
                if (!FileUtils.deleteDirectoryRelaxed(absoluteFile))
                {
                    // directory is not deleted
                    log.warn("Unable to remove " + absoluteFile.getAbsoluteFile());
                }
            }

            log.info("Installing new and updated agent files");

            log.debug("Unzipping agent archive '" + zipFile + "' to '" + directory + "' ...");
            ZipUtils.unzipFile(zipFile, directory);
            log.debug("Unzip finished.");

            for (final AgentManager agentManager : agentManagers)
            {
                agentManager.updateAgentFiles(directory);
            }
        }
        catch (final Exception ex)
        {
            throw new RuntimeException(ex);
        }
    }

    public FileReplicationIndex getAgentFilesIndex()
    {
        // report all files except svn files
        if (directory.exists())
        {
            return FileReplicationUtils.getIndex(directory, FileFilterUtils.makeSVNAware(null));
        }
        else
        {
            return null;
        }
    }

    /**
     * removes update directory
     * 
     * @throws IOException
     */
    public void clear() throws IOException
    {
        if (directory.exists())
        {
            org.apache.commons.io.FileUtils.deleteDirectory(directory);
        }
    }
}
