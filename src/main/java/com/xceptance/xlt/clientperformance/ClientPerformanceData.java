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
package com.xceptance.xlt.clientperformance;

import java.util.ArrayList;
import java.util.List;

import com.xceptance.xlt.api.engine.PageLoadTimingData;
import com.xceptance.xlt.api.engine.WebVitalData;

public class ClientPerformanceData
{
    private final List<PageLoadTimingData> customDataList = new ArrayList<>();

    private final List<ClientPerformanceRequest> requestList = new ArrayList<>();

    private final List<WebVitalData> webVitalsList = new ArrayList<>();

    public List<PageLoadTimingData> getCustomDataList()
    {
        return customDataList;
    }

    public List<ClientPerformanceRequest> getRequestList()
    {
        return requestList;
    }

    public List<WebVitalData> getWebVitalsList()
    {
        return webVitalsList;
    }
}
