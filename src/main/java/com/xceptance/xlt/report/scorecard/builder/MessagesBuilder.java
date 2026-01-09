package com.xceptance.xlt.report.scorecard.builder;

import java.util.HashMap;
import java.util.Map;

/**
 * Builder for success/fail messages.
 */
public class MessagesBuilder
{
    private String successMessage;

    private String failMessage;

    public void success(String message)
    {
        this.successMessage = message;
    }

    public void fail(String message)
    {
        this.failMessage = message;
    }

    public Map<String, String> build()
    {
        Map<String, String> messages = new HashMap<>();
        if (successMessage != null)
            messages.put("success", successMessage);
        if (failMessage != null)
            messages.put("fail", failMessage);
        return messages;
    }
}
