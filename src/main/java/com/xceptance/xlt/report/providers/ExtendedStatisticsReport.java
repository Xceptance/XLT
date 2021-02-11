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

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * Represents some more statistics for a certain value.
 */
public class ExtendedStatisticsReport extends StatisticsReport
{
    /**
     * The total count.
     */
    public BigInteger totalCount;

    /**
     * The count per second.
     */
    public BigDecimal countPerSecond;

    /**
     * The count per minute.
     */
    public BigDecimal countPerMinute;

    /**
     * The count per hour.
     */
    public BigDecimal countPerHour;

    /**
     * The count per day.
     */
    public BigDecimal countPerDay;
}
