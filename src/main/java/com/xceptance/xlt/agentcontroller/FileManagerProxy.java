/*
 * Copyright (c) 2005-2023 Xceptance Software Technologies GmbH
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
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xceptance.common.io.IoActionHandler;
import com.xceptance.common.net.UrlConnectionFactory;
import com.xceptance.xlt.agentcontroller.PartialGetUtils.ContentRangeHeaderData;
import com.xceptance.xlt.api.util.XltException;
import com.xceptance.xlt.engine.httprequest.HttpRequestHeaders;
import com.xceptance.xlt.engine.httprequest.HttpResponseHeaders;

/**
 * The FileManagerProxy class is the client-side implementation of the FileManager interface, i.e. it runs on the master
 * controller.
 */
public class FileManagerProxy implements FileManager
{
    /**
     * The result returned when downloading a chunk.
     */
    private static class ChunkInfo
    {
        /** The total size of the resource that is currently being downloaded. */
        public final long totalSize;

        /** The size of the current chunk. */
        public final long chunkSize;

        public ChunkInfo(final long totalSize, final long chunkSize)
        {
            super();
            this.totalSize = totalSize;
            this.chunkSize = chunkSize;
        }
    }

    private static final Logger log = LoggerFactory.getLogger(FileManagerProxy.class);

    private final URL url;

    private final UrlConnectionFactory urlConnectionFactory;

    /**
     * The size of a file chunk when downloading a result archive from an agent controller.
     */
    private final long downloadChunkSize;

    /**
     * The maximum number of retries in case downloading a result file (chunk) failed because of an I/O error.
     */
    private final int downloadMaxRetries;

    /**
     * Creates a new FileManagerProxy object.
     *
     * @param url
     *            the agent controller's URL
     * @param urlConnectionFactory
     *            the URL connection factory to use
     * @param downloadChunkSize
     *            the size of a file chunk
     * @param downloadMaxRetries
     *            the maximum number of download retries
     * @throws MalformedURLException
     *             if the file manager's URL cannot be created
     */
    public FileManagerProxy(final URL url, final UrlConnectionFactory urlConnectionFactory, final long downloadChunkSize,
                            final int downloadMaxRetries)
        throws MalformedURLException
    {
        this.url = new URL(url + FileManagerServlet.SERVLET_PATH);
        this.urlConnectionFactory = urlConnectionFactory;
        this.downloadChunkSize = downloadChunkSize;
        this.downloadMaxRetries = downloadMaxRetries;
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
        final URL downloadUrl = new URL(url + remoteFileName);

        log.debug("Downloading file from '{}' to '{}' ...", downloadUrl, localFile);

        // make sure the target directory exists
        FileUtils.forceMkdir(localFile.getParentFile());

        // download the file content in chunks
        long bytesRead = 0;
        long totalBytes = Long.MAX_VALUE;

        do
        {
            final long offset = bytesRead;
            final long bytes = Math.min(downloadChunkSize, totalBytes - offset);

            final ChunkInfo chunkInfo = new IoActionHandler(downloadMaxRetries).run(() -> downloadFileChunk(localFile, downloadUrl, offset,
                                                                                                          bytes));

            bytesRead += chunkInfo.chunkSize;
            totalBytes = chunkInfo.totalSize;
        }
        while (bytesRead < totalBytes);
    }

    /**
     * Downloads a file chunk from the given URL using a partial GET, falling back to downloading the full file if the
     * agent controller does not support partial GETs.
     *
     * @param localFile
     *            the file to download the chunk to
     * @param downloadUrl
     *            the URL to download the chunk from
     * @param offset
     *            the position in the remote file to start the download from
     * @param bytes
     *            the number of bytes to download
     * @return a {@link ChunkInfo} object containing the download details
     * @throws IOException
     *             if anything went wrong
     */
    private ChunkInfo downloadFileChunk(final File localFile, final URL downloadUrl, final long offset, final long bytes) throws IOException
    {
        final long startPos = offset;
        final long endPos = offset + bytes - 1;

        // request data with a partial GET
        final HttpURLConnection conn = (HttpURLConnection) urlConnectionFactory.open(downloadUrl);
        final String rangeHeaderValue = PartialGetUtils.formatRangeHeader(startPos, endPos);
        conn.setRequestProperty(HttpRequestHeaders.RANGE, rangeHeaderValue);

        // check what type of response we got
        final int statusCode = conn.getResponseCode();
        if (statusCode == HttpURLConnection.HTTP_OK)
        {
            /*
             * Response with the full content.
             */

            log.debug("Downloading complete file from '{}' ...", downloadUrl);

            final long bytesCopied = copyBytes(conn, localFile, false);

            return new ChunkInfo(bytesCopied, bytesCopied);
        }
        else if (statusCode == HttpURLConnection.HTTP_PARTIAL)
        {
            /*
             * Response with only a part of the content.
             */

            log.debug("Downloading chunk {}-{} from '{}' ...", startPos, endPos, downloadUrl);

            // validate Content-Range response header value and extract the total size of the file
            final String contentRangeHeaderValue = conn.getHeaderField(HttpResponseHeaders.CONTENT_RANGE);
            final ContentRangeHeaderData contentRangeHeaderData = PartialGetUtils.parseContentRangeHeader(contentRangeHeaderValue);
            if (contentRangeHeaderData == null)
            {
                throw new XltException("Received invalid Content-Range header: " + contentRangeHeaderValue);
            }

            // truncate the file to undo any previous attempt to append the current chunk
            truncateFile(localFile, offset);

            final long bytesCopied = copyBytes(conn, localFile, true);

            return new ChunkInfo(contentRangeHeaderData.totalBytes, bytesCopied);
        }
        else
        {
            /*
             * Unexpected response.
             */

            throw new XltException("Received unexpected status code: " + statusCode);
        }
    }

    /**
     * Truncates the given file to the given size.
     *
     * @param file
     *            the file to truncate
     * @param newSize
     *            the size to truncate the file to
     * @throws IOException
     *             if anything went wrong
     */
    private void truncateFile(final File file, final long newSize) throws IOException
    {
        if (file.length() > newSize)
        {
            try (RandomAccessFile raf = new RandomAccessFile(file, "rw"))
            {
                raf.setLength(newSize);
            }
        }
    }

    /**
     * Copies all available data from the URL connection to the file, either appending the data to the file or
     * overwriting it.
     *
     * @param conn
     *            the URL connection to read from
     * @param file
     *            the file to write to
     * @param append
     *            whether to append the data to the file or overwrite it
     * @return the number of bytes copied
     * @throws IOException
     *             if anything went wrong
     */
    private long copyBytes(final HttpURLConnection conn, final File file, final boolean append) throws IOException
    {
        try (final InputStream cin = conn.getInputStream(); final FileOutputStream fout = new FileOutputStream(file, append))
        {
            // copy what is available
            final long bytesCopied = IOUtils.copyLarge(cin, fout);

            // check whether we copied the expected number of bytes
            final long bytesAnnounced = conn.getContentLengthLong(); // -1 if not set
            if (bytesAnnounced != -1 && bytesAnnounced != bytesCopied)
            {
                throw new IOException(String.format("Expected %d bytes to copy but got %d bytes", bytesAnnounced, bytesCopied));
            }

            return bytesCopied;
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
