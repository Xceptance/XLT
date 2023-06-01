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
package com.xceptance.xlt.engine.resultbrowser;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.htmlunit.BrowserVersion;
import org.htmlunit.HttpMethod;
import org.htmlunit.MockWebConnection;
import org.htmlunit.StringWebResponse;
import org.htmlunit.WebClient;
import org.htmlunit.WebRequest;
import org.htmlunit.WebResponse;
import org.htmlunit.WebResponseData;
import org.htmlunit.html.HtmlPage;
import org.htmlunit.util.NameValuePair;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import com.xceptance.xlt.AbstractXLTTestCase;
import com.xceptance.xlt.api.engine.RequestData;
import com.xceptance.xlt.api.htmlunit.LightWeightPage;
import com.xceptance.xlt.api.util.XltProperties;
import com.xceptance.xlt.common.XltConstants;
import com.xceptance.xlt.engine.LightWeightPageImpl;
import com.xceptance.xlt.engine.XltWebClient;

/**
 * Tests the implementation of {@link DumpMgr}.
 *
 * @author Hartmut Arlt (Xceptance Software Technologies GmbH)
 */
public class DumpMgrTest extends AbstractXLTTestCase
{
    /**
     * The name format for HTML pages.
     */
    private static final String NAME_FORMAT = "%04d-%s.html";

    /**
     * The name format for screenshot pages.
     */
    private static final String NAME_FORMAT_SCREENSHOT_PAGE = "%04d-%s.png";

    /**
     * Test instance.
     */
    private DumpMgr instance;

    /**
     * Name of original result directory.
     */
    private static String originalResultDir;

    /**
     * Dummy HTML page content.
     */
    private static final String DUMMY_PAGE = "<html><head/><body>Test Page</body></html>";

    /**
     * Result directory used for tests.
     */
    private static final File resultDir = new File(getTempDir(), "results");

    /**
     * Test fixture setup.
     */
    @Before
    public void intro()
    {
        instance = new DumpMgr();
        instance.setRequestDataManager(Mockito.mock(RequestDataMgr.class));
    }

    /**
     * Static test fixture setup.
     */
    @BeforeClass
    public static void classIntro()
    {
        final XltProperties props = XltProperties.getInstance();
        originalResultDir = props.getProperty(XltConstants.XLT_PACKAGE_PATH + ".result-dir");

        props.setProperty(XltConstants.XLT_PACKAGE_PATH + ".result-dir", resultDir.getAbsolutePath());
    }

    /**
     * Static test fixture teardown.
     */
    @AfterClass
    public static void classOutro()
    {
        XltProperties.getInstance().setProperty(XltConstants.XLT_PACKAGE_PATH + ".result-dir", originalResultDir);
        FileUtils.deleteQuietly(resultDir);
    }

    /**
     * Tests the implementation of {@link DumpMgr#dump(Page)} by passing an invalid page.
     */
    @Test
    public void testDumpPage_PageNull() throws Throwable
    {
        instance.dump((Page) null);

        Mockito.verify(instance.getRequestDataManager(), Mockito.never()).pageDumped(ArgumentMatchers.anyString(), (Page) ArgumentMatchers.any());
    }

    /**
     * Tests the implementation of {@link DumpMgr#dump(Page)} by passing a valid page.
     */
    @Test
    public void testDumpPage() throws Throwable
    {
        final URL url = new URL("http://localhost");
        final WebResponse response = new StringWebResponse(DUMMY_PAGE, url);
        final LightWeightPage lwPage = new LightWeightPageImpl(response, "TimerName", null);
        final Page page = new Page(lwPage.getTimerName(), lwPage);

        instance.dump(page);

        final int pages = (Integer) getField("pageCounter", instance);
        final String fileName = String.format(NAME_FORMAT, pages - 1, page.getName());

        Mockito.verify(instance.getRequestDataManager(), Mockito.times(1)).pageDumped(ArgumentMatchers.eq(fileName), (Page) ArgumentMatchers.any());

        final File pageDirectory = (File) callMethod("getPagesDirectory", instance);
        Assert.assertNotNull(pageDirectory);

        Assert.assertTrue(Arrays.asList(pageDirectory.list()).contains(fileName));
    }

    /**
     * Ensures that each dumped frame document has a proper file extension.
     */
    @Test
    public void testDumpLWPage_ensureFrameHasFileExt() throws Throwable
    {
        final XltWebClient wc = Mockito.mock(XltWebClient.class);
        final LightWeightPage framePage;
        {
            final URL u = new URL("http://localhost/foo?bar=baz");
            framePage = new LightWeightPageImpl(new StringWebResponse(DUMMY_PAGE, u), "TimerName", wc);

        }
        final URL url = new URL("http://localhost");
        final WebResponse response = new StringWebResponse("<html><body><iframe name=\"frm\" src=\"http://localhost/foo?bar=baz\"></iframe></body></html>",
                                                           url);
        Mockito.when(wc.getLightWeightPage(ArgumentMatchers.<URL>any())).thenReturn(framePage);
        final LightWeightPage lwPage = new LightWeightPageImpl(response, "TimerName", wc);
        final Page page = new Page(lwPage.getTimerName(), lwPage);

        instance.dump(page);

        final int pages = (Integer) getField("pageCounter", instance);
        final String fileName = String.format(NAME_FORMAT, pages - 1, page.getName());

        Mockito.verify(instance.getRequestDataManager(), Mockito.times(1)).pageDumped(ArgumentMatchers.eq(fileName), ArgumentMatchers.eq(page));

        final File pageDirectory = (File) callMethod("getPagesDirectory", instance);
        Assert.assertNotNull(pageDirectory);

        Assert.assertTrue(Arrays.asList(pageDirectory.list()).contains(fileName));

        final File cacheDir = (File) callMethod("getCacheDirectory", instance);
        Assert.assertNotNull(cacheDir);
        Assert.assertTrue(Arrays.asList(cacheDir.list()).contains("frm.html"));
    }

    /**
     * Ensures that each dumped frame document has a proper file extension.
     */
    @Test
    public void testDumpPage_ensureFrameHasFileExt() throws Throwable
    {
        final MockWebConnection conn = new MockWebConnection();
        final URL frameUrl = new URL("http://localhost/foo?bar=baz");
        conn.setResponse(frameUrl, DUMMY_PAGE);
        conn.setResponse(new URL("http://localhost/"),
                         "<html><body><iframe name=\"frm\" src=\"http://localhost/foo?bar=baz\"></iframe></body></html>");

        try (final WebClient wc = new WebClient(BrowserVersion.getDefault()))
        {
            wc.setWebConnection(conn);
            wc.getOptions().setJavaScriptEnabled(false);

            final HtmlPage htmlPage = wc.getPage("http://localhost");
            final Page page = new Page("TimerName", htmlPage);

            instance.dump(page);

            final int pages = (Integer) getField("pageCounter", instance);
            final String fileName = String.format(NAME_FORMAT, pages - 1, page.getName());

            Mockito.verify(instance.getRequestDataManager(), Mockito.times(1)).pageDumped(ArgumentMatchers.eq(fileName), ArgumentMatchers.eq(page));

            final File pageDirectory = (File) callMethod("getPagesDirectory", instance);
            Assert.assertNotNull(pageDirectory);

            Assert.assertTrue(Arrays.asList(pageDirectory.list()).contains(fileName));

            final File cacheDir = (File) callMethod("getCacheDirectory", instance);
            Assert.assertNotNull(cacheDir);

            final List<String> fileList = Arrays.asList(cacheDir.list());
            for (final org.w3c.dom.Element frameE : page.getHtmlPage().getFrames().keySet())
            {
                final String frameDocPath = StringUtils.substringAfter(frameE.getAttribute("src"), "/");
                Assert.assertTrue("Frame's src attribute value does not end with '.html'", frameDocPath.endsWith(".html"));

                Assert.assertTrue("No such file: '" + frameDocPath + "'", fileList.contains(frameDocPath));
            }
        }
    }

    /**
     * Tests the implementation of {@link DumpMgr#dump(Page)} when passing a screenshot page.
     */
    @Test
    public void testDumpScreenshotPage() throws Throwable
    {
        final ActionInfo actionInfo = new ActionInfo();
        actionInfo.name = "ScreenshotPage";

        final byte[] bytes = RandomUtils.nextBytes(100);

        final Page page = new Page(actionInfo, bytes);

        instance.dump(page);

        final int pages = (Integer) getField("pageCounter", instance);
        final String fileName = String.format(NAME_FORMAT_SCREENSHOT_PAGE, pages - 1, page.getName());

        Mockito.verify(instance.getRequestDataManager(), Mockito.times(1)).pageDumped(ArgumentMatchers.eq(fileName), (Page) ArgumentMatchers.any());

        final File pageDirectory = (File) callMethod("getPagesDirectory", instance);
        Assert.assertNotNull(pageDirectory);

        Assert.assertTrue(Arrays.asList(pageDirectory.list()).contains(fileName));

        final File pageFile = new File(pageDirectory, fileName);
        Assert.assertArrayEquals(bytes, FileUtils.readFileToByteArray(pageFile));
    }

    /**
     * Tests the implementation of {@link DumpMgr#dump(Page)} when passing an empty page.
     */
    @Test
    public void testDumpEmptyPage() throws Throwable
    {
        final Page page = new Page("EmptyPage");

        instance.dump(page);

        final int pages = (Integer) getField("pageCounter", instance);
        final String fileName = String.format(NAME_FORMAT, pages - 1, page.getName());

        Mockito.verify(instance.getRequestDataManager(), Mockito.times(1)).pageDumped(ArgumentMatchers.eq(fileName), (Page) ArgumentMatchers.any());

        final File pageDirectory = (File) callMethod("getPagesDirectory", instance);
        Assert.assertNotNull(pageDirectory);

        Assert.assertTrue(Arrays.asList(pageDirectory.list()).contains(fileName));

        final File pageFile = new File(pageDirectory, fileName);
        Assert.assertEquals(0, pageFile.length());
    }

    /**
     * Tests the implementation of {@link DumpMgr#dump(Request)} by passing an invalid request.
     */
    @Test
    public void testDumpRequest_RequestNull() throws Throwable
    {
        instance.dump((Request) null);

        Mockito.verify(instance.getRequestDataManager(), Mockito.never()).requestDumped(ArgumentMatchers.anyString(),
                                                                                        (Request) ArgumentMatchers.any());
    }

    @Test
    public void testDumpRequest_RequestContentNull() throws Throwable
    {
        final List<NameValuePair> headers = Arrays.asList(new NameValuePair("Content-Type", "text/javascript"));
        final WebResponseData data = new MyData(HttpStatus.SC_OK, "OK", headers);
        final WebResponse response = new WebResponse(data, new URL("http://localhost/"), HttpMethod.GET, 100);
        final Request request = new Request("Any Name", response.getWebRequest(), response, new RequestData());

        instance.dump(request);

        final int responses = (Integer) getField("responseCounter", instance);
        final String fileName = String.format("%04d-%s", responses - 1, request.name);

        Mockito.verify(instance.getRequestDataManager()).requestDumped(ArgumentMatchers.eq(fileName), ArgumentMatchers.eq(request));

        final File responseDir = (File) callMethod("getResponseDirectory", instance);
        Assert.assertNotNull(responseDir);

        Assert.assertTrue(Arrays.asList(responseDir.list()).contains(fileName));
    }

    /**
     * Tests the implementation of {@link DumpMgr#dump(Request)} by passing a request whose response content is
     * gzip-encoded and ends prematurely. More specifically, this proves that at least the successfully read parts of
     * the response are written to file in case of I/O exceptions that occur when reading the response content. See
     * Redmine issue #2808 for details.
     */
    @Test
    public void testDumpRequest_PrematureEndOfGZIP() throws Throwable
    {
        final URL url = new URL("http://localhost");
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        final GZIPOutputStream gos = new GZIPOutputStream(bos);
        // we have to random strings since repetitions are eliminated by compression algorithm
        gos.write(("<html><body>" + RandomStringUtils.randomAlphanumeric(10000) + "</body></html>").getBytes(XltConstants.UTF8_ENCODING));
        gos.finish();

        final byte[] originalContent = bos.toByteArray();
        // IMPORTANT: we have to read in at least 8192 bytes (the default buffer size used by IOUtils)
        final byte[] content = new byte[8192];
        System.arraycopy(originalContent, 0, content, 0, Math.min(originalContent.length, content.length));

        final WebRequest _req = new WebRequest(url);
        final List<NameValuePair> headers = Arrays.asList(new NameValuePair("Content-Length", Integer.toString(content.length)),
                                                          new NameValuePair("Content-Encoding", "gzip"),
                                                          new NameValuePair("Content-Type",
                                                                            "text/html; charset=" + XltConstants.UTF8_ENCODING));
        final WebResponse response = new WebResponse(new WebResponseData(content, 200, "OK", headers), _req, 123L);
        final Request request = new Request("AnyName", _req, response, new RequestData());

        instance.dump(request);

        final int responses = (Integer) getField("responseCounter", instance);
        final String fileName = String.format(NAME_FORMAT, responses - 1, request.name);

        Mockito.verify(instance.getRequestDataManager()).requestDumped(ArgumentMatchers.eq(fileName), ArgumentMatchers.eq(request));

        final File responseDir = (File) callMethod("getResponseDirectory", instance);
        Assert.assertNotNull(responseDir);

        Assert.assertTrue(Arrays.asList(responseDir.list()).contains(fileName));

        final String actualDump = FileUtils.readFileToString(new File(responseDir, fileName), XltConstants.UTF8_ENCODING);
        Assert.assertFalse(actualDump.isEmpty());
    }

    /**
     * Tests the implementation of {@link DumpMgr#dump(Request)} by passing a valid request.
     */
    @Test
    public void testDumpRequest() throws Throwable
    {
        final URL url = new URL("http://localhost");
        final WebResponse response = new StringWebResponse(DUMMY_PAGE, url);
        final Request request = new Request("AnyName", new WebRequest(url), response, new RequestData());

        instance.dump(request);

        final int responses = (Integer) getField("responseCounter", instance);
        final String fileName = String.format(NAME_FORMAT, responses - 1, request.name);

        Mockito.verify(instance.getRequestDataManager()).requestDumped(ArgumentMatchers.eq(fileName), ArgumentMatchers.eq(request));

        final File responseDir = (File) callMethod("getResponseDirectory", instance);
        Assert.assertNotNull(responseDir);

        Assert.assertTrue(Arrays.asList(responseDir.list()).contains(fileName));
    }

    /**
     * Tests the implementation of {@link DumpMgr#dumpToDisk(List, List)} by passing a null reference as action and/or
     * as request list parameter.
     */
    @Test
    public void testDumpToDisk_ActionsAndOrRequestNull() throws Throwable
    {
        instance.dumpToDisk(null, new ArrayList<Request>());

        Mockito.verify(instance.getRequestDataManager(), Mockito.never()).requestDumped(ArgumentMatchers.anyString(),
                                                                                        (Request) ArgumentMatchers.any());
        Mockito.verify(instance.getRequestDataManager(), Mockito.never()).pageDumped(ArgumentMatchers.anyString(), (Page) ArgumentMatchers.any());

        instance.dumpToDisk(new ArrayList<Page>(), null);

        Mockito.verify(instance.getRequestDataManager(), Mockito.never()).requestDumped(ArgumentMatchers.anyString(),
                                                                                        (Request) ArgumentMatchers.any());
        Mockito.verify(instance.getRequestDataManager(), Mockito.never()).pageDumped(ArgumentMatchers.anyString(), (Page) ArgumentMatchers.any());

        instance.dumpToDisk(null, null);

        Mockito.verify(instance.getRequestDataManager(), Mockito.never()).requestDumped(ArgumentMatchers.anyString(),
                                                                                        (Request) ArgumentMatchers.any());
        Mockito.verify(instance.getRequestDataManager(), Mockito.never()).pageDumped(ArgumentMatchers.anyString(), (Page) ArgumentMatchers.any());
    }

    /**
     * Tests the implementation of {@link DumpMgr#dumpToDisk(List, List)}.
     */
    @Test
    public void testDumpToDisk() throws Throwable
    {
        final URL url = new URL("http://localhost");
        final WebResponse response = new StringWebResponse(DUMMY_PAGE, url);
        final LightWeightPage lwPage = new LightWeightPageImpl(response, "TimerName", null);
        final Page page = new Page(lwPage.getTimerName(), lwPage);
        final Request request = new Request("AnyName", new WebRequest(url), response, new RequestData());
        page.getRequests().add(request);

        final List<Page> pages = new ArrayList<Page>();
        pages.add(page);
        final List<Request> requests = new ArrayList<Request>();
        requests.add(request);

        instance.dumpToDisk(pages, requests);

        // assemble file names for requests
        final int responses = (Integer) getField("responseCounter", instance);
        final String fileName1 = String.format(NAME_FORMAT, responses - 2, request.name);
        final String fileName2 = String.format(NAME_FORMAT, responses - 1, request.name);

        // verify invocations of request data manager mock
        Mockito.verify(instance.getRequestDataManager(), Mockito.times(1)).requestDumped(fileName1, request);
        Mockito.verify(instance.getRequestDataManager(), Mockito.times(1)).requestDumped(fileName2, request);

        // assemble file name of dumped page
        final int dumpedPages = (Integer) getField("pageCounter", instance);
        final String fileName = String.format(NAME_FORMAT, dumpedPages - 1, page.getName());

        // verify invocation of request data manager mock
        Mockito.verify(instance.getRequestDataManager(), Mockito.times(1)).pageDumped(fileName, page);

        // get dump directory
        final File dumpDirectory = (File) callMethod("getDumpDirectory", instance);
        Assert.assertNotNull(dumpDirectory);

        Assert.assertTrue(Arrays.asList(dumpDirectory.getParentFile().list()).contains("last.html"));
        // make sure that the dump directory contains the files 'index.html' and 'data.js'
        final List<String> fileNames = Arrays.asList(dumpDirectory.list());
        Assert.assertTrue("Resultbrowser index file was not dumped", fileNames.contains("index.html"));
        Assert.assertTrue("Resultbrowser's JSON data file was not dumped", fileNames.contains("data.js"));
        
    }

    @Test
    public void testDumpRequest_BinaryContent() throws Throwable
    {
        final URL url = new URL("http://localhost/MyClass.class");
        final String className = "/" + getClass().getName().replace('.', '/') + ".class";
        final byte[] content = FileUtils.readFileToByteArray(new File(getClass().getResource(className).toURI()));
        final WebRequest webRequest = new WebRequest(url);
        final List<NameValuePair> responseHeaders = Arrays.asList(new NameValuePair("Content-Type", "application/java-vm"));
        final WebResponse response = new WebResponse(new WebResponseData(content, 200, "OK", responseHeaders), webRequest, 378L);
        final Request request = new Request("AnyName.class", webRequest, response, new RequestData());

        instance.dump(request);

        final int responses = (Integer) getField("responseCounter", instance);
        final String fileName = String.format("%04d-%s", responses - 1, request.name);

        Mockito.verify(instance.getRequestDataManager()).requestDumped(ArgumentMatchers.eq(fileName), ArgumentMatchers.eq(request));

        final File responseDir = (File) callMethod("getResponseDirectory", instance);
        Assert.assertNotNull(responseDir);

        Assert.assertTrue(Arrays.asList(responseDir.list()).contains(fileName));

        final File responseFile = new File(responseDir, fileName);
        final byte[] fileContent = FileUtils.readFileToByteArray(responseFile);
        Assert.assertArrayEquals(content, fileContent);
    }

    @Test
    public void testDumpRequest_NotModified() throws Throwable
    {
        /*
         * We want to simulate the case that the response for the given URL is already in cache and the server returns a
         * 304 for any subsequent request to same URL.
         */

        // (1) Build request and response
        final URL url = new URL("http://localhost/some/path/to/file.html");
        final WebRequest webRequest = new WebRequest(url);
        final String responseContent = "Some Test String";
        final WebResponse webResponse = new WebResponse(new WebResponseData(responseContent.getBytes(), HttpStatus.SC_NOT_MODIFIED,
                                                                            "Not Modified", Collections.<NameValuePair>emptyList()),
                                                        webRequest, 123L);
        final Request request = new Request("AnyName", webRequest, webResponse, new RequestData());

        // (2) Make sure that "real" response of URL is in cache
        final Method m = DumpMgr.class.getDeclaredMethod("getCacheDirectory");
        m.setAccessible(true);

        final File cacheDir = (File) m.invoke(instance);

        String fileName = instance.getUrlMapping().map(url);
        if (fileName.length() > 240)
        {
            fileName = fileName.substring(0, 240);
        }

        final File f = new File(cacheDir, fileName);
        f.createNewFile();

        try
        {
            // (3) Finally, dump our "304 Not Modified" response
            instance.dump(request);
            // ... and make sure that cached response has not been overwritten
            Assert.assertNotEquals("Cached response was overwritten", responseContent,
                                   FileUtils.readFileToString(f, XltConstants.UTF8_ENCODING));
        }
        finally
        {
            FileUtils.deleteQuietly(f);
        }
    }

    private static final class MyData extends WebResponseData
    {
        private static final long serialVersionUID = 1L;

        public MyData(final int responseCode, final String responseMsg, final List<NameValuePair> headers) throws IOException
        {
            super(responseCode, responseMsg, headers);
        }

        @Override
        public byte[] getBody()
        {
            return null;
        }
    }
}
