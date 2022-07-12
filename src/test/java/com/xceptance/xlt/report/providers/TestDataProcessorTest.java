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
package com.xceptance.xlt.report.providers;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import com.xceptance.xlt.api.engine.AbstractData;
import com.xceptance.xlt.api.engine.DummyActionData;
import com.xceptance.xlt.api.engine.DummyCustomData;
import com.xceptance.xlt.api.engine.DummyEventData;
import com.xceptance.xlt.api.engine.DummyRequestData;
import com.xceptance.xlt.api.engine.DummyTransactionData;
import com.xceptance.xlt.report.util.IntMinMaxValueSet;
import com.xceptance.xlt.report.util.RuntimeHistogram;
import com.xceptance.xlt.report.util.SummaryStatistics;
import com.xceptance.xlt.report.util.ValueSet;

/**
 * This class aims at testing the data processors. However currently all tests are set to @Ignore as they would fail.
 * These failures may indicate a problem or be due to illegal test cases which are caused by wrong usage of the API
 * However this can not be solved without clarification whether to call data processors directly or only by calling
 * report providers. Hence all tests have been set to @Ignore. Furthermore the tests are unfinished.
 * 
 * @author Sebastian Oerding
 */
public class TestDataProcessorTest
{
    private static final AbstractData[] data = new AbstractData[]
        {
            DummyActionData.getDefault(), DummyCustomData.getDefault(), DummyEventData.getDefault(), DummyRequestData.getDefault(),
            DummyTransactionData.getDefault()
        };

    @Ignore
    @Test
    public void testActionDataProcessor()
    {
        final ActionsReportProvider arp = new ActionsReportProvider();
        arp.setConfiguration(getConfiguration());
        final DummyActionDataProcessor adp = new DummyActionDataProcessor("TestActionDataProcessor", arp);
        applyData(adp);
        Assert.assertEquals("Wrong size for number of actions", 1, adp.getProxy().getMinMaxValueSetSize());

        Assert.assertEquals("Wrong size for number of errors", 1, adp.getProxy().getTotalErrors());
        ValueSet vs = adp.getProxy().getCountPerSecondValueSet();
        Assert.assertEquals("Wrong start time for countPerSecondValueSet", 1, vs.getFirstSecond());
        Assert.assertEquals("Wrong end time for countPerSecondValueSet", 1, vs.getLastSecond());
        Assert.assertEquals("Wrong length for countPerSecondValueSet", 1, vs.getLengthInSeconds());
        vs = adp.getProxy().getErrorsPerSecondValueSet();
        Assert.assertEquals("Wrong start time for countPerSecondValueSet", 1, vs.getFirstSecond());
        Assert.assertEquals("Wrong end time for countPerSecondValueSet", 1, vs.getLastSecond());
        Assert.assertEquals("Wrong length for countPerSecondValueSet", 1, vs.getLengthInSeconds());
        final RuntimeHistogram histogram = adp.getProxy().getRunTimeHistogram();
        // TODO it questionable whether we should expect these values / if we want to test this here
        Assert.assertEquals("Wrong median value", 1, histogram.getMedianValue(), 0.0);
        Assert.assertEquals("Wrong number of buckets", 1, histogram.getNumberOfBuckets());
        final SummaryStatistics ss = adp.getProxy().getRunTimeStatistics();
        Assert.assertEquals("Wrong count", 1, ss.getCount());
        Assert.assertEquals("Wrong maximum", 1, ss.getMaximum());
        Assert.assertEquals("Wrong mean", 1.0, ss.getMean(), 0.0);
        Assert.assertEquals("Wrong minimum", 1, ss.getMinimum());
        Assert.assertEquals("Wrong deviation", 0.0, ss.getStandardDeviation(), 0.0);
        Assert.assertEquals("Wrong sum", 1.0, ss.getSum(), 0.0);
        final IntMinMaxValueSet vs2 = adp.getProxy().getRunTimeValueSet();
        // TODO what should we expect here?
        Assert.assertEquals("Unexpected first second", 1, vs2.getFirstSecond());
    }

    @Ignore
    @Test
    public void testCustomDataProcessor()
    {
        final CustomTimersReportProvider crp = new CustomTimersReportProvider();
        crp.setConfiguration(getConfiguration());
        final DummyCustomDataProcessor cdp = new DummyCustomDataProcessor("TestCustomDataProcessor", crp);
        applyData(cdp);
        // System.out.println(cdp.getStartTime());
        // System.out.println(cdp.getEndTime());
        // System.out.println(cdp.getProxy());
        // System.out.println(cdp.getProxy());
        // System.out.println(cdp.getProxy());
        // System.out.println(cdp.getProxy());
        // System.out.println(cdp.getProxy());
        // System.out.println(cdp.getProxy());
    }

    @Ignore
    @Test
    public void testRequestDataProcessor()
    {
        final RequestsReportProvider rrp = new RequestsReportProvider();
        rrp.setConfiguration(DummyReportGeneratorConfiguration.getDefault());
        final DummyRequestDataProcessor rdp = new DummyRequestDataProcessor("TestRequestDataProcessor", rrp);
        applyData(rdp);
    }

    @Ignore
    @Test
    public void testTransactionDataProcessor()
    {
        final TransactionsReportProvider trp = new TransactionsReportProvider();
        trp.setConfiguration(getConfiguration());
        final DummyTransactionDataProcessor tdp = new DummyTransactionDataProcessor("TestTransactionDataProcessor", trp);
        applyData(tdp);
    }

    private static <T extends AbstractDataProcessor> void applyData(final T processor)
    {
        for (int i = 0; i < data.length; i++)
        {
            processor.processDataRecord(data[i]);
        }
    }

    private DummyReportProviderConfiguration getConfiguration()
    {
        return new DummyReportProviderConfiguration();
    }
}
