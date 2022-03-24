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
package com.xceptance.xlt.engine.clientperformance;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import com.xceptance.xlt.api.engine.PageLoadTimingData;
import com.xceptance.xlt.clientperformance.ClientPerformanceData;
import com.xceptance.xlt.clientperformance.ClientPerformanceRequest;
import com.xceptance.xlt.clientperformance.PerformanceDataTransformator;

/**
 * Tests the implementation of our {@link PerformanceDataTransformator} utility class.
 */
@RunWith(Parameterized.class)
public class PerformanceDataTransformatorTest
{
    @Parameter(0)
    public String desc;

    @Parameter(1)
    public String json;

    @Parameter(2)
    public List<ClientPerformanceData> data;

    @Parameters(name = "{index}: {0}")
    public static Iterable<Object[]> data()
    {
        final List<ClientPerformanceData> singleEmptyItem = Arrays.asList(new ClientPerformanceData());
        final ClientPerformanceData event = new ClientPerformanceData();

        final PageLoadTimingData c = new PageLoadTimingData("Event");
        c.setTime(123456789);
        c.setRunTime(999);

        event.getCustomDataList().add(c);

        final ClientPerformanceData exampleRequest = new ClientPerformanceData();
        {
            final ClientPerformanceRequest r = new ClientPerformanceRequest();
            r.getRequestData().fromCSV("R,xyz,1,0,true,0,0,0,http://example.net,,0,0,0,0,0,0,,GET,,,0");
            r.getRequestData().setTime(0);
            r.setHttpMethod("GET");

            exampleRequest.getRequestList().add(r);
        }

        return Arrays.asList(new Object[]
            {
                "<null>", null, Collections.emptyList()
            }, new Object[]
            {
                "empty string", "", Collections.emptyList()
            }, new Object[]
            {
                "blank string", "   ", Collections.emptyList()
            }, new Object[]
            {
                "empty array", "[]", Collections.emptyList()
            }, new Object[]
            {
                "no objects in array", "[1, 3, [4]]", Collections.emptyList()
            }, new Object[]
            {
                "empty object", "[{}]", singleEmptyItem
            }, new Object[]
            {
                "neither requests nor timings", "[{\"foo\": \"bar\"}]", singleEmptyItem
            }, new Object[]
            {
                "no requests and timings malformed", "[{\"foo\": \"bar\", \"timings\": \"blubb\"}]", singleEmptyItem
            }, new Object[]
            {
                "timings with one element",
                "[{\"timings\": {\"event\": { \"startTime\": " + c.getTime() + ", \"duration\": " + c.getRunTime() + " }}}]",
                Arrays.asList(event)
            }, new Object[]
            {
                "timings with one element, duration <null>",
                "[{\"timings\": {\"event\": { \"startTime\": 12356789, \"duration\": null }}}]", Arrays.asList(new ClientPerformanceData())
            }, new Object[]
            {
                "timings with one element, duration '0'", "[{\"timings\": {\"event\": { \"startTime\": 12356789, \"duration\": 0 }}}]",
                Arrays.asList(new ClientPerformanceData())
            }, new Object[]
            {
                "no timings, no request method",
                "[{\"requests\": [{ \"url\": \"http://example.net\", \"requestId\": \"xyz\", \"body\": {}, \"response\":{} }]}]",
                singleEmptyItem
            }, new Object[]
            {
                "no timings, no request body",
                "[{\"requests\": [{ \"url\": \"http://example.net\", \"requestId\": \"xyz\", \"method\": \"GET\", \"response\":{} }]}]",
                singleEmptyItem
            }, new Object[]
            {
                "no timings, response <null>",
                "[{\"requests\": [{ \"url\": \"http://example.net\", \"requestId\": \"xyz\", \"method\": \"GET\", \"body\":{}, \"response\": null }]}]",
                singleEmptyItem
            }, new Object[]
            {
                "no timings, request well-formed",
                "[{\"requests\": [{ \"url\": \"http://example.net\", \"requestId\": \"xyz\", \"method\": \"GET\", \"body\":{}, \"response\": {} }]}]",
                Arrays.asList(exampleRequest)
            }
        );
    }

    @Test
    public void test()
    {
        final List<ClientPerformanceData> list = PerformanceDataTransformator.getTransformedPerformanceDataList(json);
        Assert.assertNotNull(list);
        Assert.assertEquals(data.size(), list.size());

        if (!data.isEmpty())
        {
            final ClientPerformanceData d = data.get(0);
            final ClientPerformanceData d2 = list.get(0);

            listCompare(d.getCustomDataList(), d2.getCustomDataList(), (a, b) -> {
                Assert.assertEquals(a.toCSV(), b.toCSV());
            });

            listCompare(d.getRequestList(), d2.getRequestList(), (a, b) -> {
                Assert.assertEquals(a.getRequestData().toCSV(), b.getRequestData().toCSV());
            });
        }
    }

    private <T> void listCompare(final List<T> a, final List<T> b, final BiConsumer<T, T> consumer)
    {
        final int size = a.size();
        Assert.assertEquals(size, b.size());

        for (int i = 0; i < size; i++)
        {
            consumer.accept(a.get(i), b.get(i));
        }
    }
}
