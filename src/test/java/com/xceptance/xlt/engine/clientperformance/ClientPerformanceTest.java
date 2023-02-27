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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.WebDriver;

import com.xceptance.common.lang.ReflectionUtils;
import com.xceptance.xlt.api.engine.Data;
import com.xceptance.xlt.api.engine.RequestData;
import com.xceptance.xlt.api.util.XltCharBuffer;
import com.xceptance.xlt.api.webdriver.XltChromeDriver;
import com.xceptance.xlt.engine.clientperformance.util.HttpRequestHandlerConfiguration;
import com.xceptance.xlt.engine.clientperformance.util.HttpServer;
import com.xceptance.xlt.engine.metrics.Metrics;
import com.xceptance.xlt.engine.metrics.Metrics.LazySingletonHolder;

/**
 * Checks the client-performance measurements of native browsers or XltDriver for plausibility.
 */
@Ignore("To be run manually only")
public class ClientPerformanceTest
{
    private static final int WARM_UP_REQUESTS = 10;

    private static TestMetrics metrics;

    private static WebDriver driver;

    private static HttpServer httpServer;

    @BeforeClass
    public static void beforeClass() throws IOException, ClassNotFoundException, InterruptedException
    {
        // install our special Metrics sub class to get access to the measurements
        metrics = new TestMetrics();
        // HACK: replace the Metrics instance
        ReflectionUtils.writeStaticField(LazySingletonHolder.class, "metrics", metrics);

        // start our test HTTP server
        httpServer = new HttpServer(HttpRequestHandlerConfiguration.HTTP_PORT, -1);

        // start the browser
        driver = new XltChromeDriver();
        // driver = new XltFirefoxDriver();
        // driver = new XltDriver();

        Thread.sleep(2000);

        // fire some warm-up requests
        HttpRequestHandlerConfiguration config = new HttpRequestHandlerConfiguration();
        for (int i = 0; i < WARM_UP_REQUESTS; i++)
        {
            loadPage(config.buildUrl());
        }
    }

    @AfterClass
    public static void afterClass()
    {
        driver.quit();
        httpServer.close();

        // can check expectations only now as data is available only after Driver#quit() for FF/CH
        metrics.checkExpectations();
    }

    @Test
    public void responseSize()
    {
        HttpRequestHandlerConfiguration config = new HttpRequestHandlerConfiguration();

        config.setResponseLength(0);
        loadPage(config.buildUrl());

        config.setResponseLength(200);
        loadPage(config.buildUrl());

        config.setResponseLength(1000);
        loadPage(config.buildUrl());
    }

    @Test
    @Ignore("Simulating receive delay on the server does not really affect the sender because of buffering in the network layer")
    public void sendTime()
    {
        // first create some cookies for the current domain to make the requests sufficiently large
        HttpRequestHandlerConfiguration config = new HttpRequestHandlerConfiguration();
        loadPage(config.buildUrl());

        for (int i = 0; i < 100; i++)
        {
            driver.manage().addCookie(new Cookie("xlt_" + i, StringUtils.repeat('A', 1024)));
        }

        // test
        try
        {
            config.setServerReadTime(0);
            loadPage(config.buildUrl());

            config.setServerReadTime(100);
            loadPage(config.buildUrl());

            config.setServerReadTime(8000);
            loadPage(config.buildUrl());
        }
        finally
        {
            // remove all cookies again
            driver.manage().deleteAllCookies();
        }
    }

    @Test
    public void busyTime()
    {
        HttpRequestHandlerConfiguration config = new HttpRequestHandlerConfiguration();

        config.setServerBusyTime(0);
        loadPage(config.buildUrl());

        config.setServerBusyTime(100);
        loadPage(config.buildUrl());

        config.setServerBusyTime(1000);
        loadPage(config.buildUrl());
    }

    @Test
    public void receiveTime()
    {
        HttpRequestHandlerConfiguration config = new HttpRequestHandlerConfiguration();
        config.setResponseLength(1000);

        config.setServerWriteTime(0);
        loadPage(config.buildUrl());

        config.setServerWriteTime(100);
        loadPage(config.buildUrl());

        config.setServerWriteTime(1000);
        loadPage(config.buildUrl());

        config.setServerWriteTime(5000);
        loadPage(config.buildUrl());
    }

    @Test
    public void typical()
    {
        HttpRequestHandlerConfiguration config = new HttpRequestHandlerConfiguration();

        config.setServerBusyTime(500).setServerWriteTime(100).setResponseLength(64000);
        loadPage(config.buildUrl());
    }

    @Test
    public void typical_repeated()
    {
        HttpRequestHandlerConfiguration config = new HttpRequestHandlerConfiguration();

        config.setServerBusyTime(500).setServerWriteTime(100).setResponseLength(64000);

        for (int i = 0; i < 50; i++)
        {
            loadPage(config.buildUrl());
        }
    }

    private static void loadPage(final String url)
    {
        driver.get(url);

        // add delay between requests
        // ThreadUtils.sleep(350);
    }

    /**
     * A sub class of {@link Metrics} that stores data for later evaluation.
     */
    private static class TestMetrics extends Metrics
    {
        List<Data> dataRecords = new ArrayList<>();

        boolean failed = false;

        @Override
        public void updateMetrics(final Data data)
        {
            dataRecords.add(data);
        }

        public void checkExpectations()
        {
            int requestsToIgnore = WARM_UP_REQUESTS;

            for (Data data : dataRecords)
            {
                if (data instanceof RequestData)
                {
                    final RequestData reqData = (RequestData) data;

                    final String url = reqData.getUrl().toString();
                    if (url.startsWith("http://localhost") && !url.endsWith("favicon.ico"))
                    {
                        if (requestsToIgnore == 0)
                        {
                            checkRequest(reqData);
                        }
                        else
                        {
                            requestsToIgnore--;
                        }
                    }
                }
            }

            if (failed)
            {
                Assert.fail("See console log for more information");
            }
        }

        private void checkRequest(RequestData data)
        {
            System.err.println(data.toCSV());

            // extract expected values from the request's URL
            HttpRequestHandlerConfiguration config = new HttpRequestHandlerConfiguration();
            config.parseUrl(data.getUrl().toString());

            int expectedConnectTime = 0;
            int expectedSendTime = 0;
            int expectedBusyTime = config.getServerBusyTime();
            int expectedReceiveTime = config.getServerWriteTime();
            int expectedTimeToFirst = expectedConnectTime + expectedSendTime + expectedBusyTime;
            int expectedTimeToLast = expectedTimeToFirst + expectedReceiveTime;
            int expectedReceivedBytes = config.getResponseLength();

            // now check, partly with some fuzziness
            checkInRange("connect time", expectedConnectTime, data.getConnectTime(), 5);
            checkInRange("send time", expectedSendTime, data.getSendTime(), 5);
            checkInRange("busy time", expectedBusyTime, data.getServerBusyTime(), 5);
            checkInRange("receive time", expectedReceiveTime, data.getReceiveTime(), 5);
            checkInRange("time to first", expectedTimeToFirst, data.getTimeToFirstBytes(), 5);
            checkInRange("time to last", expectedTimeToLast, data.getTimeToLastBytes(), 10);
            checkInRange("runtime", expectedTimeToLast, (int) data.getRunTime(), 10);

            if (expectedReceivedBytes > 0)
            {
                checkInRange("received bytes", expectedReceivedBytes, data.getBytesReceived(), 0);
            }

            checkGreaterThanOrEqual("time to first is smaller than connect time", data.getTimeToFirstBytes(), data.getConnectTime());
            checkGreaterThanOrEqual("time to first is smaller than send time", data.getTimeToFirstBytes(), data.getSendTime());
            checkGreaterThanOrEqual("time to first is smaller than busy time", data.getTimeToFirstBytes(), data.getServerBusyTime());
            checkGreaterThanOrEqual("time to last is smaller than time to first", data.getTimeToLastBytes(), data.getTimeToFirstBytes());
            checkGreaterThanOrEqual("total runtime is smaller than time to last", (int) data.getRunTime(), data.getTimeToLastBytes());

            System.err.println();
        }

        private void checkInRange(String name, int expected, int actual, int maxDelta)
        {
            int min = expected;
            int max = expected + maxDelta;

            if (actual < min || actual > max)
            {
                String message = String.format("%s: value %d is outside of interval [%d, %d]", name, actual, min, max);
                System.err.printf("  -> %s\n", message);
                failed = true;
            }
        }

        private void checkGreaterThanOrEqual(String message, int value1, int value2)
        {
            if (value1 < value2)
            {
                System.err.printf("  -> %s\n", message);
                failed = true;
            }
        }
    }
}
