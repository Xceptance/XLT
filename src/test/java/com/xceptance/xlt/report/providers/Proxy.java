/*
 * Copyright (c) 2005-2021 Xceptance Software Technologies GmbH
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

import com.xceptance.common.lang.ReflectionUtils;
import com.xceptance.xlt.report.util.MinMaxValueSet;
import com.xceptance.xlt.report.util.RuntimeHistogram;
import com.xceptance.xlt.report.util.SummaryStatistics;
import com.xceptance.xlt.report.util.ValueSet;

/**
 * @author Sebastian Oerding
 */
public class Proxy
{
    private final AbstractDataProcessor instance;

    public Proxy(final AbstractDataProcessor instance)
    {
        this.instance = instance;
    }

    /**
     * Overwrites the default implementation such that only the chartsDir is set but the directory / file is not
     * created.
     */
    public void setChartDir(final File chartsDir)
    {
        ReflectionUtils.writeField(AbstractDataProcessor.class, instance, "chartsDir", chartsDir);
    }

    /**
     * Overwrites the default implementation such that only the csvDir is set but the directory / file is not created.
     */
    public void setCsvDir(final File csvDir)
    {
        ReflectionUtils.writeField(AbstractDataProcessor.class, instance, "csvDir", csvDir);
    }

    /**
     * @return the countPerSecondValueSet
     */
    public ValueSet getCountPerSecondValueSet()
    {
        return ReflectionUtils.readField(BasicTimerDataProcessor.class, instance, "countPerSecondValueSet");
    }

    /**
     * @return the errorsPerSecondValueSet
     */
    public ValueSet getErrorsPerSecondValueSet()
    {
        return ReflectionUtils.readField(BasicTimerDataProcessor.class, instance, "errorsPerSecondValueSet");
    }

    /**
     * @return the runTimeStatistics
     */
    public SummaryStatistics getRunTimeStatistics()
    {
        return ReflectionUtils.readField(BasicTimerDataProcessor.class, instance, "runTimeStatistics");
    }

    /**
     * @return the runTimeHistogram
     */
    public RuntimeHistogram getRunTimeHistogram()
    {
        return ReflectionUtils.readField(BasicTimerDataProcessor.class, instance, "runTimeHistogram");
    }

    /**
     * @return the runTimeValueSet
     */
    public MinMaxValueSet getRunTimeValueSet()
    {
        return ReflectionUtils.readField(BasicTimerDataProcessor.class, instance, "runTimeValueSet");
    }

    /**
     * @return the totalErrors
     */
    public int getTotalErrors()
    {
        final Integer returnValue = ReflectionUtils.readField(BasicTimerDataProcessor.class, instance, "totalErrors");
        return returnValue.intValue();
    }

    /**
     * @return the minMaxValueSetSize
     */
    public int getMinMaxValueSetSize()
    {
        final Integer returnValue = ReflectionUtils.readField(BasicTimerDataProcessor.class, instance, "minMaxValueSetSize");
        return returnValue.intValue();
    }

    /**
     * @return the chartCapping
     */
    public int getChartCapping()
    {
        final Integer returnValue = ReflectionUtils.readField(BasicTimerDataProcessor.class, instance, "chartCapping");
        return returnValue.intValue();
    }
}
