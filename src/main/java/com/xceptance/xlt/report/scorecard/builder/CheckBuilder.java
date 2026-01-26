package com.xceptance.xlt.report.scorecard.builder;

import com.xceptance.xlt.report.scorecard.RuleDefinition.Check;
import com.xceptance.xlt.report.scorecard.Status;

/**
 * Groovy DSL builder for constructing {@link Check} objects within a rule.
 * <p>
 * A check represents a single validation condition within a rule. Each check uses an XPath selector to extract a value
 * from the test report XML and applies a condition to determine pass/fail status. Multiple checks within a rule are
 * evaluated in order; the rule fails if any enabled check fails.
 * </p>
 * <p>
 * Checks can operate in two modes:
 * </p>
 * <ul>
 * <li><b>Dynamic evaluation:</b> Uses selector + condition to evaluate against the XML document</li>
 * <li><b>Manual status:</b> Uses status/value/message to set a predetermined result</li>
 * </ul>
 * <p>
 * Example usage in Groovy DSL:
 * </p>
 * 
 * <pre>{@code
 * check {
 *     selectorId "errors"        // Reference a predefined selector
 *     condition "= 0"            // XPath condition to evaluate
 *     displayValue true          // Show the extracted value in reports
 *     formatter "%.0f"           // Optional: format the displayed value
 * }
 * }</pre>
 *
 * @see Check
 * @see ChecksBuilder
 * @see RuleBuilder
 */
public class CheckBuilder
{
    /** Inline XPath expression to select a value from the XML document */
    private String selector;

    /** Reference to a predefined selector by ID (alternative to inline selector) */
    private String selectorId;

    /** XPath condition expression to evaluate against the selected value */
    private String condition;

    /** Whether this check is active; disabled checks are skipped */
    private boolean enabled = true;

    /** Whether to include the extracted value in report output */
    private boolean displayValue = true;

    /** Zero-based index of this check within its parent rule (set by ChecksBuilder) */
    private int index = 0;

    /** Optional printf-style format string for displaying the value (e.g., "%.2f%%") */
    private String formatter;

    /** Manual status override; if set, bypasses normal selector/condition evaluation */
    private Status manualStatus;

    /** Manual value to display when using manual status */
    private String manualValue;

    /** Manual error message when using manual status with FAILED or ERROR */
    private String manualErrorMessage;

    /**
     * Sets an inline XPath selector expression.
     * <p>
     * Use this for one-off selectors. For reusable selectors, prefer {@link #selectorId(String)}.
     * </p>
     *
     * @param selector
     *                     XPath expression to extract a value from the XML document
     */
    public void selector(String selector)
    {
        this.selector = selector;
    }

    /**
     * References a predefined selector by its ID.
     * <p>
     * The referenced selector must be defined in the {@code selectors} section of the configuration. This is preferred over
     * inline selectors for commonly used XPath expressions.
     * </p>
     *
     * @param selectorId
     *                       ID of a selector defined in the selectors section
     */
    public void selectorId(String selectorId)
    {
        this.selectorId = selectorId;
    }

    /**
     * Sets the condition expression to evaluate against the selected value.
     * <p>
     * The condition is an XPath expression that should evaluate to a boolean. Comparison operators (=, !=, &lt;, &gt;,
     * etc.) are automatically prefixed with the context item if they appear at the start of the expression.
     * </p>
     * <p>
     * Examples:
     * </p>
     * <ul>
     * <li>{@code "= 0"} - value must equal 0</li>
     * <li>{@code "> 99.5"} - value must be greater than 99.5</li>
     * <li>{@code "!= 'error'"} - value must not equal 'error'</li>
     * </ul>
     *
     * @param condition
     *                      XPath condition expression
     */
    public void condition(String condition)
    {
        this.condition = condition;
    }

    /**
     * Enables or disables this check.
     *
     * @param enabled
     *                    if false, this check is skipped during evaluation
     */
    public void enabled(boolean enabled)
    {
        this.enabled = enabled;
    }

    /**
     * Controls whether the selected value is shown in report output.
     *
     * @param displayValue
     *                         if true, the extracted/formatted value appears in reports
     */
    public void displayValue(boolean displayValue)
    {
        this.displayValue = displayValue;
    }

    /**
     * Sets the format string for displaying the value.
     * <p>
     * Uses Java's String.format() syntax (printf-style).
     * </p>
     *
     * @param formatter
     *                      format string, e.g., "%.2f" for two decimal places
     */
    public void formatter(String formatter)
    {
        this.formatter = formatter;
    }

    /**
     * Sets a manual status, bypassing normal selector/condition evaluation.
     * <p>
     * When set, the check always returns this status regardless of the XML content. This is useful for placeholder checks
     * or hardcoded pass/fail scenarios.
     * </p>
     *
     * @param status
     *                   one of "PASSED", "FAILED", "ERROR", or "SKIPPED"
     */
    public void status(String status)
    {
        this.manualStatus = Status.valueOf(status.toUpperCase());
    }

    /**
     * Sets a manual value to display when using manual status.
     *
     * @param value
     *                  the value to show in reports
     */
    public void value(String value)
    {
        this.manualValue = value;
    }

    /**
     * Sets a manual error message when using manual FAILED or ERROR status.
     *
     * @param message
     *                    error description to show in reports
     */
    public void message(String message)
    {
        this.manualErrorMessage = message;
    }

    /**
     * Sets the index of this check within its parent rule.
     * <p>
     * Package-private: called by {@link ChecksBuilder} to assign sequential indices.
     * </p>
     *
     * @param index
     *                  zero-based index
     */
    void index(int index)
    {
        this.index = index;
    }

    /**
     * Builds and returns the configured {@link Check}.
     *
     * @return a new Check instance with all configured properties
     */
    public Check build()
    {
        return new Check(index, selector, selectorId, condition, enabled, displayValue, formatter, manualStatus, manualValue,
                         manualErrorMessage);
    }
}
