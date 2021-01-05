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
