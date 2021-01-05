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
package com.xceptance.xlt.agentcontroller;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.io.FileUtils;

public class ResultArchives
{
    /**
     * Agent results archive state options
     */
    public enum State
    {
        /**
         * Initial state
         */
        CLEAN,

        /**
         * Creating agent results archives is in progress
         */
        IN_PROGRESS,

        /**
         * Creating agent results archives is done and ready for download
         */
        READY_FOR_DOWNLOAD
    }

    /**
     * The current agent results archive state
     */
    private State state = State.CLEAN;

    private ArchiveToken archiveToken;

    /** agent ID : archive file */
    private Map<String, File> archives = new HashMap<String, File>();

    /**
     * Request permission to archive the agent results
     * 
     * @return <code>true</code> if an archive job is currently not running
     */
    public synchronized ArchiveToken requestCreating()
    {
        if (!state.equals(State.IN_PROGRESS))
        {
            clear();
            state = State.IN_PROGRESS;
            return archiveToken;
        }

        return null;
    }

    /**
     * Update current archive list
     * 
     * @param agentID
     *            the agent ID
     * @param archiveFile
     *            the agent results archive file
     * @param archiveToken
     *            current archive token
     */
    public synchronized boolean update(final String agentID, final File archiveFile, final ArchiveToken archiveToken)
    {
        if (!validate(archiveToken))
        {
            return false;
        }

        if (state.equals(State.IN_PROGRESS))
        {
            archives.put(agentID, archiveFile);
        }
        else
        {
            throw new IllegalStateException("Update result archive map for state " + State.IN_PROGRESS + " only.");
        }

        return true;
    }

    /**
     * Finish agent results archive creation and mark ready for download.
     * 
     * @param archiveToken
     *            current archive token
     */
    public synchronized boolean setReadyForDownload(final ArchiveToken archiveToken)
    {
        if (!validate(archiveToken))
        {
            return false;
        }

        state = State.READY_FOR_DOWNLOAD;
        return true;
    }

    /**
     * Get the agent results archive creation state
     * 
     * @return the agent results archive creation state
     */
    public synchronized State getState()
    {
        return state;
    }

    /**
     * Get the agent IDs and corresponding results archive file names
     * 
     * @return the agent IDs (key) and corresponding results archive file names (value)
     */
    public synchronized Map<String, String> getArchives()
    {
        /** agent ID : archive file name */
        final Map<String, String> map = new HashMap<String, String>();
        if (state.equals(State.READY_FOR_DOWNLOAD))
        {
            for (final Map.Entry<String, File> archive : archives.entrySet())
            {
                map.put(archive.getKey(), archive.getValue().getName());
            }
        }
        return map;
    }

    /**
     * Discard the latest archives
     */
    public synchronized void clear()
    {
        for (final File archiveFile : archives.values())
        {
            FileUtils.deleteQuietly(archiveFile);
        }

        archives.clear();

        archiveToken = new ArchiveToken();
        state = State.CLEAN;
    }

    /**
     * Check if given token matches current archive token.
     * 
     * @param archiveToken
     *            current archive token
     * @return
     */
    private boolean validate(final ArchiveToken archiveToken)
    {
        if(this.archiveToken != null)
        {
            return this.archiveToken.equals(archiveToken);
        }
        return false;
    }

    /**
     * ArchiveToken
     *
     */
    public static class ArchiveToken
    {
        private String tokenCode;

        private ArchiveToken()
        {
            tokenCode = UUID.randomUUID().toString();
        }


        /**
         * Get token string.
         */
        public String toString()
        {
            return tokenCode;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean equals(final Object token)
        {
            if (token != null && token instanceof ArchiveToken)
            {
                return this.tokenCode.equals(((ArchiveToken) token).tokenCode);
            }

            return false;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int hashCode()
        {
            return tokenCode.hashCode();
        }
    }
}
