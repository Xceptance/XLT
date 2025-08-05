package com.xceptance.xlt.report.mergerules;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.xceptance.xlt.api.engine.RequestData;

public class RequestProcessing
{
    /**
     * Class logger.
     */
    private static final Log LOG = LogFactory.getLog(RequestProcessing.class);
    
    /**
     * The list of request processing rules to apply.
     */
    private final RequestProcessingRule[] requestProcessingRules;
    
    /**
     * Whether or not to remove indexes from request names.
     */
    private final boolean removeIndexesFromRequestNames;
    
    /**
     * The Constructor.
     * 
     * @param requestProcessingRules the rules to apply
     * @param removeIndexesFromRequestNames whether or not to remove indexes from request names
     */
    public RequestProcessing(final List<RequestProcessingRule> requestProcessingRules, final boolean removeIndexesFromRequestNames)
    {
        this.requestProcessingRules = requestProcessingRules.toArray(new RequestProcessingRule[requestProcessingRules.size()]);
        this.removeIndexesFromRequestNames = removeIndexesFromRequestNames;

        // this is a sanity test against programming errors and not user data input errors
        // we can start at 0!
        int lastId = -1;
        for (RequestProcessingRule rule : this.requestProcessingRules)
        {
            if (rule.getId() <= lastId)
            {
                throw new IllegalArgumentException("Request processing rules must be sorted by ID in ascending order.");
            }
            lastId = rule.getId();
        }
    }
    
    /**
     * Processes a request according to the configured request processing rules. 
     * Currently, this means renaming or discarding requests. Because we want to test it
     * we made it static public. This is not meant to be a public API, but rather a utility method.
     *
     * @param requestData
     *            the request data record
     * @param requestProcessingRules
     *            the rules to apply
     * @param removeIndexesFromRequestNames
     *            in case we want to clean the name too
     * @return the processed request data record, or <code>null</code> if the data record is to be discarded
     */
    public RequestData postprocess(final RequestData requestData)
    {
        // fix up the name first (Product.1.2 -> Product) if so configured
        // this can likely live in RequestData and act on XltCharBuffer instead String
        String requestName = requestData.getName();
        if (removeIndexesFromRequestNames)
        {
            final int firstDotPos = requestName.indexOf(".");
            if (firstDotPos > 0)
            {
                requestName = requestName.substring(0, firstDotPos);
                requestData.setName(requestName);
            }
        }

        // what is the next rule to process in case we want to jump on match or mismatch
        // we can optionally also drop or just stop
        int nextId = 0;
        RequestProcessingRule requestProcessingRule = null;
        
        try
        {
            for (int i = 0; i < requestProcessingRules.length; i++)
            {
                requestProcessingRule = requestProcessingRules[i];
                
                // shall we process this rule or jump ahead
                if (nextId <= requestProcessingRule.getId())
                {
                    // request data comes back indirectly modified if needed
                    nextId = requestProcessingRule.process(requestData);
                    if (nextId >= 0)
                    {
                        // we wish to continue with one of the next rules
                        continue;
                    }

                    // we want to stop here
                    if (nextId == RequestProcessingRule.STOP)
                    {
                        break;
                    }
                    else
                    {
                        // this leaves us with a only the "drop"
                        // if (nextId == RequestProcessingRule.DROP)
                        return null;
                    }
                }
            }

            // ok, we processed all rules for this dataset, get us the final hashcode for the name, because we need that
            // later; here because the cache is likely still hot, so this is less expensive
            requestData.getName().hashCode();
        }
        catch (final Throwable t)
        {
            final String msg = String.format("Failed to apply request merge rule: %s\n%s", requestProcessingRule, t);
            LOG.error(msg);

            // restore the request's original name
            requestData.setName(requestName);
        }

        return requestData;
    }
}
