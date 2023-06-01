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
package com.xceptance.xlt.api.util;

import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.xceptance.xlt.engine.util.TimerUtils;

public class LightweightHtmlPageUtilsTest
{
    @Test
    public final void testGetAllAnchorLinks()
    {
        final String content = "<html><body>" + "<a href=\"http://1\">Test</a>" + "<A href=\"2\">Test</a>"
                               + "<a title=\"test\" href=\"3\">Test</a>" + "<a href= \"4\">Test</a>" + "<a HREF=\"5\">Test</a>"
                               + "<a href='6'>Test</a>" + "<a  href='7'>Test</a>" + "<a title=\"this is an href\" href=\"8\">"
                               + "<a href=\" 9 \">Test</a>" + "<a\nhref=\"10\">Test</a>" + "<a title=\"Test\"\nhref=\"11\">Test</a>"
                               + "</body></html>";
        final List<String> list = LightweightHtmlPageUtils.getAllAnchorLinks(content);

        Assert.assertEquals(11, list.size());
        Assert.assertTrue(Arrays.deepEquals(new String[]
            {
                "http://1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11"
            }, list.toArray()));
    }

    @Test
    public final void testGetAllImageLinks()
    {
        final String content = "<html><body>" + "<img src=\"http://1\">" + "<IMG src=\"2\">" + "<img title=\"test\" src=\"3\">"
                               + "<img src= \"4\">" + "<img SRC=\"5\">" + "<img src='6'>" + "<img  src='7'>"
                               + "<img title=\"this is an src\" src=\"8\">" + "<img src=\" 9 \">" + "<img src=\"10\"/>"
                               + "<img src=\"11\" />" + "<img\nsrc=\"12\">" + "<img title=\"Test\"\nsrc=\"13\">" + "</body></html>";
        final List<String> list = LightweightHtmlPageUtils.getAllImageLinks(content);

        Assert.assertEquals(13, list.size());
        Assert.assertTrue(Arrays.deepEquals(new String[]
            {
                "http://1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13"
            }, list.toArray()));
    }

    @Test
    public final void testGetAllLinkLinks()
    {
        final String content = "<html><body>" + "<link href=\"http://1\">Test</a>" + "<LINK href=\"2\">Test</a>"
                               + "<link title=\"test\" href=\"3\">Test</a>" + "<link href= \"4\">Test</a>" + "<link HREF=\"5\">Test</a>"
                               + "<link href='6'>Test</a>" + "<link  href='7'>Test</a>" + "<link title=\"this is an href\" href=\"8\">"
                               + "<link href=\" 9 \">Test</a>" + "<link\nhref=\"10\">Test</a>"
                               + "<link title=\"Test\"\nhref=\"11\">Test</a>" + "</body></html>";
        final List<String> list = LightweightHtmlPageUtils.getAllLinkLinks(content);

        Assert.assertEquals(11, list.size());
        Assert.assertTrue(Arrays.deepEquals(new String[]
            {
                "http://1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11"
            }, list.toArray()));
    }

    @Test
    public final void testGetAllScriptLinks()
    {
        final String content = "<html><body>" + "<script src=\"http://1\">" + "<SCRIPT src=\"2\">" + "<script title=\"test\" src=\"3\">"
                               + "<script src= \"4\">" + "<script SRC=\"5\">" + "<script src='6'>" + "<script  src='7'>"
                               + "<script title=\"this is an src\" src=\"8\">" + "<script src=\" 9 \">" + "<script src=\"10\"/>"
                               + "<script src=\"11\" />" + "<script\nsrc=\"12\">" + "<script title=\"Test\"\nsrc=\"13\">"
                               + "</body></html>";
        final List<String> list = LightweightHtmlPageUtils.getAllScriptLinks(content);
        for (int i = 0; i < 100000; i++)
        {
            LightweightHtmlPageUtils.getAllScriptLinks(content);
        }
        final long s = TimerUtils.get().getStartTime();
        for (int i = 0; i < 100000; i++)
        {
            LightweightHtmlPageUtils.getAllScriptLinks(content);
        }
        System.out.println("script: " + (TimerUtils.get().getElapsedTime(s)));

        Assert.assertEquals(13, list.size());
        Assert.assertTrue(Arrays.deepEquals(new String[]
            {
                "http://1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13"
            }, list.toArray()));
    }

}
