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

import java.io.File;
import java.util.Set;

import com.xceptance.common.lang.ReflectionUtils;
import com.xceptance.xlt.report.util.HistogramValueSet;
import com.xceptance.xlt.report.util.SegmentationValueSet;
import com.xceptance.xlt.report.util.SummaryStatistics;
import com.xceptance.xlt.report.util.ValueSet;

/**
 * Overrides {@link #setChartDir(File)} and {@link #setCsvDir(File)} to avoid problems on instantiation. Makes all
 * private fields readable via reflection, uses a proxy for this purpose to avoid code duplication, see
 * {@link #getProxy()}.
 * <p>
 * This class has further getters as {@link RequestDataProcessor} contains additional member compared to its super
 * class.
 * </p>
 * 
 * @author Sebastian Oerding
 */
public class DummyRequestDataProcessor extends RequestDataProcessor
{
    private Proxy proxy;

    /**
     * @param name
     * @param provider
     */
    public <T extends AbstractDataProcessor> DummyRequestDataProcessor(final String name,
                                                                       final AbstractDataProcessorBasedReportProvider<T> provider)
    {
        super(name, provider);
        setProxy();
    }

    /**
     * Overwrites the default implementation such that only the chartsDir is set but the directory / file is not
     * created.
     */
    @Override
    public void setChartDir(final File chartsDir)
    {
        setProxy();
        proxy.setChartDir(chartsDir);
    }

    /**
     * Overwrites the default implementation such that only the csvDir is set but the directory / file is not created.
     */
    @Override
    public void setCsvDir(final File csvDir)
    {
        setProxy();
        proxy.setChartDir(csvDir);
    }

    /**
     * Gives access to the proxy which is used to avoid code duplication.
     * 
     * @return a proxy from which all values can be read
     */
    public Proxy getProxy()
    {
        return proxy;
    }

    /**
     * @return the bytesReceivedValueSet
     */
    public ValueSet getBytesReceivedValueSet()
    {
        return ReflectionUtils.readField(BasicTimerDataProcessor.class, this, "bytesReceivedValueSet");
    }

    /**
     * @return the distinctUrlHashCodeSet
     */
    public Set<Integer> getDistinctUrlHashCodeSet()
    {
        return ReflectionUtils.readField(BasicTimerDataProcessor.class, this, "distinctUrlHashCodeSet");
    }

    /**
     * @return the distinctUrlSet
     */
    public Set<String> getDistinctUrlSet()
    {
        return ReflectionUtils.readField(BasicTimerDataProcessor.class, this, "distinctUrlSet");
    }

    /**
     * @return the boundaries
     */
    public int[] getBoundaries()
    {
        return ReflectionUtils.readField(BasicTimerDataProcessor.class, this, "boundaries");
    }

    /**
     * @return the countPerSegment
     */
    public SegmentationValueSet getCountPerSegment()
    {
        return ReflectionUtils.readField(BasicTimerDataProcessor.class, this, "countPerSegment");
    }

    /**
     * @return the runTimeHistogramValueSet
     */
    public HistogramValueSet getRunTimeHistogramValueSet()
    {
        return ReflectionUtils.readField(BasicTimerDataProcessor.class, this, "runTimeHistogramValueSet");
    }

    /**
     * @return the bytesSentStatistics
     */
    public SummaryStatistics getBytesSentStatistics()
    {
        return ReflectionUtils.readField(BasicTimerDataProcessor.class, this, "bytesSentStatistics");
    }

    /**
     * @return the bytesReceivedStatistics
     */
    public SummaryStatistics getBytesReceivedStatistics()
    {
        return ReflectionUtils.readField(BasicTimerDataProcessor.class, this, "bytesReceivedStatistics");
    }

    /**
     * @return the connectTimeStatistics
     */
    public SummaryStatistics getConnectTimeStatistics()
    {
        return ReflectionUtils.readField(BasicTimerDataProcessor.class, this, "connectTimeStatistics");
    }

    /**
     * @return the sendTimeStatistics
     */
    public SummaryStatistics getSendTimeStatistics()
    {
        return ReflectionUtils.readField(BasicTimerDataProcessor.class, this, "sendTimeStatistics");
    }

    /**
     * @return the serverBusyTimeStatistics
     */
    public SummaryStatistics getServerBusyTimeStatistics()
    {
        return ReflectionUtils.readField(BasicTimerDataProcessor.class, this, "serverBusyTimeStatistics");
    }

    /**
     * @return the receiveTimeStatistics
     */
    public SummaryStatistics getReceiveTimeStatistics()
    {
        return ReflectionUtils.readField(BasicTimerDataProcessor.class, this, "receiveTimeStatistics");
    }

    /**
     * @return the timeToFirstBytesStatistics
     */
    public SummaryStatistics getTimeToFirstBytesStatistics()
    {
        return ReflectionUtils.readField(BasicTimerDataProcessor.class, this, "timeToFirstBytesStatistics");
    }

    /**
     * @return the timeToLastBytesStatistics
     */
    public SummaryStatistics getTimeToLastBytesStatistics()
    {
        return ReflectionUtils.readField(BasicTimerDataProcessor.class, this, "timeToLastBytesStatistics");
    }

    /**
     * Sets the proxy with the current instance as data processor if it is <code>null</code>.
     */
    private void setProxy()
    {
        if (proxy == null)
        {
            proxy = new Proxy(this);
        }
    }
}
