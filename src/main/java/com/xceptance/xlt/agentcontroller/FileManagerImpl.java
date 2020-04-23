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
package com.xceptance.xlt.agentcontroller;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

/**
 * A special {@link FileManager} implementation that is used when running an embedded agent controller. This
 * implementation does not transfer files via HTTPS, but simply copies files around.
 */

public class FileManagerImpl implements FileManager
{
    private final File rootDirectory;

    public FileManagerImpl(final File rootDirectory)
    {
        this.rootDirectory = rootDirectory;
    }

    /**
     * {@inheritDoc}
     */

    @Override
    public void deleteFile(final String remoteFileName) throws IOException
    {
        final File remoteFile = new File(rootDirectory, remoteFileName);

        FileUtils.deleteQuietly(remoteFile);
    }

    /**
     * {@inheritDoc}
     */

    @Override
    public void downloadFile(final File localFile, final String remoteFileName) throws IOException
    {
        final File remoteFile = new File(rootDirectory, remoteFileName);

        FileUtils.copyFile(remoteFile, localFile);
    }

    /**
     * {@inheritDoc}
     */

    @Override
    public void uploadFile(final File localFile, final String remoteFileName) throws IOException
    {
        final File remoteFile = new File(rootDirectory, remoteFileName);

        FileUtils.copyFile(localFile, remoteFile);
    }
}
