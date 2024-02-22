/*
 * Copyright (c) 2005-2023 Xceptance Software Technologies GmbH
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

/**
 * The score, rating, and supplemental data calculated for a certain web vital and a certain action.
 */
public class WebVitalReport
{
    public enum Rating
    {
        good,
        improve,
        poor
    }

    public final BigDecimal score;

    public final Rating rating;

    public final int goodCount;

    public final int improveCount;

    public final int poorCount;

    public WebVitalReport(BigDecimal score, Rating rating, int goodCount, int improveCount, int poorCount)
    {
        this.score = score;
        this.rating = rating;
        this.goodCount = goodCount;
        this.improveCount = improveCount;
        this.poorCount = poorCount;
    }
}
