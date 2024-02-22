/*
 * Copyright (c) 2005-2024 Xceptance Software Technologies GmbH
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

import static org.junit.Assert.assertEquals;

import org.junit.Assert;
import org.junit.Test;

import com.xceptance.xlt.api.util.XltCharBuffer;
import com.xceptance.xlt.report.util.UrlHostParser;

public class UrlHostParserTest
{
    @Test
    public void happyPath()
    {
        final XltCharBuffer foobar = XltCharBuffer.valueOf("www.foo.bar");
        
        Assert.assertEquals(foobar, UrlHostParser.retrieveHostFromUrl(XltCharBuffer.valueOf(("http://www.foo.bar"))));
        Assert.assertEquals(foobar, UrlHostParser.retrieveHostFromUrl(XltCharBuffer.valueOf(("http://www.foo.bar?"))));
        Assert.assertEquals(foobar, UrlHostParser.retrieveHostFromUrl(XltCharBuffer.valueOf(("http://www.foo.bar#"))));
        Assert.assertEquals(foobar, UrlHostParser.retrieveHostFromUrl(XltCharBuffer.valueOf(("http://www.foo.bar/"))));
        Assert.assertEquals(foobar, UrlHostParser.retrieveHostFromUrl(XltCharBuffer.valueOf(("http://www.foo.bar/?"))));
        Assert.assertEquals(foobar, UrlHostParser.retrieveHostFromUrl(XltCharBuffer.valueOf(("http://www.foo.bar/test/index.html"))));
        Assert.assertEquals(foobar, UrlHostParser.retrieveHostFromUrl(XltCharBuffer.valueOf(("www.foo.bar?81"))));
        Assert.assertEquals(foobar, UrlHostParser.retrieveHostFromUrl(XltCharBuffer.valueOf(("www.foo.bar#test"))));
        Assert.assertEquals(foobar, UrlHostParser.retrieveHostFromUrl(XltCharBuffer.valueOf(("www.foo.bar/foo"))));
        Assert.assertEquals(foobar, UrlHostParser.retrieveHostFromUrl(foobar));
        Assert.assertEquals(XltCharBuffer.EMPTY, UrlHostParser.retrieveHostFromUrl(XltCharBuffer.valueOf((""))));
        Assert.assertEquals(XltCharBuffer.EMPTY, UrlHostParser.retrieveHostFromUrl(XltCharBuffer.valueOf(("/"))));
        Assert.assertEquals(XltCharBuffer.EMPTY, UrlHostParser.retrieveHostFromUrl(XltCharBuffer.valueOf(("?"))));
        Assert.assertEquals(XltCharBuffer.EMPTY, UrlHostParser.retrieveHostFromUrl(XltCharBuffer.valueOf(("#"))));

        Assert.assertEquals(XltCharBuffer.valueOf("www"), UrlHostParser.retrieveHostFromUrl(XltCharBuffer.valueOf(("://www"))));
    }
    
    @Test 
    public void bufferOfBuffer()
    {
        final XltCharBuffer line = XltCharBuffer.valueOf(
            "R,COBillingAndPlaceOrder.4,1571925807692,77,false,1633,578,200,https://production.foobar.net/on/any.store/Sites-MyStore-Site/en_US/Send-ClearFlag,text/html,0,0,77,0,77,77,,,,,0,,");
        final XltCharBuffer url = line.substring(63, 145);

        assertEquals(XltCharBuffer.valueOf("https://production.foobar.net/on/any.store/Sites-MyStore-Site/en_US/Send-ClearFlag"), url);
        assertEquals(XltCharBuffer.valueOf("production.foobar.net"), UrlHostParser.retrieveHostFromUrl(url));

    }
}
