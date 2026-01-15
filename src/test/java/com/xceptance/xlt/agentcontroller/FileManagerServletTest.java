/*
 * Copyright (c) 2005-2026 Xceptance Software Technologies GmbH
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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.UUID;

import javax.servlet.ReadListener;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class FileManagerServletTest
{
    private File tempDir;

    private File rootDir;

    private FileManagerServlet servlet;

    private HttpServletRequest request;

    private HttpServletResponse response;

    @Before
    public void setup() throws IOException
    {
        tempDir = Files.createTempDirectory("FileManagerServletTest-" + UUID.randomUUID()).toFile();
        rootDir = new File(tempDir, "root");
        rootDir.mkdirs();
        servlet = new FileManagerServlet(rootDir);
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
    }

    @After
    public void tearDown() throws IOException
    {
        FileUtils.deleteDirectory(tempDir);
    }

    @Test
    public void testDoGet_PathTraversal() throws ServletException, IOException
    {
        // Try to access a file outside the root directory
        when(request.getPathInfo()).thenReturn("/../secret.txt");

        servlet.doGet(request, response);

        // Expect 403 Forbidden for bad paths.
        verify(response).setStatus(HttpServletResponse.SC_FORBIDDEN);
    }

    @Test
    public void testDoPut_PathTraversal() throws ServletException, IOException
    {
        when(request.getPathInfo()).thenReturn("/../malicious.txt");

        servlet.doPut(request, response);

        verify(response).setStatus(HttpServletResponse.SC_FORBIDDEN);
    }

    @Test
    public void testDoGet_ValidFile() throws ServletException, IOException
    {
        // create a valid file
        final File validFile = new File(rootDir, "test.txt");
        FileUtils.writeStringToFile(validFile, "Hello World", "UTF-8");

        when(request.getPathInfo()).thenReturn("/test.txt");
        when(response.getOutputStream()).thenReturn(createNoOpServletOutputStream());
        servlet.doGet(request, response);

        verify(response).setStatus(HttpServletResponse.SC_OK);
        // We could also verify content length or interactions with output stream if we mocked it,
        // but status code 200 confirms it didn't block it.
    }

    @Test
    public void testDoPut_ValidFile() throws ServletException, IOException
    {
        final String fileName = "upload.txt";
        final File targetFile = new File(rootDir, fileName);

        when(request.getPathInfo()).thenReturn("/" + fileName);
        // mock input stream if we want to test content writing, but for path safety check, just triggering doPut logic is
        // enough
        when(request.getInputStream()).thenReturn(createEmptyServletInputStream());

        servlet.doPut(request, response);

        verify(response).setStatus(HttpServletResponse.SC_OK);
        // Verify file was created (even if empty)
        Assert.assertTrue("File should have been created", targetFile.exists());
    }

    @Test
    public void testDoGet_ValidSubdirectory() throws ServletException, IOException
    {
        final File subDir = new File(rootDir, "subdir");
        subDir.mkdirs();
        final File validFile = new File(subDir, "test.txt");
        FileUtils.writeStringToFile(validFile, "Hello Subdir", "UTF-8");

        when(request.getPathInfo()).thenReturn("/subdir/test.txt");
        when(response.getOutputStream()).thenReturn(createNoOpServletOutputStream());
        servlet.doGet(request, response);

        verify(response).setStatus(HttpServletResponse.SC_OK);
    }

    @Test
    public void testDoPut_ValidSubdirectory() throws ServletException, IOException
    {
        // Parent directories will be created automatically by the servlet

        final String fileName = "subdir_upload/upload.txt";
        final File targetFile = new File(rootDir, fileName);

        when(request.getPathInfo()).thenReturn("/" + fileName);
        when(request.getInputStream()).thenReturn(createEmptyServletInputStream());

        servlet.doPut(request, response);

        verify(response).setStatus(HttpServletResponse.SC_OK);

        Assert.assertTrue("File should have been created", targetFile.exists());
    }

    @Test
    public void testDoGet_PartialPathTraversal() throws ServletException, IOException
    {
        // One level up is tempDir. Create a directory that starts with "root" but is not "root" (e.g. "root-sibling")
        final File siblingDir = new File(tempDir, "root-sibling");
        siblingDir.mkdirs();
        final File secretFile = new File(siblingDir, "secret.txt");
        FileUtils.writeStringToFile(secretFile, "Secret Content", "UTF-8");

        // The path info should be constructed such that it resolves to the sibling file.
        // If we request /../root-sibling/secret.txt relative to root, it stays outside.
        // But wait, the servlet constructs new File(rootDirectory, fileName).
        // ROOT: /tmp/UUID/root
        // REQUEST: /../root-sibling/secret.txt
        // FILE: /tmp/UUID/root/../root-sibling/secret.txt
        // CANONICAL: /tmp/UUID/root-sibling/secret.txt
        // ROOT CANONICAL: /tmp/UUID/root/
        // /tmp/UUID/root-sibling/secret.txt STARTS WITH /tmp/UUID/root/ ? NO.

        when(request.getPathInfo()).thenReturn("/../root-sibling/secret.txt");
        servlet.doGet(request, response);

        // Expect 403 Forbidden because it is outside the root directory (even if it shares prefix)
        verify(response).setStatus(HttpServletResponse.SC_FORBIDDEN);
    }

    @Test
    public void testDoPut_PartialPathTraversal() throws ServletException, IOException
    {
        // One level up is tempDir. Create a directory that starts with "root" but is not "root" (e.g. "root-sibling")
        final File siblingDir = new File(tempDir, "root-sibling");
        siblingDir.mkdirs();
        // File to upload
        final String fileName = "../root-sibling/malicious.txt";

        when(request.getPathInfo()).thenReturn("/" + fileName);
        when(request.getInputStream()).thenReturn(createEmptyServletInputStream());

        servlet.doPut(request, response);

        verify(response).setStatus(HttpServletResponse.SC_FORBIDDEN);
    }

    @Test
    public void testDoGet_EdgeCase_AbsolutePath() throws ServletException, IOException
    {
        // Test absolute path traversal
        when(request.getPathInfo()).thenReturn("//etc/passwd");

        servlet.doGet(request, response);

        // Expect 404 Not Found because the file doesn't exist yet
        // The path "/etc/passwd" is treated as relative to root, resulting in "/tmp/root/etc/passwd"
        verify(response).setStatus(HttpServletResponse.SC_NOT_FOUND);
    }

    @Test
    public void testDoPut_EdgeCase_AbsolutePath() throws ServletException, IOException
    {
        // Test absolute path traversal
        when(request.getPathInfo()).thenReturn("//etc/passwd");
        when(request.getInputStream()).thenReturn(createEmptyServletInputStream());

        servlet.doPut(request, response);

        // Expect 200 OK - parent directories are created automatically
        // The path "/etc/passwd" is treated as relative to root, resulting in "/tmp/root/etc/passwd"
        verify(response).setStatus(HttpServletResponse.SC_OK);

        // Verify the file was created in the correct location
        final File expectedFile = new File(rootDir, "etc/passwd");
        Assert.assertTrue("File should have been created", expectedFile.exists());
    }

    @Test
    public void testDoGet_EdgeCase_CurrentDirectory() throws ServletException, IOException
    {
        // Test /./ references (valid)
        final File validFile = new File(rootDir, "test.txt");
        FileUtils.writeStringToFile(validFile, "Hello World", "UTF-8");

        when(request.getPathInfo()).thenReturn("/./test.txt");
        when(response.getOutputStream()).thenReturn(createNoOpServletOutputStream());

        servlet.doGet(request, response);

        verify(response).setStatus(HttpServletResponse.SC_OK);
    }

    @Test
    public void testDoPut_EdgeCase_CurrentDirectory() throws ServletException, IOException
    {
        // Test /./ references (valid)
        final String fileName = "./upload_current_dir.txt";
        final File targetFile = new File(rootDir, "upload_current_dir.txt");

        when(request.getPathInfo()).thenReturn("/" + fileName);
        when(request.getInputStream()).thenReturn(createEmptyServletInputStream());

        servlet.doPut(request, response);

        verify(response).setStatus(HttpServletResponse.SC_OK);
        Assert.assertTrue("File should have been created", targetFile.exists());
    }

    @Test
    public void testDoGet_EdgeCase_UrlEncoded() throws ServletException, IOException
    {
        // Test URL-encoded traversal "..%2F"
        when(request.getPathInfo()).thenReturn("/..%2Fsecret.txt");

        servlet.doGet(request, response);

        // Expect 404 Not Found.
        // If the container does NOT decode the path, it looks for a file strictly named "..%2Fsecret.txt", which does not
        // exist.
        // If it DOES decode it to "../", it would be blocked by our traversal check (403).
        // A 404 confirms that either the file was not found (safe) or the path was not interpreted as a traversal helper by the
        // OS.
        verify(response).setStatus(HttpServletResponse.SC_NOT_FOUND);
    }

    @Test
    public void testDoPut_EdgeCase_UrlEncoded() throws ServletException, IOException
    {
        // Test URL-encoded traversal "..%2F"
        final File targetFile = new File(rootDir, "..%2Fsecret.txt");

        when(request.getPathInfo()).thenReturn("/..%2Fsecret.txt");
        when(request.getInputStream()).thenReturn(createEmptyServletInputStream());

        servlet.doPut(request, response);

        verify(response).setStatus(HttpServletResponse.SC_OK);
        Assert.assertTrue("File should have been created", targetFile.exists());
    }

    private ServletInputStream createEmptyServletInputStream()
    {
        return new ServletInputStream()
        {
            @Override
            public int read() throws IOException
            {
                return -1; // end of stream
            }

            @Override
            public boolean isFinished()
            {
                return true;
            }

            @Override
            public boolean isReady()
            {
                return true;
            }

            @Override
            public void setReadListener(final ReadListener readListener)
            {
            }
        };
    }

    private ServletOutputStream createNoOpServletOutputStream()
    {
        return new ServletOutputStream()
        {
            @Override
            public void write(final int b) throws IOException
            {
            }

            @Override
            public boolean isReady()
            {
                return true;
            }

            @Override
            public void setWriteListener(final WriteListener writeListener)
            {
            }
        };
    }
}
