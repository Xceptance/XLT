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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xceptance.xlt.agentcontroller.PartialGetUtils.RangeHeaderData;
import com.xceptance.xlt.engine.httprequest.HttpRequestHeaders;
import com.xceptance.xlt.engine.httprequest.HttpResponseHeaders;

/**
 * The FileManagerServlet handles all file requests made from the master controller.
 *
 * @author Jörg Werner (Xceptance Software Technologies GmbH)
 */
public class FileManagerServlet extends HttpServlet
{
    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = -4559286002497439251L;

    /**
     * class logger
     */
    private static final Logger log = LoggerFactory.getLogger(FileManagerServlet.class);

    /**
     * servlet path
     */
    static final String SERVLET_PATH = "/fileManager/";

    /**
     * servlet mapping
     */
    static final String SERVLET_MAPPING = SERVLET_PATH + "*";

    /**
     * web root directory
     */
    private final File rootDirectory;

    /**
     * Creates a new FileManagerServlet object.
     *
     * @param rootDirectory
     *            the local directory that is the web root
     */
    public FileManagerServlet(final File rootDirectory)
    {
        this.rootDirectory = rootDirectory;
    }

    /**
     * Handles all download requests.
     *
     * @param req
     *            the servlet request
     * @param resp
     *            the servlet response
     */
    @Override
    protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException
    {
        InputStream in = null;
        final String fileName = getFileName(req);

        try
        {
            log.debug("File being downloaded: " + fileName);

            // paranoia check
            if (fileName == null)
            {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }

            final File file = new File(rootDirectory, fileName);

            // check if the file does not exist
            if (!file.isFile())
            {
                // handle file does not exist
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                return;
            }

            // check if the file is empty
            final long fileLength = file.length();
            if (fileLength == 0)
            {
                // handle empty file
                resp.setStatus(HttpServletResponse.SC_OK);
                resp.setContentLengthLong(0);
                return;
            }

            // open the file for reading
            in = new FileInputStream(file);

            // check for a partial GET request
            final String rangeHeaderValue = req.getHeader(HttpRequestHeaders.RANGE);
            if (rangeHeaderValue == null)
            {
                /*
                 * No partial request -> serve the full file content in one go.
                 */

                log.debug("Serving full content from file '{}' ...", file);

                resp.setStatus(HttpServletResponse.SC_OK);
                resp.setContentLengthLong(fileLength);

                final OutputStream out = resp.getOutputStream();

                IOUtils.copyLarge(in, out);
            }
            else
            {
                /*
                 * Partial request -> serve only the requested part of the file.
                 */

                // validate the Range request header
                final RangeHeaderData rangeHeaderData = PartialGetUtils.parseRangeHeader(rangeHeaderValue);
                if (rangeHeaderData == null)
                {
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    return;
                }

                // extract and validate the start/end position passed in the Range header
                final long startPos = rangeHeaderData.startPos;
                final long endPos = rangeHeaderData.endPos;

                if (startPos > endPos || startPos > fileLength - 1)
                {
                    resp.setStatus(HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE);
                    return;
                }

                // determine how many bytes can be served at all
                final long finalEndPos = Math.min(endPos, fileLength - 1);
                final long bytes = finalEndPos - startPos + 1;

                // prepare the Content-Range response header
                final String contentRangeHeaderValue = PartialGetUtils.formatContentRangeHeader(startPos, finalEndPos, fileLength);

                // serve the requested byte range
                log.debug("Serving chunk {}-{} from file '{}' ...", startPos, endPos, file);

                resp.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
                resp.setContentLengthLong(bytes);
                resp.setHeader(HttpResponseHeaders.CONTENT_RANGE, contentRangeHeaderValue);

                final OutputStream out = resp.getOutputStream();

                IOUtils.copyLarge(in, out, startPos, bytes);
            }
        }
        catch (final Exception ex)
        {
            log.error("Error while file is downloaded: " + fileName, ex);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
        finally
        {
            IOUtils.closeQuietly(in);
        }
    }

    /**
     * Handles all upload requests.
     *
     * @param req
     *            the servlet request
     * @param resp
     *            the servlet response
     */
    @Override
    protected void doPut(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException
    {
        OutputStream out = null;
        final String fileName = getFileName(req);

        try
        {
            log.debug("File being uploaded: " + fileName);

            if (fileName == null)
            {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            }
            else
            {
                final File file = new File(rootDirectory, fileName);

                out = new FileOutputStream(file);
                final InputStream in = req.getInputStream();

                IOUtils.copy(in, out);

                resp.setStatus(HttpServletResponse.SC_OK);
            }
        }
        catch (final Exception ex)
        {
            log.error("Error while file is uploaded: " + fileName, ex);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
        finally
        {
            IOUtils.closeQuietly(out);
        }
    }

    /**
     * Returns the file name from the URL parameters.
     *
     * @param req
     *            the servlet request
     * @return the file name, or null if not found
     */
    private String getFileName(final HttpServletRequest req)
    {
        String pathInfo = req.getPathInfo();

        if (pathInfo == null || pathInfo.isEmpty())
        {
            return null;
        }

        if (pathInfo.charAt(0) == '/')
        {
            if (pathInfo.length() == 1)
            {
                return null;
            }

            pathInfo = pathInfo.substring(1);
        }

        return pathInfo;
    }
}
