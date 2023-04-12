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
package com.xceptance.xlt.engine.resultbrowser;

import java.awt.Desktop;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.URI;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.htmlunit.WebRequest;
import org.htmlunit.WebResponse;
import org.htmlunit.util.UrlUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.io.json.AbstractJsonWriter;
import com.thoughtworks.xstream.io.json.JsonHierarchicalStreamDriver;
import com.thoughtworks.xstream.io.json.JsonWriter;
import com.xceptance.common.xml.HtmlDomPrinter;
import com.xceptance.xlt.api.engine.Session;
import com.xceptance.xlt.api.htmlunit.LightWeightPage;
import com.xceptance.xlt.api.util.XltLogger;
import com.xceptance.xlt.api.util.XltProperties;
import com.xceptance.xlt.common.XltConstants;
import com.xceptance.xlt.engine.LightWeightPageImpl;
import com.xceptance.xlt.engine.SessionImpl;
import com.xceptance.xlt.engine.har.HarWriter;
import com.xceptance.xlt.engine.util.CssUtils;
import com.xceptance.xlt.engine.util.URLCleaner;

/**
 * Manager responsible for dumping all kind of requests.
 *
 * @author Hartmut Arlt (Xceptance Software Technologies GmbH)
 */
class DumpMgr
{
    private static final Logger LOG = LoggerFactory.getLogger(DumpMgr.class);

    /**
     * Known content types that denote HTML content.
     */
    private static final String[] HTML_CONTENT_TYPES =
        {
            "text/html", "application/xhtml+xml"
        };

    /**
     * Format used to construct the last page.
     */
    private static final String LAST_PAGE_FORMAT = "<!DOCTYPE html>\n" +
                                                   "<html><head><title>%s - XLT Result Browser</title></head><body style='margin:0; padding:0'><iframe src='%s/index.html' style='display:block; border:none; width:100vw; height:100vh;'></iframe></body></html>";

    /**
     * Maximum length of a file name.
     */
    private static final int FILENAME_LENGTH_LIMIT = 80;

    /**
     * Cache directory.
     */
    private File cacheDirectory;

    /**
     * Page directory.
     */
    private File pageDirectory;

    /**
     * Response directory.
     */
    private File responseDirectory;

    /**
     * Dump directory.
     */
    private File dumpDirectory;

    /**
     * Page counter.
     */
    private int pageCounter = 0;

    /**
     * Response counter.
     */
    private int responseCounter = 0;

    /**
     * Request data manager.
     */
    private RequestDataMgr dataMgr;

    /**
     * URL mapping used for URL rewriting.
     */
    private UrlMapping urlMapping;

    /**
     * Whether or not to additionally write an HAR file as part of the result browser dump.
     */
    private boolean harExportEnabled;

    /**
     * Constructor.
     */
    DumpMgr()
    {
        dataMgr = new RequestDataMgr();
        urlMapping = new CrcUrlMapping();
    }

    /**
     * Returns the request data manager.
     *
     * @return request data manager
     */
    public RequestDataMgr getRequestDataManager()
    {
        return dataMgr;
    }

    /**
     * Sets the new request data manager.
     *
     * @param requestDataMgr
     *            new request data manager
     */
    public void setRequestDataManager(final RequestDataMgr requestDataMgr)
    {
        if (requestDataMgr != null)
        {
            dataMgr = requestDataMgr;
        }
    }

    /**
     * Returns the URL mapping.
     *
     * @return URL mapping
     */
    public UrlMapping getUrlMapping()
    {
        return urlMapping;
    }

    /**
     * Sets the new URL mapping.
     *
     * @param mapping
     *            new URL mapping
     */
    public void setUrlMapping(final UrlMapping mapping)
    {
        if (mapping != null)
        {
            urlMapping = mapping;
        }
    }

    void setHarExportEnabled(final boolean enabled)
    {
        this.harExportEnabled = enabled;
    }

    /**
     * Returns the root directory to which responses as well as rendered HTML pages are dumped. If the directory does
     * not exist yet, it is created.
     *
     * @return the directory
     */
    private File getDumpDirectory()
    {
        if (dumpDirectory == null)
        {
            final SessionImpl session = SessionImpl.getCurrent();
            dumpDirectory = new File(new File(session.getResultsDirectory().toFile(), XltConstants.DUMP_OUTPUT_DIR), session.getID());
            dumpDirectory.mkdirs();
        }

        return dumpDirectory;
    }

    /**
     * Returns the cache directory.
     *
     * @return cache directory
     */
    private File getCacheDirectory()
    {
        if (cacheDirectory == null)
        {
            cacheDirectory = new File(getPagesDirectory(), XltConstants.DUMP_CACHE_DIR);
            cacheDirectory.mkdirs();
        }

        return cacheDirectory;

    }

    /**
     * Returns the page directory.
     *
     * @return page directory
     */
    private File getPagesDirectory()
    {
        if (pageDirectory == null)
        {
            pageDirectory = new File(getDumpDirectory(), XltConstants.DUMP_PAGES_DIR);
            pageDirectory.mkdirs();
        }

        return pageDirectory;

    }

    /**
     * Returns the response directory.
     *
     * @return response directory
     */
    private File getResponseDirectory()
    {
        if (responseDirectory == null)
        {
            responseDirectory = new File(getDumpDirectory(), XltConstants.DUMP_RESPONSES_DIR);
            responseDirectory.mkdirs();
        }

        return responseDirectory;
    }

    /**
     * Dumps the given page.
     *
     * @param page
     *            the page to be dumped
     */
    public void dump(final Page page)
    {
        if (page == null)
        {
            return;
        }

        String fileName = getDumpFileName(page.getName(), pageCounter++, true);

        // TODO: temporary hack
        if (page.isScreenshotPage())
        {
            fileName = fileName.replaceAll("\\.html$", ".png");
        }

        final File file = new File(getPagesDirectory(), fileName);

        dumpPage(page, file);

        dataMgr.pageDumped(fileName, page);
    }

    private static String getDumpFileName(String fileName, final int counter, final boolean isHtml)
    {
        // convert illegal characters
        fileName = com.xceptance.common.io.FileUtils.convertIllegalCharsInFileName(fileName);

        // shorten file name if necessary
        if (fileName.length() > FILENAME_LENGTH_LIMIT)
        {
            fileName = fileName.substring(0, FILENAME_LENGTH_LIMIT);
        }

        // build final name
        fileName = String.format("%04d-%s", counter, fileName);

        if (isHtml)
        {
            fileName += ".html";
        }

        return fileName;
    }

    /**
     * Dumps the given request.
     *
     * @param request
     *            the request to be dumped
     */
    public void dump(final Request request)
    {
        if (request == null)
        {
            return;
        }

        final String name = request.name;
        final WebRequest webRequest = request.webRequest;
        final WebResponse webResponse = request.webResponse;
        String fileName = null;

        if (webResponse == null || isHtmlContent(webResponse))
        {
            fileName = dumpHtmlContent(name, webResponse);
        }
        else
        {
            fileName = dumpStaticContent(name, webResponse);
            dumpStaticContentToCache(webRequest, webResponse);
        }

        dataMgr.requestDumped(fileName, request);

    }

    /**
     * Dumps the given pages and request to disk.
     *
     * @param pages
     *            the list of pages to be dumped
     * @param requests
     *            the list of requests to be dumped
     */
    public synchronized void dumpToDisk(final List<Page> pages, final List<Request> requests)
    {
        if (pages != null)
        {
            // dump the pages and their requests
            for (final Page page : pages)
            {
                for (final Request request : page.getRequests())
                {
                    dump(request);
                }

                dump(page);
            }
        }

        if (requests != null)
        {
            // dump any pending request as well
            for (final Request request : requests)
            {
                dump(request);
            }

        }

        // now dump the result browser stuff
        dumpJson();

        printAndOpenResultBrowserUrl();
    }

    /**
     * Dumps the JSON data file to disk.
     */
    private void dumpJson()
    {
        // generate the entry pages for the last failed test run
        final Session session = Session.getCurrent();
        if (!session.isLoadTest())
        {
            // do not (re-)generate the entry pages during load tests -> might cause problems in case of parallel users

            // first entry page
            final File lastRunFile = new File(getDumpDirectory(), "../last.html");
            final String lastRunFileContent = String.format(LAST_PAGE_FORMAT, session.getUserName(), session.getID());

            try
            {
                FileUtils.writeStringToFile(lastRunFile, lastRunFileContent, XltConstants.UTF8_ENCODING);
            }
            catch (final IOException e)
            {
                XltLogger.runTimeLogger.error("Failed to create file: " + lastRunFile, e);
            }

            // second entry page
            // be aware of the assumptions made on the directory layout
            final File shortcutFile = new File(getDumpDirectory(), "../../../../" + session.getUserName() + ".html");
            final String path = com.xceptance.common.io.FileUtils.computeRelativeUri(shortcutFile, dumpDirectory, false);
            final String shortcutFileContent = String.format(LAST_PAGE_FORMAT, session.getUserName(), path);

            try
            {
                FileUtils.writeStringToFile(shortcutFile, shortcutFileContent, XltConstants.UTF8_ENCODING);
            }
            catch (final IOException e)
            {
                XltLogger.runTimeLogger.error("Failed to create file: " + shortcutFile, e);
            }
        }

        // copy the result browser resources
        copyResources();

        generateJsonFile();
    }

    /**
     * Copies the result browser resources from the class path to the dump directory.
     */
    private void copyResources()
    {
        for (final String resource : resourcesToCopy())
        {
            final URL url = getClass().getResource("assets/" + resource);
            final File file = new File(getDumpDirectory(), resource);

            try
            {
                FileUtils.copyURLToFile(url, file);
            }
            catch (final Exception e)
            {
                XltLogger.runTimeLogger.error("Failed to copy resource file: " + resource, e);
            }
        }
    }

    /**
     * All resources to copy when HAR is off
     */
    private static final String[] RESOURCES =
    {
        "index.html"
    };
    /**
     * All resources to copy when HAR is on
     */
    private static final String[] RESOURCES_AND_HAR =
    {
        "index.html",
        "harviewer.html"
    };

    private String[] resourcesToCopy()
    {
        return harExportEnabled ? RESOURCES_AND_HAR : RESOURCES;
    }

    /**
     * Generates, encodes and writes the JSON data file to disk.
     */
    private void generateJsonFile()
    {
        final TransactionInfo txn = dataMgr.generateTransaction();
        generateResultBrowserData(txn);

        if (harExportEnabled)
        {
            generateHar(txn);
        }
    }

    private void generateResultBrowserData(final TransactionInfo transaction)
    {
        final File jsonFile = new File(getDumpDirectory(), "data.js");
        try (final OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(jsonFile), XltConstants.UTF8_ENCODING))
        {
            osw.write("var jsonData = ");

            final XStream xstream = new XStream(new JsonHierarchicalStreamDriver()
            {
                @Override
                public HierarchicalStreamWriter createWriter(final Writer writer)
                {
                    return new JsonWriter(writer, AbstractJsonWriter.DROP_ROOT_MODE);
                }
            });

            xstream.setMode(XStream.NO_REFERENCES);

            xstream.toXML(transaction, osw);
        }
        catch (final Exception e)
        {
            XltLogger.runTimeLogger.error("Failed to generate file: " + jsonFile, e);
        }
    }

    private void generateHar(final TransactionInfo transaction)
    {
        final File harFile = new File(getDumpDirectory(), "data.har");
        final HarWriter harWriter = new HarWriter();

        try
        {
            harWriter.writeHarLogToFile(new HarExporter(transaction).exportToHAR(), harFile);
        }
        catch (final Exception e)
        {
            XltLogger.runTimeLogger.error("Failed to generate HAR file: " + harFile, e);
        }
    }

    /**
     * Dumps the given page to the given file.
     *
     * @param page
     *            the page to be dumped
     * @param file
     *            the file to dump the given page to
     */
    private void dumpPage(final Page page, final File file)
    {
        if (page.isEmptyPage())
        {
            dumpEmptyPage(file);
        }
        else if (page.isScreenshotPage())
        {
            final byte[] image = page.getScreenshotPage();
            dumpScreenshotPage(image, file);
        }
        else if (page.isHtmlPage())
        {
            final PageDOMClone hPage = page.getHtmlPage();
            if (hPage != null)
            {
                dumpHtmlPage(hPage, file);
            }
        }
        else
        {
            dumpLWPage(page.getLightWeightPage(), file);
        }
    }

    /**
     * Dumps the passed page to the given file. All contained frame pages are dumped recursively.
     *
     * @param htmlPage
     *            the page to dump
     * @param file
     *            the target file
     */
    private void dumpHtmlPage(final PageDOMClone htmlPage, final File file)
    {
        try
        {
            final boolean outermost = !getCacheDirectory().equals(file.getParentFile());
            final Document document = new PageTransformer(htmlPage, outermost).transform(urlMapping);

            final String urlPrefix = (outermost ? XltConstants.DUMP_CACHE_DIR + "/" : StringUtils.EMPTY);

            // dump all frames on the page
            for (final Entry<Element, PageDOMClone> frameEntry : htmlPage.getFrames().entrySet())
            {
                final Element frameElement = frameEntry.getKey();
                final PageDOMClone framePage = frameEntry.getValue();

                final String fileName = com.xceptance.common.lang.StringUtils.crc32(RandomStringUtils.randomAlphanumeric(32)) + ".html";
                final File framePageFile = new File(getCacheDirectory(), fileName);

                dumpHtmlPage(framePage, framePageFile);

                // modify the "src" attribute of the frame element
                final String srcValue = urlPrefix + fileName;
                frameElement.setAttribute("src", srcValue);
            }

            // the document is complete now, dump it
            final String html = new HtmlDomPrinter().printNode(document);
            FileUtils.writeStringToFile(file, html, XltConstants.UTF8_ENCODING);
        }
        catch (final IOException e)
        {
            XltLogger.runTimeLogger.error("Failed to write HTML page to file: " + file.getAbsolutePath(), e);
        }
        catch (final Exception e)
        {
            XltLogger.runTimeLogger.error("Failed to dump HTML page", e);
        }
    }

    /**
     * Dumps the passed lightweight page to the given file.
     *
     * @param lwPage
     *            the lightweight page to be dumped
     * @param file
     *            destination file
     */
    private void dumpLWPage(final LightWeightPage lwPage, final File file)
    {
        // first of all, dump all frame pages recursively
        for (final Entry<String, LightWeightPage> frame : ((LightWeightPageImpl) lwPage).getFramePages().entrySet())
        {
            dumpLWPage(frame.getValue(), new File(getCacheDirectory(), frame.getKey() + ".html"));
        }

        try
        {
            FileUtils.writeStringToFile(file, new PageTransformer(lwPage).transformLW(urlMapping), XltConstants.UTF8_ENCODING);
        }
        catch (final IOException ioe)
        {
            XltLogger.runTimeLogger.error("Cannot write page to file: " + file.getAbsolutePath(), ioe);
        }
    }

    /**
     * Dumps the passed image data to the given file.
     *
     * @param image
     *            the image data
     * @param file
     *            destination file
     */
    private void dumpScreenshotPage(final byte[] image, final File file)
    {
        try
        {
            FileUtils.writeByteArrayToFile(file, image);
        }
        catch (final IOException ioe)
        {
            XltLogger.runTimeLogger.error("Cannot write page to file: " + file.getAbsolutePath(), ioe);
        }
    }

    /**
     * Dumps an empty page to the given file.
     *
     * @param file
     *            destination file
     */
    private void dumpEmptyPage(final File file)
    {
        try
        {
            FileUtils.writeByteArrayToFile(file, new byte[0]);
        }
        catch (final IOException ioe)
        {
            XltLogger.runTimeLogger.error("Cannot write page to file: " + file.getAbsolutePath(), ioe);
        }
    }

    /**
     * Checks whether the given web response carries HTML content.
     *
     * @param webResponse
     *            the web response to check
     * @return whether the content is HTML
     */
    private static boolean isHtmlContent(final WebResponse webResponse)
    {
        return ArrayUtils.contains(HTML_CONTENT_TYPES, webResponse.getContentType());
    }

    /**
     * Dumps a piece of HTML represented by the passed response to the dump directory.
     *
     * @param name
     *            the name to use
     * @param webResponse
     *            the response
     * @return the dump file name
     */
    private String dumpHtmlContent(final String name, final WebResponse webResponse)
    {
        // remove any trailing ".html" - it will be appended again
        final File file = new File(getResponseDirectory(),
                                   getDumpFileName(name.replaceAll("\\.(x)?htm(l)?$", StringUtils.EMPTY), responseCounter++, true));

        try (final Writer out = new OutputStreamWriter(new FileOutputStream(file), XltConstants.UTF8_ENCODING))
        {
            // write the response
            if (webResponse != null)
            {
                // HACK: use a LightWeightPage to get the right encoding
                final Charset charset = new LightWeightPage(webResponse, "dummy").getCharset();

                try (final Reader in = new InputStreamReader(webResponse.getContentAsStream(), charset))
                {
                    IOUtils.copy(in, out);
                }
            }
        }
        catch (final Exception e)
        {
            XltLogger.runTimeLogger.error("Cannot write page to file: " + file.getAbsolutePath(), e);
        }

        return file.getName();
    }

    /**
     * Dumps a piece of static content represented by the passed response to a file in the dump directory.
     *
     * @param name
     *            the name of the resource to write
     * @param webResponse
     *            the response to dump
     * @return the dump file name
     */
    private String dumpStaticContent(final String name, final WebResponse webResponse)
    {
        final File file = new File(getResponseDirectory(), getDumpFileName(name, responseCounter++, false));
        InputStream content = null;

        try
        {
            content = webResponse != null ? webResponse.getContentAsStream() : null;

            if (content != null)
            {
                FileUtils.copyInputStreamToFile(content, file);
            }
        }
        catch (final Exception e)
        {
            XltLogger.runTimeLogger.error("Cannot write static content to file: " + file.getAbsolutePath(), e);
        }
        finally
        {
            IOUtils.closeQuietly(content);
        }

        return file.getName();
    }

    /**
     * Dumps the static content enclosed in the given response to the cache.
     *
     * @param webRequest
     *            the request settings used to get the given response
     * @param webResponse
     *            the response to be dumped
     */
    private void dumpStaticContentToCache(final WebRequest webRequest, final WebResponse webResponse)
    {
        final URL url = URLCleaner.removeUserInfoIfNecessaryAsURL(webRequest.isRedirected() ? webRequest.getOriginalURL() : webRequest.getUrl());

        String fileName = urlMapping.map(url);

        // shorten file name if necessary
        if (fileName.length() > FILENAME_LENGTH_LIMIT)
        {
            fileName = fileName.substring(0, FILENAME_LENGTH_LIMIT);
        }

        if (fileName != null)
        {
            final File file = new File(getCacheDirectory(), fileName);

            if (file.exists() && webResponse.getStatusCode() == HttpStatus.SC_NOT_MODIFIED)
            {
                // don't let empty 304 responses overwrite existing cache entries
            }
            else
            {
                try (final InputStream content = rewriteResponseIfCss(url, webResponse))
                {
                    FileUtils.copyInputStreamToFile(content, file);
                }
                catch (final Exception e)
                {
                    XltLogger.runTimeLogger.error("Cannot write static content to cache: " + file.getAbsolutePath(), e);
                }
            }
        }
    }

    /**
     * Rewrites the content of the given response if its URL refers to a CSS file. Otherwise, the response's content
     * will be kept unmodified. Finally, the response's content will be returned as stream.
     *
     * @param baseURL
     *            the URL to use for resolving CSS url strings
     * @param response
     *            the response
     * @return content of response (rewritten or original) as stream
     * @throws IOException
     */
    private InputStream rewriteResponseIfCss(final URL baseURL, final WebResponse response) throws IOException
    {
        if (CssUtils.isCssResponse(response))
        {
            String responseData = response.getContentAsString();
            if (responseData != null)
            {
                final Collection<String> toBeReplaced = CssUtils.getUrlStrings(responseData);

                for (final String urlString : toBeReplaced)
                {
                    final String url = UrlUtils.resolveUrl(baseURL, urlString);
                    final String urlCheckSum = urlMapping.map(url);
                    if (null != urlCheckSum && urlCheckSum.length() > 0)
                    {
                        responseData = responseData.replace(urlString, urlCheckSum);
                    }
                }
            }

            return IOUtils.toInputStream(responseData, XltConstants.UTF8_ENCODING);
        }
        else
        {
            return response.getContentAsStream();
        }
    }

    /**
     * Prints the URL of the result browser just created to the console for easy copy&paste into a Web browser.
     * Additionally, the result browser will be opened in the default Web browser automatically if so configured in the
     * test suite settings.
     */
    private void printAndOpenResultBrowserUrl()
    {
        // only in dev mode
        if (XltProperties.getInstance().isDevMode())
        {
            try
            {
                final File indexFile = new File(getDumpDirectory(), "index.html").getCanonicalFile();

                // only if the result browser has been created successfully
                if (indexFile.isFile())
                {
                    final URI indexFileUri = indexFile.toURI();

                    // print the URL of the index file
                    System.out.printf("\n\nResult Browser:\n\t%s\n\n", indexFileUri);

                    // open the index file URL in the Web browser if so configured
                    final boolean openResultBrowser = XltProperties.getInstance().getProperty(XltConstants.XLT_PACKAGE_PATH +
                                                                                              ".results.openResultBrowser", false);
                    if (openResultBrowser)
                    {
                        Desktop.getDesktop().browse(indexFileUri);
                    }
                }
            }
            catch (final Exception e)
            {
                LOG.debug("Failed to determine/open the result browser URL", e);
            }
        }
    }

    /**
     * Resets this dump manager instance.
     */
    public synchronized void clear()
    {
        pageCounter = 0;
        responseCounter = 0;

        dumpDirectory = null;
        responseDirectory = null;
        pageDirectory = null;
        cacheDirectory = null;

        dataMgr.clear();
    }
}
