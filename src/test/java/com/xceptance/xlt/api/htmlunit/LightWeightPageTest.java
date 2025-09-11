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
package com.xceptance.xlt.api.htmlunit;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;

import org.htmlunit.WebRequest;
import org.htmlunit.WebResponse;
import org.htmlunit.WebResponseData;
import org.htmlunit.util.NameValuePair;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;

/**
 * Tests {@link LightWeightPage}'s determineContentCharset() exhaustively but does not test which charsets are supported
 * and which not. Also does not test each possible branch but test coverage should be sufficient.
 * 
 * @author Sebastian Oerding
 */
@RunWith(JUnitParamsRunner.class)
public class LightWeightPageTest
{
    @Test
    public void testDetermineContentCharset()
    {
        final String timerName = "myTimerName";
        final LightWeightPage lwp = new LightWeightPage(null, timerName);

        final String actual = lwp.getTimerName();
        Assert.assertEquals("Timer name mismatch.", timerName, actual);
        Assert.assertEquals("Char set has been changed.", StandardCharsets.ISO_8859_1, lwp.getCharset());
    }

    @Test
    @Parameters(value =
        {
            "text/plain                 | ISO-8859-1", //
            "charset=utf-8              | UTF-8", //
            "text/plain; charset=utf8   | UTF-8", //
            "application/hal+json;charset=utf8;profile=\"https://my.api.com/\";version=1 | UTF-8"
        })
    public void testDetermineContentCharsetFromResponseHeader(final String headerValue, final String expectedCharsetName) throws IOException
    {
        final WebResponseData wrd = new WebResponseData(new byte[] {}, 200, "OK", Arrays.asList(new NameValuePair[]
            {
                new NameValuePair("content-type", headerValue)
            }));

        testDetermineContentCharset(wrd, expectedCharsetName);
    }

    @Test
    @Parameters(value =
        {
            "<?xml version=\"1.0\" ?>                       | ISO-8859-1", //
            "<?xml version=\"1.0\" encoding=\"utf8\" ?>     | UTF-8", //
            "<meta name=\"author\" content=\"John Doe\">    | ISO-8859-1", //
            "<meta charset=\"utf8\">                        | UTF-8", //
            "<meta http-equiv=\"content-type\" content=\"text/html; charset=UTF-8\">    | UTF-8",
            "<meta http-equiv=\"content-type\" content=\"application/hal+json;charset=utf8;profile=https://my.api.com/;version=1\"> | UTF-8"
        })
    public void testDetermineContentCharsetFromResponseBody(final String responseBody, final String expectedCharsetName) throws IOException
    {
        final WebResponseData wrd = new WebResponseData(responseBody.getBytes(), 200, "OK", new ArrayList<NameValuePair>());

        testDetermineContentCharset(wrd, expectedCharsetName);
    }

    @Test
    public void testDetermineContentCharsetFromRequest() throws IOException
    {
        final WebRequest wr = new WebRequest(null);
        wr.setCharset(StandardCharsets.UTF_8);
        final WebResponseData wrd = new WebResponseData("bla".getBytes(), 200, "OK", new ArrayList<NameValuePair>());
        final WebResponse response = new WebResponse(wrd, wr, 0);

        testDetermineContentCharset(response, StandardCharsets.UTF_8.name());
    }

    // --- Helper Methods ---

    private void testDetermineContentCharset(WebResponseData wrd, String expectedCharsetName) throws IOException
    {
        final WebResponse response = new WebResponse(wrd, null, 0);

        testDetermineContentCharset(response, expectedCharsetName);
    }

    private void testDetermineContentCharset(WebResponse response, String expectedCharsetName) throws IOException
    {
        final LightWeightPage lwp = new LightWeightPage(response, "myTimerName");

        Assert.assertEquals(expectedCharsetName, lwp.getCharset().name());
    }
}
