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
package com.xceptance.xlt.engine.httprequest;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.FileUtils;
import org.htmlunit.HttpMethod;
import org.htmlunit.WebRequest;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class BinaryRequestBodyTest
{
    private static final String URL = "http://www.example.org/index.html";

    private static final String CONTENT_TYPE = "application/gzip";

    private static byte[] BYTES;

    private static File FILE;

    @BeforeClass
    public static void createTempFileWithBinaryContent() throws IOException
    {
        // create some binary content as byte array
        BYTES = new byte[256];
        for (int i = 0; i < BYTES.length; i++)
        {
            BYTES[i] = (byte) i;
        }

        // store that binary content also to a file
        FILE = File.createTempFile(BinaryRequestBodyTest.class.getName(), ".bin");
        FILE.deleteOnExit();
        FileUtils.writeByteArrayToFile(FILE, BYTES);
    }

    @AfterClass
    public static void deleteTempFileWithBinaryContent() throws IOException
    {
        FILE.delete();
    }

    @Test
    public void binaryBodyGivenAsByteArray() throws MalformedURLException, URISyntaxException
    {
        final WebRequest webRequest = new HttpRequest().baseUrl(URL).method(HttpMethod.POST).header("Content-Type", CONTENT_TYPE)
                                                       .body(BYTES).buildWebRequest();

        validate(webRequest);
    }

    @Test
    public void binaryBodyGivenAsFile() throws URISyntaxException, IOException
    {
        final WebRequest webRequest = new HttpRequest().baseUrl(URL).method(HttpMethod.PUT).header("Content-Type", CONTENT_TYPE).body(FILE)
                                                       .buildWebRequest();

        validate(webRequest);
    }

    @Test
    public void binaryBodyGivenAsInputStream() throws URISyntaxException, FileNotFoundException, IOException
    {
        final WebRequest webRequest = new HttpRequest().baseUrl(URL).method(HttpMethod.PATCH).header("Content-Type", CONTENT_TYPE)
                                                       .body(new FileInputStream(FILE)).buildWebRequest();

        validate(webRequest);
    }

    private void validate(final WebRequest webRequest)
    {
        Assert.assertEquals(StandardCharsets.ISO_8859_1, webRequest.getCharset());
        Assert.assertEquals(CONTENT_TYPE, webRequest.getAdditionalHeader("Content-Type"));
        Assert.assertEquals(0, webRequest.getRequestParameters().size());

        final byte[] actualBytes = webRequest.getRequestBody().getBytes(webRequest.getCharset());
        Assert.assertArrayEquals(BYTES, actualBytes);
    }
}
