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
package com.xceptance.xlt.agentcontroller;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import com.xceptance.xlt.agentcontroller.PartialGetUtils.ContentRangeHeaderData;

@RunWith(Parameterized.class)
public class PartialGetUtils_ContentRangeHeaderTest
{
    @Parameters(name = "{index}: {0}")
    public static Iterable<Object[]> data()
    {
        return Arrays.asList(new Object[][]
            {
                {
                    null, null
                },
                {
                    "", null
                },
                {
                    "   ", null
                },
                {
                    "bytes", null
                },
                {
                    "bytes ", null
                },
                {
                    "bytes 0", null
                },
                {
                    "bytes 0-0", null
                },
                {
                    "bytes 0-0/1", new ContentRangeHeaderData(0, 0, 1)
                },
                {
                    "bytes 0-999/12345", new ContentRangeHeaderData(0, 999, 12345)
                },
                {
                    "bytes 1000-1999/12345", new ContentRangeHeaderData(1000, 1999, 12345)
                }
            });
    }

    @Parameter(value = 0)
    public String headerValue;

    @Parameter(value = 1)
    public ContentRangeHeaderData expectedHeaderData;

    @Test
    public void parseContentRangeHeader()
    {
        final ContentRangeHeaderData actualHeaderData = PartialGetUtils.parseContentRangeHeader(headerValue);

        if (expectedHeaderData == null)
        {
            Assert.assertNull(actualHeaderData);
        }
        else
        {
            Assert.assertEquals(expectedHeaderData.startPos, actualHeaderData.startPos);
            Assert.assertEquals(expectedHeaderData.endPos, actualHeaderData.endPos);
            Assert.assertEquals(expectedHeaderData.totalBytes, actualHeaderData.totalBytes);
        }
    }
}
