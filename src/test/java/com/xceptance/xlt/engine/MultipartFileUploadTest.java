package com.xceptance.xlt.engine;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.htmlunit.FormEncodingType;
import org.htmlunit.HttpMethod;
import org.htmlunit.WebRequest;
import org.htmlunit.WebResponse;
import org.htmlunit.util.KeyDataPair;
import org.htmlunit.util.NameValuePair;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.xceptance.xlt.AbstractXLTTestCase;
import com.xceptance.xlt.api.engine.Session;
import com.xceptance.xlt.util.XltPropertiesImpl;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;

@RunWith(JUnitParamsRunner.class)
public class MultipartFileUploadTest extends AbstractXLTTestCase
{
    private static Server localServer;
    private static String baseUrl;

    private static String receivedContentType;
    private static byte[] receivedBytes;

    @BeforeClass
    public static final void setUp() throws Exception
    {
        localServer = new Server(0);

        localServer.setHandler(new AbstractHandler()
        {
            @Override
            public void handle(final String target, final Request baseRequest, final HttpServletRequest request,
                               final HttpServletResponse response)
                throws IOException, ServletException
            {
                receivedContentType = request.getContentType();
                receivedBytes = IOUtils.toByteArray(request.getInputStream());

                response.setStatus(HttpStatus.OK_200);
                baseRequest.setHandled(true);
            }
        });

        localServer.start();
        baseUrl = localServer.getURI().toString();
    }

    @AfterClass
    public static final void tearDown() throws Exception
    {
        XltEngine.reset();
        SessionImpl.removeCurrent();

        localServer.stop();
        localServer.destroy();
    }

    @After
    public final void cleanUp() throws Exception
    {
        Session.getCurrent().clear();
        receivedContentType = null;
        receivedBytes = null;
    }

    @Test
    @Parameters(value = { "apache4", "okhttp3", "jdk" })
    public void testMultipartFileUpload(final String httpClientName) throws Exception
    {
        // choose the underlying HTTP client
        XltPropertiesImpl.getInstance().setProperty("com.xceptance.xlt.http.client", httpClientName);

        final WebRequest webRequest = new WebRequest(new URL(baseUrl + "/upload"), HttpMethod.POST);
        webRequest.setEncodingType(FormEncodingType.MULTIPART);

        // Required to avoid NPE inside HtmlUnit during encoding, need to specify parameters directly
        webRequest.setRequestParameters(new ArrayList<>());
        
        // Add a normal text field
        webRequest.getRequestParameters().add(new NameValuePair("username", "testuser"));

        // Add a file field
        final File tempFile = File.createTempFile("test-upload", ".txt");
        tempFile.deleteOnExit();
        try (FileOutputStream fos = new FileOutputStream(tempFile))
        {
            fos.write("hello multipart world".getBytes(StandardCharsets.UTF_8));
        }

        final KeyDataPair filePair = new KeyDataPair("document", tempFile, "upload.txt", "text/plain", "utf-8");
        webRequest.getRequestParameters().add(filePair);

        final WebResponse webResponse;
        try (XltWebClient webClient = new XltWebClient())
        {
            webClient.setTimerName("upload_test");
            webResponse = webClient.loadWebResponse(webRequest);
        }
        
        Assert.assertEquals(HttpStatus.OK_200, webResponse.getStatusCode());
        Assert.assertNotNull("Content type should be present", receivedContentType);
        Assert.assertTrue("Content type should be multipart/form-data for " + httpClientName, 
            receivedContentType.startsWith("multipart/form-data"));

        final String payload = new String(receivedBytes, StandardCharsets.UTF_8);
        Assert.assertTrue("Payload should contain the text field", payload.contains("name=\"username\""));
        Assert.assertTrue("Payload should contain the text field value", payload.contains("testuser"));
        
        Assert.assertTrue("Payload should contain the file field", payload.contains("name=\"document\""));
        Assert.assertTrue("Payload should contain the file name", payload.contains("filename=\"upload.txt\""));
        Assert.assertTrue("Payload should contain the file content", payload.contains("hello multipart world"));
    }
}
