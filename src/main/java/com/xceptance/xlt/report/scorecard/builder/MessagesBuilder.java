package com.xceptance.xlt.report.scorecard.builder;

import java.util.HashMap;
import java.util.Map;

/**
 * Groovy DSL builder for defining success and failure messages.
 * <p>
 * This builder collects optional messages that are displayed when a rule or group passes or fails. Messages are used in
 * reports to provide human-readable feedback about the evaluation result.
 * </p>
 * <p>
 * Example usage in Groovy DSL:
 * </p>
 * 
 * <pre>{@code
 * messages {
 *     success "All checks passed successfully"
 *     fail "One or more checks failed"
 * }
 * }</pre>
 *
 * @see RuleBuilder
 * @see GroupBuilder
 */
public class MessagesBuilder
{
    /** Message shown when the parent rule/group passes */
    private String successMessage;

    /** Message shown when the parent rule/group fails */
    private String failMessage;

    /**
     * Sets the success message.
     *
     * @param message
     *                    text shown when the evaluation passes
     */
    public void success(String message)
    {
        this.successMessage = message;
    }

    /**
     * Sets the failure message.
     *
     * @param message
     *                    text shown when the evaluation fails
     */
    public void fail(String message)
    {
        this.failMessage = message;
    }

    /**
     * Builds and returns the configured messages as a map.
     * <p>
     * Only non-null messages are included in the returned map.
     * </p>
     *
     * @return map with "success" and/or "fail" keys, or empty if no messages set
     */
    public Map<String, String> build()
    {
        Map<String, String> messages = new HashMap<>();
        if (successMessage != null)
        {
            messages.put("success", successMessage);
        }
        if (failMessage != null)
        {
            messages.put("fail", failMessage);
        }
        return messages;
    }
}
