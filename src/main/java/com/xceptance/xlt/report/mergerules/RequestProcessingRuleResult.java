package com.xceptance.xlt.report.mergerules;

import com.xceptance.xlt.api.engine.RequestData;

/**
 * Bundles the result artifacts of a request processing rule. This includes:
 * <ol>
 * <li>the processed request data (may also be <code>null</code>)</li>
 * <li>a flag indicating whether request processing should stop or continue</li>
 * </ol>
 * 
 * @see RequestProcessingRule
 */
public class RequestProcessingRuleResult
{
    public final RequestData requestData;

    public final boolean stopRequestProcessing;

    public RequestProcessingRuleResult(final RequestData requestData, final boolean stopRequestProcessing)
    {
        this.requestData = requestData;
        this.stopRequestProcessing = stopRequestProcessing;
    }
}
