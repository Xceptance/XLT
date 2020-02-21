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
