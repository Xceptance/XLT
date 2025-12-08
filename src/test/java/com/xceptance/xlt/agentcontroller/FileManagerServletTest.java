package com.xceptance.xlt.agentcontroller;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;
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
        when(request.getInputStream()).thenReturn(new javax.servlet.ServletInputStream()
        {
            @Override
            public int read() throws IOException
            {
                return -1;
            } // empty

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
            public void setReadListener(final javax.servlet.ReadListener readListener)
            {
            }
        });

        servlet.doPut(request, response);

        verify(response).setStatus(HttpServletResponse.SC_OK);
        // Verify file was created (even if empty)
        if (!targetFile.exists())
        {
            // Depending on implementation, it might create it.
            // The servlet logic:
            // out = new FileOutputStream(file);
            // IOUtils.copy(in, out);
            // This definitely creates the file.
            throw new AssertionError("File should have been created");
        }
    }

    @Test
    public void testDoGet_ValidSubdirectory() throws ServletException, IOException
    {
        final File subDir = new File(rootDir, "subdir");
        subDir.mkdirs();
        final File validFile = new File(subDir, "test.txt");
        FileUtils.writeStringToFile(validFile, "Hello Subdir", "UTF-8");

        when(request.getPathInfo()).thenReturn("/subdir/test.txt");
        servlet.doGet(request, response);

        verify(response).setStatus(HttpServletResponse.SC_OK);
    }

    @Test
    public void testDoPut_ValidSubdirectory() throws ServletException, IOException
    {
        final File subDir = new File(rootDir, "subdir_upload");
        // subdir must probably exist for FileOutputStream if parent dirs are not automatically created by code logic?
        // FileManagerServlet just does new FileOutputStream(file).
        // If the parent directory doesn't exist, FileOutputStream throws FileNotFoundException (in standard Java).
        // The current implementation of FileManagerServlet does NOT create parent directories.
        // So we must ensure it exists for the test to pass DO_PUT logic (unless we want to test that failure, but here we test
        // path security first).
        subDir.mkdirs();

        final String fileName = "subdir_upload/upload.txt";
        final File targetFile = new File(rootDir, fileName);

        when(request.getPathInfo()).thenReturn("/" + fileName);
        when(request.getInputStream()).thenReturn(new javax.servlet.ServletInputStream()
        {
            @Override
            public int read() throws IOException
            {
                return -1;
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
            public void setReadListener(final javax.servlet.ReadListener readListener)
            {
            }
        });

        servlet.doPut(request, response);

        verify(response).setStatus(HttpServletResponse.SC_OK);

        // Verify file was created (even if empty)
        if (!targetFile.exists())
        {
            throw new AssertionError("File should have been created");
        }
    }
}
