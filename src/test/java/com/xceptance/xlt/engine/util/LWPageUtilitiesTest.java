/*
 * Copyright (c) 2005-2025 Xceptance Software Technologies GmbH
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
package com.xceptance.xlt.engine.util;

import java.util.Collection;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

/**
 * Tests the implementation of the utility class {@link LWPageUtilities}.
 * 
 * @author Hartmut Arlt (Xceptance Software Technologies GmbH)
 */
public class LWPageUtilitiesTest
{
    @Test
    public void testGetAllScriptLinks_NoScriptLink()
    {
        final String pageContent = "<scripty src=\"bla.js\"></script> <script> src=\"<script src=\"\">\"></script>";
        Assert.assertTrue(LWPageUtilities.getAllScriptLinks(pageContent).isEmpty());
    }

    @Test
    public void testGetAllScriptLinks_WhitespaceLink()
    {
        final String pageContent = "<script src=\"  \"></script>";
        final List<String> scriptLinks = LWPageUtilities.getAllScriptLinks(pageContent);

        Assert.assertTrue(scriptLinks.toString(), scriptLinks.isEmpty());
    }

    @Test
    public void testGetAllScriptLinks()
    {
        final String pageContent = "<script src=\"testscript.js\"/><script src=\"   testscript.js  \"";
        final List<String> scriptLinks = LWPageUtilities.getAllScriptLinks(pageContent);

        Assert.assertEquals(2, scriptLinks.size());
        Assert.assertEquals(scriptLinks.get(0), scriptLinks.get(1));
    }

    @Test
    public void testGetAllLinkLinks_NoLinkLink()
    {
        final String pageContent = "<linky> href=\"example.html\"<link href=\"\"/>link src=\"test.html\"";
        Assert.assertTrue(LWPageUtilities.getAllLinkLinks(pageContent).isEmpty());
    }

    @Test
    public void testGetAllLinkLinks_WhitespaceLink()
    {
        final String pageContent = "<link href=\"     \"/>";
        Assert.assertTrue(LWPageUtilities.getAllLinkLinks(pageContent).isEmpty());
    }

    @Test
    public void testGetAllLinkLinks()
    {
        final String pageContent = "<link href=\"test.html\"><<link href=\"    test.html \"";
        final List<String> linkLinks = LWPageUtilities.getAllLinkLinks(pageContent);

        Assert.assertEquals(2, linkLinks.size());
        Assert.assertEquals(linkLinks.get(0), linkLinks.get(1));
    }

    @Test
    public void testGetAllImageLinks_NoImageLinks()
    {
        final String pageContent = "img src=\"foo\"<img> src=\"bar\"<img src=\"\"> <img <src=\"foobar\"<imgo src=\"fubar\"";
        Assert.assertTrue(LWPageUtilities.getAllImageLinks(pageContent).isEmpty());
    }

    @Test
    public void testGetAllImageLinks_WhitespaceLink()
    {
        final String pageContent = "<img src=\"    \"/>";
        Assert.assertTrue(LWPageUtilities.getAllImageLinks(pageContent).isEmpty());
    }

    @Test
    public void testGetAllImageLinks_JSHandler()
    {
        final String pageContent = "<img onerror=\"this.src='placeholder.gif'\" src=\"http://somehost/product.jpg\">";
        final List<String> imageLinks = LWPageUtilities.getAllImageLinks(pageContent);

        Assert.assertEquals(1, imageLinks.size());
        Assert.assertEquals("http://somehost/product.jpg", imageLinks.get(0));
    }

    @Test
    public void testGetAllImageLinks()
    {
        final String pageContent = "<img src=\"foobar\"/><img src=\"   foobar  \"";
        final List<String> imageLinks = LWPageUtilities.getAllImageLinks(pageContent);

        Assert.assertEquals(2, imageLinks.size());
        Assert.assertEquals(imageLinks.get(0), imageLinks.get(1));
    }

    @Test
    public void testGetAllAnchorLinks_NoAnchorLinks()
    {
        final String pageContent = "a href=\"foo\"<a href=\"\"/><ay href=\"bar\"/><a <href=\"foobar\" ";
        Assert.assertTrue(LWPageUtilities.getAllAnchorLinks(pageContent).isEmpty());
    }

    @Test
    public void testGetAllAnchorLinks_WhitespaceLink()
    {
        final String pageContent = "<a href=\"    \" ";
        Assert.assertTrue(LWPageUtilities.getAllAnchorLinks(pageContent).isEmpty());
    }

    @Test
    public void testGetAllAnchorLinks()
    {
        final String pageContent = "<a href=\"test.html\"/>  <a    href=\"  test.html   \" />";
        final List<String> anchorLinks = LWPageUtilities.getAllAnchorLinks(pageContent);

        Assert.assertEquals(2, anchorLinks.size());
        Assert.assertEquals(anchorLinks.get(0), anchorLinks.get(1));
    }

    @Test
    public void testGetAllBaseLinks_NoBaseLinks()
    {
        final String pageContent = "base href=\"foo\"/><base href=\"\"/><bases href=\"bar\" <base <href=\"foobar\"";
        Assert.assertTrue(LWPageUtilities.getAllBaseLinks(pageContent).isEmpty());
    }

    @Test
    public void testGetAllBaseLinks_WhitespaceLink()
    {
        final String pageContent = "<base   href=\"       \"  />";
        Assert.assertTrue(LWPageUtilities.getAllBaseLinks(pageContent).isEmpty());
    }

    @Test
    public void testGetAllBaseLinks()
    {
        final String pageContent = "<base href=\"test.html\"/>  <base    href=\"  test.html   \" />";
        final List<String> baseLinks = LWPageUtilities.getAllBaseLinks(pageContent);

        Assert.assertEquals(2, baseLinks.size());
        Assert.assertEquals(baseLinks.get(0), baseLinks.get(1));
    }

    @Test
    public void testGetAllInlineCssStatements_NoStatement()
    {
        final String pageContent = "<bla style=\"\" < <style=\"foo\"><sample styles=\"foobar\"";
        Assert.assertTrue(LWPageUtilities.getAllInlineCssStatements(pageContent).isEmpty());
    }

    @Test
    public void testGetAllInlineCssStatements_WhitespaceStatement()
    {
        final String pageContent = "<sample style=\"     \"  />";
        Assert.assertTrue(LWPageUtilities.getAllInlineCssStatements(pageContent).isEmpty());
    }

    @Test
    public void testGetAllInlineCssStatements()
    {
        final String pageContent = "<sample style=\"color:red;\"/> <test style=\"  color:red;    \"";
        final List<String> statements = LWPageUtilities.getAllInlineCssStatements(pageContent);

        Assert.assertEquals(2, statements.size());
        Assert.assertEquals(statements.get(0), statements.get(1));
    }

    @Test
    public void testGetAllInlineCssResourceUrls_NoUrl()
    {
        final String pageContent = "<a style=\"color:url red\" /> <div style=\"\" url('sample')";
        Assert.assertTrue(LWPageUtilities.getAllInlineCssResourceUrls(pageContent).isEmpty());
    }

    @Test
    public void testGetAllInlineCssResourceUrls_WhitespaceUrl()
    {
        final String pageContent = " <div style=\"  \" /> <table  style=\"  color:  url('    ')\"";
        Assert.assertTrue(LWPageUtilities.getAllInlineCssResourceUrls(pageContent).isEmpty());
    }

    @Test
    public void testGetAllInlineCssResourceUrls()
    {
        final String pageContent = "<ol style=\"color:url('blau.jpg')\"  />   <div style=\" color: url(   blau.jpg) \"  />";
        final Collection<String> urls = LWPageUtilities.getAllInlineCssResourceUrls(pageContent);

        Assert.assertEquals(1, urls.size());
        Assert.assertTrue(urls.contains("blau.jpg"));
    }

    @Test
    public void testRemoveHtmlComments()
    {
        // happy path
        Assert.assertEquals("", LWPageUtilities.removeHtmlComments(""));
        Assert.assertEquals("aaa bbb", LWPageUtilities.removeHtmlComments("aaa bbb"));
        Assert.assertEquals("aaa  bbb", LWPageUtilities.removeHtmlComments("aaa <!-- aaa \n bbb --> bbb"));
        Assert.assertEquals("aaa  bbb  ccc", LWPageUtilities.removeHtmlComments("aaa <!-- aaa --> bbb <!-- bbb --> ccc"));
        Assert.assertEquals("aaa  bbb", LWPageUtilities.removeHtmlComments("aaa <!----> bbb"));

        // incomplete comments
        Assert.assertEquals("aaa <!- --> bbb", LWPageUtilities.removeHtmlComments("aaa <!- --> bbb"));
        Assert.assertEquals("aaa ", LWPageUtilities.removeHtmlComments("aaa <!-- bbb"));
        Assert.assertEquals("aaa ", LWPageUtilities.removeHtmlComments("aaa <!-- \n\n bbb"));
        Assert.assertEquals("aaa ", LWPageUtilities.removeHtmlComments("aaa <!-- -> bbb"));
        Assert.assertEquals("aaa ", LWPageUtilities.removeHtmlComments("aaa <!--> bbb"));

        // nested comments
        Assert.assertEquals("aaa  bbb", LWPageUtilities.removeHtmlComments("aaa <!-- <!-- aaa --> bbb"));
        Assert.assertEquals("aaa  --> bbb", LWPageUtilities.removeHtmlComments("aaa <!-- aaa --> --> bbb"));
    }
}
