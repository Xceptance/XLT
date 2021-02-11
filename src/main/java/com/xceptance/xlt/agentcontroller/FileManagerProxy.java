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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.xceptance.common.net.UrlConnectionFactory;

/**
 * The FileManagerProxy class is the client-side implementation of the FileManager interface, i.e. it runs on the master
 * controller.
 */
public class FileManagerProxy implements FileManager
{
    private static final Log log = LogFactory.getLog(FileManagerProxy.class);

    private final URL url;

    private final UrlConnectionFactory urlConnectionFactory;

    /**
     * Creates a new FileManagerProxy object.
     * 
     * @param url
     *            the agent controller's URL
     * @param urlConnectionFactory
     *            the URL connection factory to use
     * @throws MalformedURLException
     *             if the file manager's URL cannot be created
     */
    public FileManagerProxy(final URL url, final UrlConnectionFactory urlConnectionFactory) throws MalformedURLException
    {
        this.url = new URL(url + FileManagerServlet.SERVLET_PATH);
        this.urlConnectionFactory = urlConnectionFactory;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteFile(final String remoteFileName) throws IOException
    {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void downloadFile(final File localFile, final String remoteFileName) throws IOException
    {
        InputStream cin = null;
        OutputStream fout = null;

        try
        {
            final URL downloadUrl = new URL(url + remoteFileName);

            log.debug("Downloading file from '" + downloadUrl + "' to '" + localFile + "' ...");

            // make sure the target directory exists
            FileUtils.forceMkdir(localFile.getParentFile());

            // download file
            fout = new FileOutputStream(localFile);
            final URLConnection conn = urlConnectionFactory.open(downloadUrl);
            cin = conn.getInputStream();
            IOUtils.copy(cin, fout);
        }
        finally
        {
            IOUtils.closeQuietly(fout);
            IOUtils.closeQuietly(cin);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void uploadFile(final File localFile, final String remoteFileName) throws IOException
    {
        InputStream fin = null;
        OutputStream cout = null;

        try
        {
            final URL uploadUrl = new URL(url + remoteFileName);

            log.debug("Uploading file '" + localFile + "' to '" + uploadUrl + "' ...");

            // prepare connection for upload
            final HttpURLConnection conn = (HttpURLConnection) urlConnectionFactory.open(uploadUrl);
            conn.setDoOutput(true);
            conn.setRequestMethod("PUT");
            conn.setChunkedStreamingMode(4096);

            // upload file
            fin = new FileInputStream(localFile);
            cout = conn.getOutputStream();
            IOUtils.copy(fin, cout);

            // check the response
            final int responseCode = conn.getResponseCode();
            if (responseCode != 200)
            {
                // mimic the same exception as it is automatically thrown for GET requests (see above)
                throw new IOException(String.format("Server returned HTTP response code: %d for URL: %s", responseCode, uploadUrl));
            }
        }
        finally
        {
            IOUtils.closeQuietly(fin);
            IOUtils.closeQuietly(cout);
        }
    }
}
