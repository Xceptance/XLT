package com.xceptance.xlt.agentcontroller;

import java.io.File;
import java.io.IOException;

/**
 * The AgentManager interface represents the (remote) file manager and defines its API.
 */

public interface FileManager
{
    /**
     * Uploads the given local file to the agent controller. The file is stored there under the specified name.
     * 
     * @param localFile
     *            the file to upload
     * @param remoteFileName
     *            the remote file's name
     * @throws IOException
     *             if an I/O error occurs
     */

    public void uploadFile(File localFile, String remoteFileName) throws IOException;

    /**
     * Downloads the file with the given name from the agent controller. The file is stored locally under the specified
     * name.
     * 
     * @param localFile
     *            the location to store the downloaded file
     * @param remoteFileName
     *            the remote file's name
     * @throws IOException
     *             if an I/O error occurs
     */

    public void downloadFile(File localFile, String remoteFileName) throws IOException;

    /**
     * Deletes the file with the given name from the agent controller.
     * 
     * @param remoteFileName
     *            the remote file's name
     * @throws IOException
     *             if an I/O error occurs
     */

    public void deleteFile(String remoteFileName) throws IOException;

}
