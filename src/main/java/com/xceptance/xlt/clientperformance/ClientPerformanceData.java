package com.xceptance.xlt.clientperformance;

import java.util.ArrayList;
import java.util.List;

import com.xceptance.xlt.api.engine.PageLoadTimingData;

public class ClientPerformanceData
{
    private final List<PageLoadTimingData> customDataList = new ArrayList<>();

    private final List<ClientPerformanceRequest> requestList = new ArrayList<>();

    public List<PageLoadTimingData> getCustomDataList()
    {
        return customDataList;
    }

    public List<ClientPerformanceRequest> getRequestList()
    {
        return requestList;
    }

}
