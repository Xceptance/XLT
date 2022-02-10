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

            if (fileName == null)
            {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            }
            else
            {
                final File file = new File(rootDirectory, fileName);
                in = new FileInputStream(file);

                resp.setContentLength((int) file.length());
                // resp.setContentType("???");

                final OutputStream out = resp.getOutputStream();

                IOUtils.copy(in, out);

                resp.setStatus(HttpServletResponse.SC_OK);
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
