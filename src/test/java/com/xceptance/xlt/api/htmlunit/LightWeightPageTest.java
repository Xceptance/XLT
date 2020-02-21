package com.xceptance.xlt.api.htmlunit;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;

import com.gargoylesoftware.htmlunit.WebRequest;
import com.gargoylesoftware.htmlunit.WebResponse;
import com.gargoylesoftware.htmlunit.WebResponseData;
import com.gargoylesoftware.htmlunit.util.NameValuePair;

/**
 * Tests {@link LightWeightPage}'s determineContentCharset() exhaustively but does not test which charsets are supported
 * and which not. Also does not test each possible branch but test coverage should be sufficient.
 * 
 * @author Sebastian Oerding
 */
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
    public void testDetermineContentCharsetFromResponseHeader() throws IOException
    {
        final WebResponseData wrd = new WebResponseData(new byte[] {}, 200, "Don't bother about it", Arrays.asList(new NameValuePair[]
            {
                new NameValuePair("content-type", "charset=utf8")
            }));

        final WebResponse response = new WebResponse(wrd, null, 0);
        final LightWeightPage lwp = new LightWeightPage(response, "myTimerName");
        Assert.assertEquals("Char set has been changed.", StandardCharsets.UTF_8, lwp.getCharset());
    }

    @Test
    public void testDetermineContentCharsetFromXml() throws IOException
    {
        final String content = "<?xml encoding=\"utf8\" ?>";
        final WebResponseData wrd = new WebResponseData(content.getBytes(), 200, "Don't mind", new ArrayList<NameValuePair>());
        final WebResponse response = new WebResponse(wrd, null, 0);
        final LightWeightPage lwp = new LightWeightPage(response, "myTimerName");
        Assert.assertEquals("Char set has been changed.", StandardCharsets.UTF_8, lwp.getCharset());
    }

    @Test
    public void testDetermineContentCharsetFromHtml() throws IOException
    {
        final String content = "<meta content=\" charset=utf8\"";
        final WebResponseData wrd = new WebResponseData(content.getBytes(), 200, "Don't mind", new ArrayList<NameValuePair>());
        final WebResponse response = new WebResponse(wrd, null, 0);
        final LightWeightPage lwp = new LightWeightPage(response, "myTimerName");
        Assert.assertEquals("Char set has been changed.", StandardCharsets.UTF_8, lwp.getCharset());
    }

    @Test
    public void testDetermineContentCharsetFromRequest() throws IOException
    {
        final WebRequest wr = new WebRequest(null);
        wr.setCharset(StandardCharsets.UTF_8);
        final WebResponseData wrd = new WebResponseData("bla".getBytes(), 200, "Don't mind", new ArrayList<NameValuePair>());
        final WebResponse response = new WebResponse(wrd, wr, 0);
        final LightWeightPage lwp = new LightWeightPage(response, "myTimerName");
        Assert.assertEquals("Char set has been changed.", StandardCharsets.UTF_8, lwp.getCharset());
    }
}
