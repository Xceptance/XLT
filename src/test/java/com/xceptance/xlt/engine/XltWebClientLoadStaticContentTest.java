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
package com.xceptance.xlt.engine;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.gargoylesoftware.htmlunit.MockWebConnection;
import com.xceptance.xlt.AbstractXLTTestCase;
import com.xceptance.xlt.XltMockWebConnection;
import com.xceptance.xlt.api.actions.AbstractHtmlPageAction;
import com.xceptance.xlt.api.util.XltProperties;
import com.xceptance.xlt.engine.XltWebClientTest.URLCollector;
import com.xceptance.xlt.util.XltPropertiesImpl;

public class XltWebClientLoadStaticContentTest extends AbstractXLTTestCase
{
    // test URLs
    URL urlHtml;

    URL urlCssTest;

    URL urlCssPrint;

    URL urlCssScreen;

    URL urlCssSpeaker;

    URL urlCssAll;

    URL urlImg;

    URL urlImgScreen;

    URL urlImgPrint;

    URL urlImgSpeaker;

    URL urlImgAll;

    URL urlJs;

    // stores the requested URLs
    URLCollector collector = new URLCollector();

    @AfterClass
    public static void afterClass()
    {
        // clean-up
        XltPropertiesImpl.reset();
        SessionImpl.removeCurrent();
    }

    @Before
    public void setUp()
    {
        // reset the stored URLs
        collector.clear();
        // reset the test specific load parameter
        final XltProperties props = XltProperties.getInstance();
        props.setProperty("com.xceptance.xlt.loadStaticContent", "");
        props.setProperty("com.xceptance.xlt.cssEnabled", "");
        props.setProperty("com.xceptance.xlt.javaScriptEnabled", "");
        props.setProperty("com.xceptance.xlt.css.download.images", "");
    }

    // ======================================================
    // all static content is requested by the html page
    // ======================================================

    @Test
    public void testDefaultSettingLoadNoStaticContent()
    {
        // start an open page action
        startAllLoadedByHtml();

        // check, if the right URLs were requested
        Assert.assertEquals(1, collector.getUrls().size());
        Assert.assertTrue("Didn't load html page!", collector.getUrls().contains(urlHtml));
    }

    @Test
    public void testDefaultSettingLoadCss()
    {
        // set the test specific load parameter
        final XltProperties props = XltProperties.getInstance();
        props.setProperty("com.xceptance.xlt.cssEnabled", "true");

        // start an open page action
        startAllLoadedByHtml();

        // check, if the right URLs were requested
        Assert.assertEquals(2, collector.getUrls().size());
        Assert.assertTrue("Didn't load html page!", collector.getUrls().contains(urlHtml));
        Assert.assertTrue("Didn't load css!", collector.getUrls().contains(urlCssTest));
    }

    @Test
    public void testDefaultSettingLoadJs()
    {
        // set the test specific load parameter
        final XltProperties props = XltProperties.getInstance();
        props.setProperty("com.xceptance.xlt.javaScriptEnabled", "true");

        // start an open page action
        startAllLoadedByHtml();

        // check, if the right URLs were requested
        Assert.assertEquals(2, collector.getUrls().size());
        Assert.assertTrue("Didn't load html page!", collector.getUrls().contains(urlHtml));
        Assert.assertTrue("Didn't load java script!", collector.getUrls().contains(urlJs));
    }

    @Test
    public void testLoadAllStaticContent()
    {
        // set the test specific load parameter
        final XltProperties props = XltProperties.getInstance();
        props.setProperty("com.xceptance.xlt.loadStaticContent", "true");
        props.setProperty("com.xceptance.xlt.cssEnabled", "true");
        props.setProperty("com.xceptance.xlt.javaScriptEnabled", "true");

        // start an open page action
        startAllLoadedByHtml();

        // check, if the right URLs were requested
        Assert.assertEquals(4, collector.getUrls().size());
        Assert.assertTrue("Didn't load html page!", collector.getUrls().contains(urlHtml));
        Assert.assertTrue("Didn't load css!", collector.getUrls().contains(urlCssTest));
        Assert.assertTrue("Didn't load image!", collector.getUrls().contains(urlImg));
        Assert.assertTrue("Didn't load java script!", collector.getUrls().contains(urlJs));
    }

    @Test
    public void testLoadAllStaticContentButNoJsAndCss()
    {
        // set the test specific load parameter
        final XltProperties props = XltProperties.getInstance();
        props.setProperty("com.xceptance.xlt.loadStaticContent", "true");
        props.setProperty("com.xceptance.xlt.cssEnabled", "false");
        props.setProperty("com.xceptance.xlt.javaScriptEnabled", "false");

        // start an open page action
        startAllLoadedByHtml();

        // check, if the right URLs were requested
        Assert.assertEquals(4, collector.getUrls().size());
        Assert.assertTrue("Didn't load html page!", collector.getUrls().contains(urlHtml));
        Assert.assertTrue("Didn't load css!", collector.getUrls().contains(urlCssTest));
        Assert.assertTrue("Didn't load image!", collector.getUrls().contains(urlImg));
        Assert.assertTrue("Didn't load java script!", collector.getUrls().contains(urlJs));
    }

    @Test
    public void testLoadAllStaticContentButNoJs()
    {
        // set the test specific load parameter
        final XltProperties props = XltProperties.getInstance();
        props.setProperty("com.xceptance.xlt.loadStaticContent", "true");
        props.setProperty("com.xceptance.xlt.cssEnabled", "true");
        props.setProperty("com.xceptance.xlt.javaScriptEnabled", "false");

        // start an open page action
        startAllLoadedByHtml();

        // check, if the right URLs were requested
        Assert.assertEquals(4, collector.getUrls().size());
        Assert.assertTrue("Didn't load html page!", collector.getUrls().contains(urlHtml));
        Assert.assertTrue("Didn't load css!", collector.getUrls().contains(urlCssTest));
        Assert.assertTrue("Didn't load image!", collector.getUrls().contains(urlImg));
        Assert.assertTrue("Didn't load java script!", collector.getUrls().contains(urlJs));
    }

    @Test
    public void testLoadAllStaticContentButNoCss()
    {
        // set the test specific load parameter
        final XltProperties props = XltProperties.getInstance();
        props.setProperty("com.xceptance.xlt.loadStaticContent", "true");
        props.setProperty("com.xceptance.xlt.cssEnabled", "false");
        props.setProperty("com.xceptance.xlt.javaScriptEnabled", "true");

        // start an open page action
        startAllLoadedByHtml();

        // check, if the right URLs were requested
        Assert.assertEquals(4, collector.getUrls().size());
        Assert.assertTrue("Didn't load html page!", collector.getUrls().contains(urlHtml));
        Assert.assertTrue("Didn't load css!", collector.getUrls().contains(urlCssTest));
        Assert.assertTrue("Didn't load image!", collector.getUrls().contains(urlImg));
        Assert.assertTrue("Didn't load java script!", collector.getUrls().contains(urlJs));
    }

    @Test
    public void testLoadNoStaticContent()
    {
        // set the test specific load parameter
        final XltProperties props = XltProperties.getInstance();
        props.setProperty("com.xceptance.xlt.loadStaticContent", "false");
        props.setProperty("com.xceptance.xlt.cssEnabled", "false");
        props.setProperty("com.xceptance.xlt.javaScriptEnabled", "false");

        // start an open page action
        startAllLoadedByHtml();

        // check, if the right URLs were requested
        Assert.assertEquals(1, collector.getUrls().size());
        Assert.assertTrue("Didn't load html page!", collector.getUrls().contains(urlHtml));
    }

    @Test
    public void testLoadNoStaticContentButCssAndJs()
    {
        // set the test specific load parameter
        final XltProperties props = XltProperties.getInstance();
        props.setProperty("com.xceptance.xlt.loadStaticContent", "false");
        props.setProperty("com.xceptance.xlt.cssEnabled", "true");
        props.setProperty("com.xceptance.xlt.javaScriptEnabled", "true");

        // start an open page action
        startAllLoadedByHtml();

        // check, if the right URLs were requested
        Assert.assertEquals(3, collector.getUrls().size());
        Assert.assertTrue("Didn't load html page!", collector.getUrls().contains(urlHtml));
        Assert.assertTrue("Didn't load css!", collector.getUrls().contains(urlCssTest));
        Assert.assertTrue("Didn't load java script!", collector.getUrls().contains(urlJs));
    }

    @Test
    public void testLoadNoStaticContentButCss()
    {
        // set the test specific load parameter
        final XltProperties props = XltProperties.getInstance();
        props.setProperty("com.xceptance.xlt.loadStaticContent", "false");
        props.setProperty("com.xceptance.xlt.cssEnabled", "true");
        props.setProperty("com.xceptance.xlt.javaScriptEnabled", "false");

        // start an open page action
        startAllLoadedByHtml();

        // check, if the right URLs were requested
        Assert.assertEquals(2, collector.getUrls().size());
        Assert.assertTrue("Didn't load html page!", collector.getUrls().contains(urlHtml));
        Assert.assertTrue("Didn't load css!", collector.getUrls().contains(urlCssTest));
    }

    @Test
    public void testLoadNoStaticContentButJs()
    {
        // set the test specific load parameter
        final XltProperties props = XltProperties.getInstance();
        props.setProperty("com.xceptance.xlt.loadStaticContent", "false");
        props.setProperty("com.xceptance.xlt.cssEnabled", "false");
        props.setProperty("com.xceptance.xlt.javaScriptEnabled", "true");

        // start an open page action
        startAllLoadedByHtml();

        // check, if the right URLs were requested
        Assert.assertEquals(2, collector.getUrls().size());
        Assert.assertTrue("Didn't load html page!", collector.getUrls().contains(urlHtml));
        Assert.assertTrue("Didn't load java script!", collector.getUrls().contains(urlJs));
    }

    // ======================================================
    // some static content is requested recursive
    // by a css file
    // ======================================================

    @Test
    public void testLoadAllStaticContentRequestedByCss()
    {
        // set the test specific load parameter
        final XltProperties props = XltProperties.getInstance();
        props.setProperty("com.xceptance.xlt.loadStaticContent", "true");
        props.setProperty("com.xceptance.xlt.cssEnabled", "true");
        props.setProperty("com.xceptance.xlt.css.download.images", "always");

        // start an open page action
        startCssLoadsImage();

        // check, if the right URLs were requested
        Assert.assertEquals(4, collector.getUrls().size());
        Assert.assertTrue("Didn't load html page!", collector.getUrls().contains(urlHtml));
        Assert.assertTrue("Didn't load css!", collector.getUrls().contains(urlCssTest));
        Assert.assertTrue("Didn't load java script!", collector.getUrls().contains(urlJs));
        Assert.assertTrue("Didn't load image!", collector.getUrls().contains(urlImg));
    }

    @Test
    public void testLoadAllStaticContentButNoCssRequestedByCss()
    {
        // set the test specific load parameter
        final XltProperties props = XltProperties.getInstance();
        props.setProperty("com.xceptance.xlt.loadStaticContent", "true");
        props.setProperty("com.xceptance.xlt.cssEnabled", "false");
        props.setProperty("com.xceptance.xlt.css.download.images", "always");

        // start an open page action
        startCssLoadsImage();

        // check, if the right URLs were requested
        Assert.assertEquals(4, collector.getUrls().size());
        Assert.assertTrue("Didn't load html page!", collector.getUrls().contains(urlHtml));
        Assert.assertTrue("Didn't load css!", collector.getUrls().contains(urlCssTest));
        Assert.assertTrue("Didn't load java script!", collector.getUrls().contains(urlJs));
        Assert.assertTrue("Didn't load image!", collector.getUrls().contains(urlImg));
    }

    @Test
    public void testLoadAllStaticContentButNoImagesByCssRequestedByCss()
    {
        // set the test specific load parameter
        final XltProperties props = XltProperties.getInstance();
        props.setProperty("com.xceptance.xlt.loadStaticContent", "true");
        props.setProperty("com.xceptance.xlt.cssEnabled", "true");
        props.setProperty("com.xceptance.xlt.css.download.images", "never");

        // start an open page action
        startCssLoadsImage();

        // check, if the right URLs were requested
        Assert.assertEquals(3, collector.getUrls().size());
        Assert.assertTrue("Didn't load html page!", collector.getUrls().contains(urlHtml));
        Assert.assertTrue("Didn't load css!", collector.getUrls().contains(urlCssTest));
        Assert.assertTrue("Didn't load java script!", collector.getUrls().contains(urlJs));
    }

    @Test
    public void testLoadNoStaticContentButCssRequestedByCss()
    {
        // set the test specific load parameter
        final XltProperties props = XltProperties.getInstance();
        props.setProperty("com.xceptance.xlt.loadStaticContent", "false");
        props.setProperty("com.xceptance.xlt.cssEnabled", "true");
        props.setProperty("com.xceptance.xlt.css.download.images", "always");

        // start an open page action
        startCssLoadsImage();

        // check, if the right URLs were requested
        Assert.assertEquals(2, collector.getUrls().size());
        Assert.assertTrue("Didn't load html page!", collector.getUrls().contains(urlHtml));
        Assert.assertTrue("Didn't load css!", collector.getUrls().contains(urlCssTest));
    }

    // ======================================================
    // some static content is requested recursive
    // by a java script file
    // ======================================================

    @Test
    public void testLoadAllStaticContentRequestedByJs()
    {
        // set the test specific load parameter
        final XltProperties props = XltProperties.getInstance();
        props.setProperty("com.xceptance.xlt.loadStaticContent", "true");
        props.setProperty("com.xceptance.xlt.cssEnabled", "true");
        props.setProperty("com.xceptance.xlt.javaScriptEnabled", "true");

        // start an open page action
        startJsLoadCss();

        // check, if the right URLs were requested
        Assert.assertEquals(3, collector.getUrls().size());
        Assert.assertTrue("Didn't load html page!", collector.getUrls().contains(urlHtml));
        Assert.assertTrue("Didn't load css!", collector.getUrls().contains(urlCssTest));
        Assert.assertTrue("Didn't load java script!", collector.getUrls().contains(urlJs));
    }

    // ======================================================
    // different css files will be loaded according to
    // the current media type
    // ======================================================

    @Ignore("#1702")
    @Test
    public void testDifferentMediaTypesLoadImagesAlwaysHTMLSyntax()
    {
        // set the test specific load parameter
        final XltProperties props = XltProperties.getInstance();
        props.setProperty("com.xceptance.xlt.loadStaticContent", "true");
        props.setProperty("com.xceptance.xlt.cssEnabled", "true");
        props.setProperty("com.xceptance.xlt.css.download.images", "always");

        // start an open page action
        startDifferentMediaTypes("testWebSites/testWebSiteMediaTypes.html");

        // check, if the right URLs were requested
        Assert.assertEquals(11, collector.getUrls().size());
        Assert.assertTrue("Didn't load html page!", collector.getUrls().contains(urlHtml));
        // all css files should be loaded
        Assert.assertTrue("Didn't load test.css!", collector.getUrls().contains(urlCssTest));
        Assert.assertTrue("Didn't load screen.css!", collector.getUrls().contains(urlCssScreen));
        Assert.assertTrue("Didn't load print.css!", collector.getUrls().contains(urlCssPrint));
        Assert.assertTrue("Didn't load speaker.css!", collector.getUrls().contains(urlCssSpeaker));
        Assert.assertTrue("Didn't load all.css!", collector.getUrls().contains(urlCssAll));
        // all images imported by css should be executed, because the setting is 'always'
        Assert.assertTrue("Didn't load test.gif!", collector.getUrls().contains(urlImg));
        Assert.assertTrue("Didn't load screen.gif!", collector.getUrls().contains(urlImgScreen));
        Assert.assertTrue("Didn't load all.gif!", collector.getUrls().contains(urlImgAll));
        Assert.assertTrue("Didn't load print.gif!", collector.getUrls().contains(urlImgPrint));
        Assert.assertTrue("Didn't load speaker.gif!", collector.getUrls().contains(urlImgSpeaker));
    }

    @Test
    public void testDifferentMediaTypesLoadImagesAlwaysCssSyntax()
    {
        // set the test specific load parameter
        final XltProperties props = XltProperties.getInstance();
        props.setProperty("com.xceptance.xlt.loadStaticContent", "true");
        props.setProperty("com.xceptance.xlt.cssEnabled", "true");
        props.setProperty("com.xceptance.xlt.css.download.images", "always");

        // start an open page action
        startDifferentMediaTypes("testWebSites/testWebSiteMediaTypesCssSyntax.html");

        // check, if the right URLs were requested
        Assert.assertEquals(11, collector.getUrls().size());
        Assert.assertTrue("Didn't load html page!", collector.getUrls().contains(urlHtml));
        // all css files should be loaded
        Assert.assertTrue("Didn't load test.css!", collector.getUrls().contains(urlCssTest));
        Assert.assertTrue("Didn't load screen.css!", collector.getUrls().contains(urlCssScreen));
        Assert.assertTrue("Didn't load print.css!", collector.getUrls().contains(urlCssPrint));
        Assert.assertTrue("Didn't load speaker.css!", collector.getUrls().contains(urlCssSpeaker));
        Assert.assertTrue("Didn't load all.css!", collector.getUrls().contains(urlCssAll));
        // all images imported by css should be executed, because the setting is 'always'
        Assert.assertTrue("Didn't load test.gif!", collector.getUrls().contains(urlImg));
        Assert.assertTrue("Didn't load screen.gif!", collector.getUrls().contains(urlImgScreen));
        Assert.assertTrue("Didn't load all.gif!", collector.getUrls().contains(urlImgAll));
        Assert.assertTrue("Didn't load print.gif!", collector.getUrls().contains(urlImgPrint));
        Assert.assertTrue("Didn't load speaker.gif!", collector.getUrls().contains(urlImgSpeaker));
    }

    @Test
    public void testDifferentMediaTypesLoadImagesAlwaysWithAtImport()
    {
        // set the test specific load parameter
        final XltProperties props = XltProperties.getInstance();
        props.setProperty("com.xceptance.xlt.loadStaticContent", "true");
        props.setProperty("com.xceptance.xlt.cssEnabled", "true");
        props.setProperty("com.xceptance.xlt.css.download.images", "always");

        // start an open page action
        startDifferentMediaTypesAndRecursiveCss("@import url(\"print.css\") print; @import \"screen.css\" screen; " +
                                                "@import url(\"speaker.css\") speaker; @import \"all.css\" all;");

        // check, if the right URLs were requested
        Assert.assertEquals(10, collector.getUrls().size());
        Assert.assertTrue("Didn't load html page!", collector.getUrls().contains(urlHtml));
        // all css files should be loaded
        Assert.assertTrue("Didn't load test.css!", collector.getUrls().contains(urlCssTest));
        Assert.assertTrue("Didn't load screen.css!", collector.getUrls().contains(urlCssScreen));
        Assert.assertTrue("Didn't load print.css!", collector.getUrls().contains(urlCssPrint));
        Assert.assertTrue("Didn't load speaker.css!", collector.getUrls().contains(urlCssSpeaker));
        Assert.assertTrue("Didn't load all.css!", collector.getUrls().contains(urlCssAll));
        // all images imported by css should be executed, because the setting is 'always'
        Assert.assertTrue("Didn't load screen.gif!", collector.getUrls().contains(urlImgScreen));
        Assert.assertTrue("Didn't load all.gif!", collector.getUrls().contains(urlImgAll));
        Assert.assertTrue("Didn't load print.gif!", collector.getUrls().contains(urlImgPrint));
        Assert.assertTrue("Didn't load speaker.gif!", collector.getUrls().contains(urlImgSpeaker));
    }

    @Test
    public void testDifferentMediaTypesLoadImagesAlwaysWithAtMedia()
    {
        // set the test specific load parameter
        final XltProperties props = XltProperties.getInstance();
        props.setProperty("com.xceptance.xlt.loadStaticContent", "true");
        props.setProperty("com.xceptance.xlt.cssEnabled", "true");
        props.setProperty("com.xceptance.xlt.css.download.images", "always");

        // start an open page action
        startDifferentMediaTypesAndRecursiveCss("@media screen {#background{background-image: url(\"screen.gif\");}} " +
                                                "@media all {#background{background-image: url(\"all.gif\");}} " +
                                                "@media speaker {#background{background-image: url(\"speaker.gif\");}} " +
                                                "@media print {#background{background-image: url(\"print.gif\");}}");

        // check, if the right URLs were requested
        Assert.assertEquals(6, collector.getUrls().size());
        Assert.assertTrue("Didn't load html page!", collector.getUrls().contains(urlHtml));
        // all css files should be loaded
        Assert.assertTrue("Didn't load test.css!", collector.getUrls().contains(urlCssTest));
        // all images imported by css should be executed, because the setting is 'always'
        Assert.assertTrue("Didn't load screen.gif!", collector.getUrls().contains(urlImgScreen));
        Assert.assertTrue("Didn't load all.gif!", collector.getUrls().contains(urlImgAll));
        Assert.assertTrue("Didn't load print.gif!", collector.getUrls().contains(urlImgPrint));
        Assert.assertTrue("Didn't load speaker.gif!", collector.getUrls().contains(urlImgSpeaker));
    }

    @Test
    public void testDifferentMediaTypesLoadImagesOnDemandHTMLSyntax()
    {
        // set the test specific load parameter
        final XltProperties props = XltProperties.getInstance();
        props.setProperty("com.xceptance.xlt.loadStaticContent", "true");
        props.setProperty("com.xceptance.xlt.cssEnabled", "true");
        props.setProperty("com.xceptance.xlt.css.download.images", "ondemand");

        // start an open page action
        startDifferentMediaTypes("testWebSites/testWebSiteMediaTypes.html");

        // check, if the right URLs were requested
        Assert.assertEquals(9, collector.getUrls().size());
        Assert.assertTrue("Didn't load html page!", collector.getUrls().contains(urlHtml));
        // all css files should be loaded
        Assert.assertTrue("Didn't load test.css!", collector.getUrls().contains(urlCssTest));
        Assert.assertTrue("Didn't load screen.css!", collector.getUrls().contains(urlCssScreen));
        Assert.assertTrue("Didn't load print.css!", collector.getUrls().contains(urlCssPrint));
        Assert.assertTrue("Didn't load speaker.css!", collector.getUrls().contains(urlCssSpeaker));
        Assert.assertTrue("Didn't load all.css!", collector.getUrls().contains(urlCssAll));
        // just css files with no media type, with the media type all or screen should be executed
        Assert.assertTrue("Didn't load test.gif!", collector.getUrls().contains(urlImg));
        Assert.assertTrue("Didn't load screen.gif!", collector.getUrls().contains(urlImgScreen));
        Assert.assertTrue("Didn't load all.gif!", collector.getUrls().contains(urlImgAll));
    }

    @Test
    public void testDifferentMediaTypesLoadImagesOnDemandCssSyntax()
    {
        // set the test specific load parameter
        final XltProperties props = XltProperties.getInstance();
        props.setProperty("com.xceptance.xlt.loadStaticContent", "true");
        props.setProperty("com.xceptance.xlt.cssEnabled", "true");
        props.setProperty("com.xceptance.xlt.css.download.images", "ondemand");

        // start an open page action
        startDifferentMediaTypes("testWebSites/testWebSiteMediaTypesCssSyntax.html");

        // check, if the right URLs were requested
        Assert.assertEquals(9, collector.getUrls().size());
        Assert.assertTrue("Didn't load html page!", collector.getUrls().contains(urlHtml));
        // all css files should be loaded
        Assert.assertTrue("Didn't load test.css!", collector.getUrls().contains(urlCssTest));
        Assert.assertTrue("Didn't load screen.css!", collector.getUrls().contains(urlCssScreen));
        Assert.assertTrue("Didn't load print.css!", collector.getUrls().contains(urlCssPrint));
        Assert.assertTrue("Didn't load speaker.css!", collector.getUrls().contains(urlCssSpeaker));
        Assert.assertTrue("Didn't load all.css!", collector.getUrls().contains(urlCssAll));
        // just css files with no media type, with the media type all or screen should be executed
        Assert.assertTrue("Didn't load test.gif!", collector.getUrls().contains(urlImg));
        Assert.assertTrue("Didn't load screen.gif!", collector.getUrls().contains(urlImgScreen));
        Assert.assertTrue("Didn't load all.gif!", collector.getUrls().contains(urlImgAll));
    }

    @Test
    public void testDifferentMediaTypesLoadImagesOnDemandWithAtImport()
    {
        // set the test specific load parameter
        final XltProperties props = XltProperties.getInstance();
        props.setProperty("com.xceptance.xlt.loadStaticContent", "true");
        props.setProperty("com.xceptance.xlt.cssEnabled", "true");
        props.setProperty("com.xceptance.xlt.css.download.images", "ondemand");

        // start an open page action
        startDifferentMediaTypesAndRecursiveCss("@import url(\"print.css\") print; @import \"screen.css\" screen; " +
                                                "@import url(\"speaker.css\") speaker; @import \"all.css\" all;");

        // check, if the right URLs were requested
        Assert.assertEquals(8, collector.getUrls().size());
        Assert.assertTrue("Didn't load html page!", collector.getUrls().contains(urlHtml));
        // all css files should be loaded
        Assert.assertTrue("Didn't load test.css!", collector.getUrls().contains(urlCssTest));
        Assert.assertTrue("Didn't load screen.css!", collector.getUrls().contains(urlCssScreen));
        Assert.assertTrue("Didn't load print.css!", collector.getUrls().contains(urlCssPrint));
        Assert.assertTrue("Didn't load speaker.css!", collector.getUrls().contains(urlCssSpeaker));
        Assert.assertTrue("Didn't load all.css!", collector.getUrls().contains(urlCssAll));
        // just css files with no media type, with the media type all or screen should be executed
        Assert.assertTrue("Didn't load screen.gif!", collector.getUrls().contains(urlImgScreen));
        Assert.assertTrue("Didn't load all.gif!", collector.getUrls().contains(urlImgAll));
    }

    @Test
    public void testDifferentMediaTypesLoadImagesOnDemandWithAtMedia()
    {
        // set the test specific load parameter
        final XltProperties props = XltProperties.getInstance();
        props.setProperty("com.xceptance.xlt.loadStaticContent", "true");
        props.setProperty("com.xceptance.xlt.cssEnabled", "true");
        props.setProperty("com.xceptance.xlt.css.download.images", "ondemand");

        // start an open page action
        startDifferentMediaTypesAndRecursiveCss("@media screen {#background{background-image: url(\"screen.gif\");}} " +
                                                "@media all {#background{background-image: url(\"all.gif\");}} " +
                                                "@media speaker {#background{background-image: url(\"speaker.gif\");}} " +
                                                "@media print {#background{background-image: url(\"print.gif\");}}");

        // check, if the right URLs were requested
        Assert.assertEquals(4, collector.getUrls().size());
        Assert.assertTrue("Didn't load html page!", collector.getUrls().contains(urlHtml));
        // all css files should be loaded
        Assert.assertTrue("Didn't load test.css!", collector.getUrls().contains(urlCssTest));
        // just css files with no media type, with the media type all or screen should be executed
        Assert.assertTrue("Didn't load screen.gif!", collector.getUrls().contains(urlImgScreen));
        Assert.assertTrue("Didn't load all.gif!", collector.getUrls().contains(urlImgAll));
    }

    @Ignore("#1702")
    @Test
    public void testDifferentMediaTypesLoadImagesNeverHTMLSyntax()
    {
        // set the test specific load parameter
        final XltProperties props = XltProperties.getInstance();
        props.setProperty("com.xceptance.xlt.loadStaticContent", "true");
        props.setProperty("com.xceptance.xlt.cssEnabled", "true");
        props.setProperty("com.xceptance.xlt.css.download.images", "never");

        // start an open page action
        startDifferentMediaTypes("testWebSites/testWebSiteMediaTypes.html");

        // check, if the right URLs were requested
        Assert.assertEquals(6, collector.getUrls().size());
        Assert.assertTrue("Didn't load html page!", collector.getUrls().contains(urlHtml));
        // all css files should be loaded
        Assert.assertTrue("Didn't load test.css!", collector.getUrls().contains(urlCssTest));
        Assert.assertTrue("Didn't load screen.css!", collector.getUrls().contains(urlCssScreen));
        Assert.assertTrue("Didn't load print.css!", collector.getUrls().contains(urlCssPrint));
        Assert.assertTrue("Didn't load speaker.css!", collector.getUrls().contains(urlCssSpeaker));
        Assert.assertTrue("Didn't load all.css!", collector.getUrls().contains(urlCssAll));
    }

    @Test
    public void testDifferentMediaTypesLoadImagesNeverCssSyntax()
    {
        // set the test specific load parameter
        final XltProperties props = XltProperties.getInstance();
        props.setProperty("com.xceptance.xlt.loadStaticContent", "true");
        props.setProperty("com.xceptance.xlt.cssEnabled", "true");
        props.setProperty("com.xceptance.xlt.css.download.images", "never");

        // start an open page action
        startDifferentMediaTypes("testWebSites/testWebSiteMediaTypesCssSyntax.html");

        // check, if the right URLs were requested
        Assert.assertEquals(6, collector.getUrls().size());
        Assert.assertTrue("Didn't load html page!", collector.getUrls().contains(urlHtml));
        // all css files should be loaded
        Assert.assertTrue("Didn't load test.css!", collector.getUrls().contains(urlCssTest));
        Assert.assertTrue("Didn't load screen.css!", collector.getUrls().contains(urlCssScreen));
        Assert.assertTrue("Didn't load print.css!", collector.getUrls().contains(urlCssPrint));
        Assert.assertTrue("Didn't load speaker.css!", collector.getUrls().contains(urlCssSpeaker));
        Assert.assertTrue("Didn't load all.css!", collector.getUrls().contains(urlCssAll));
    }

    @Test
    public void testDifferentMediaTypesLoadImagesNeverWithAtImport()
    {
        // set the test specific load parameter
        final XltProperties props = XltProperties.getInstance();
        props.setProperty("com.xceptance.xlt.loadStaticContent", "true");
        props.setProperty("com.xceptance.xlt.cssEnabled", "true");
        props.setProperty("com.xceptance.xlt.css.download.images", "never");

        // start an open page action
        startDifferentMediaTypesAndRecursiveCss("@import url(\"print.css\") print; @import \"screen.css\" screen; " +
                                                "@import url(\"speaker.css\") speaker; @import \"all.css\" all;");

        // check, if the right URLs were requested
        Assert.assertEquals(6, collector.getUrls().size());
        Assert.assertTrue("Didn't load html page!", collector.getUrls().contains(urlHtml));
        // all css files should be loaded
        Assert.assertTrue("Didn't load test.css!", collector.getUrls().contains(urlCssTest));
        Assert.assertTrue("Didn't load screen.css!", collector.getUrls().contains(urlCssScreen));
        Assert.assertTrue("Didn't load print.css!", collector.getUrls().contains(urlCssPrint));
        Assert.assertTrue("Didn't load speaker.css!", collector.getUrls().contains(urlCssSpeaker));
        Assert.assertTrue("Didn't load all.css!", collector.getUrls().contains(urlCssAll));
    }

    @Test
    public void testDifferentMediaTypesLoadImagesNeverWithAtMedia()
    {
        // set the test specific load parameter
        final XltProperties props = XltProperties.getInstance();
        props.setProperty("com.xceptance.xlt.loadStaticContent", "true");
        props.setProperty("com.xceptance.xlt.cssEnabled", "true");
        props.setProperty("com.xceptance.xlt.css.download.images", "never");

        // start an open page action
        startDifferentMediaTypesAndRecursiveCss("@media screen {#background{background-image: url(\"screen.gif\");}} " +
                                                "@media all {#background{background-image: url(\"all.gif\");}} " +
                                                "@media speaker {#background{background-image: url(\"speaker.gif\");}} " +
                                                "@media print {#background{background-image: url(\"print.gif\");}}");

        // check, if the right URLs were requested
        Assert.assertEquals(2, collector.getUrls().size());
        Assert.assertTrue("Didn't load html page!", collector.getUrls().contains(urlHtml));
        // all css files should be loaded
        Assert.assertTrue("Didn't load test.css!", collector.getUrls().contains(urlCssTest));
    }

    /**
     * Tests the ability to load a resource referenced by a dynamically added HTML link element.
     */
    @Test
    public void testLoadDynLinks() throws Throwable
    {
        final XltProperties props = XltProperties.getInstance();
        props.setProperty("com.xceptance.xlt.loadStaticContent", "true");
        props.setProperty("com.xceptance.xlt.cssEnabled", "true");
        props.setProperty("com.xceptance.xlt.css.download.images", "never");
        props.setProperty("com.xceptance.xlt.javaScriptEnabled", "true");

        final String html = "<html><head></head><body><h1>Foo</h1><script>" + "var e = document.createElement('link');" +
                            "e.setAttribute('href','test.css');" + "e.setAttribute('rel','stylesheet');" +
                            "e.setAttribute('type','text/css');" + "document.body.appendChild(e);" + "</script></body></html>";
        startOpenPageAction(html, "", "");

        final Set<URL> urls = collector.getUrls();
        Assert.assertEquals(2, urls.size());
        Assert.assertTrue("", urls.contains(urlHtml));
        Assert.assertTrue("", urls.contains(urlCssTest));

    }

    private void startAllLoadedByHtml()
    {
        try
        {
            // test content
            final String contentHtml = IOUtils.toString(getClass().getResource("testWebSites/testWebSite.html"), StandardCharsets.UTF_8);
            // start open page action
            startOpenPageAction(contentHtml, "", "");
        }
        catch (final Throwable e)
        {
            Assert.fail("Can't open the page!");
        }
    }

    private void startCssLoadsImage()
    {
        try
        {
            // test content
            final String contentHtml = IOUtils.toString(getClass().getResource("testWebSites/testWebSiteRecursiveCss.html"),
                                                        StandardCharsets.UTF_8);
            final String contentCss = "#background{background-image: url(\"test.gif\");}";
            // start open page action
            startOpenPageAction(contentHtml, contentCss, "");
        }
        catch (final Throwable e)
        {
            Assert.fail("Can't open the page!");
        }
    }

    private void startJsLoadCss()
    {
        try
        {
            // test content
            final String contentHtml = IOUtils.toString(getClass().getResource("testWebSites/testWebSiteRecursiveJs.html"),
                                                        StandardCharsets.UTF_8);
            final String contentJs = "document.write(\"<link rel='stylesheet' type='text/css' href='test.css' />\");";
            // start open page action
            startOpenPageAction(contentHtml, "", contentJs);
        }
        catch (final Throwable e)
        {
            Assert.fail("Can't open the page!");
        }
    }

    private void startDifferentMediaTypes(final String webSite)
    {
        try
        {
            // test content
            final String contentHtml = IOUtils.toString(getClass().getResource(webSite), StandardCharsets.UTF_8);
            // start open page action
            startOpenPageAction(contentHtml, "#background{background-image: url(\"test.gif\");}", "");
        }
        catch (final Throwable e)
        {
            Assert.fail("Can't open the page! " + e);
        }
    }

    private void startDifferentMediaTypesAndRecursiveCss(final String contentCss)
    {
        try
        {
            // test content
            final String contentHtml = IOUtils.toString(getClass().getResource("testWebSites/testWebSiteMediaTypesAndRecursiveCss.html"),
                                                        StandardCharsets.UTF_8);
            // start open page action
            startOpenPageAction(contentHtml, contentCss, "");
        }
        catch (final Throwable e)
        {
            Assert.fail("Can't open the page! " + e);
        }
    }

    // private void outputRequestedUrls()
    // {
    // for (final URL output : collector.getUrls())
    // {
    // System.out.println("requested: " + output);
    // }
    // }

    private void setUrls() throws MalformedURLException
    {
        // test URLs
        urlHtml = new URL("http://localhost/test.html");
        urlCssTest = new URL("http://localhost/test.css");
        urlCssPrint = new URL("http://localhost/print.css");
        urlCssScreen = new URL("http://localhost/screen.css");
        urlCssSpeaker = new URL("http://localhost/speaker.css");
        urlCssAll = new URL("http://localhost/all.css");
        urlImg = new URL("http://localhost/test.gif");
        urlImgScreen = new URL("http://localhost/screen.gif");
        urlImgPrint = new URL("http://localhost/print.gif");
        urlImgSpeaker = new URL("http://localhost/speaker.gif");
        urlImgAll = new URL("http://localhost/all.gif");
        urlJs = new URL("http://localhost/test.js");
    }

    private void startOpenPageAction(final String contentHtml, final String contentCss, final String contentJs) throws Throwable
    {
        // test URLs
        setUrls();

        final AbstractHtmlPageAction action = new OpenPageAction(urlHtml.toExternalForm());
        ((XltWebClient) action.getWebClient()).addResponseProcessor(collector);

        // create mocked web connection
        final MockWebConnection mockWebConnection = new XltMockWebConnection(((XltWebClient) action.getWebClient()));
        // set the response for the test URLs
        mockWebConnection.setResponse(urlHtml, contentHtml);
        mockWebConnection.setResponse(urlCssTest, contentCss, "text/css");
        mockWebConnection.setResponse(urlCssPrint, "#print,#background{background-image: url(\"print.gif\");}", "text/css");
        mockWebConnection.setResponse(urlCssScreen, "#screen,#background{background-image: url(\"screen.gif\");}", "text/css");
        mockWebConnection.setResponse(urlCssSpeaker, "#speacker,#background{background-image: url(\"speaker.gif\");}", "text/css");
        mockWebConnection.setResponse(urlCssAll, "#all,#background{background-image: url(\"all.gif\");}", "text/css");
        mockWebConnection.setResponse(urlImg, "");
        mockWebConnection.setResponse(urlImgScreen, "");
        mockWebConnection.setResponse(urlImgPrint, "");
        mockWebConnection.setResponse(urlImgSpeaker, "");
        mockWebConnection.setResponse(urlImgAll, "");
        mockWebConnection.setResponse(urlJs, contentJs, "application/javascript");

        ((XltWebClient) action.getWebClient()).setWebConnection(mockWebConnection);
        action.run();
        // outputRequestedUrls();
    }
}
