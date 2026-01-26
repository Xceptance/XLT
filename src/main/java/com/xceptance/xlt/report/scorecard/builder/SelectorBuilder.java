package com.xceptance.xlt.report.scorecard.builder;

import com.xceptance.xlt.report.scorecard.SelectorDefinition;
import groovy.lang.Closure;
import groovy.lang.DelegatesTo;

/**
 * Groovy DSL builder for constructing {@link SelectorDefinition} objects.
 * <p>
 * Selectors define reusable XPath expressions that can be referenced by rules via their ID. This avoids repeating
 * complex XPath expressions throughout the configuration and makes it easier to update them in one place.
 * </p>
 * <p>
 * Example usage in Groovy DSL:
 * </p>
 * 
 * <pre>{@code
 * selector {
 *     id "error_count"
 *     expression "//testreport/summary/errors"
 *     comment "Selects the error count from the test summary"
 * }
 * }</pre>
 *
 * @see SelectorDefinition
 * @see SelectorsBuilder
 */
public class SelectorBuilder
{
    /** Unique identifier used to reference this selector from rules */
    private String id;

    /** The XPath expression to evaluate against the XML document */
    private String expression;

    /** Optional comment describing what this selector does (for documentation) */
    private String comment;

    /**
     * Sets the unique identifier for this selector.
     *
     * @param id
     *               the selector identifier, used in rule selectorId references
     * @return this builder for method chaining
     */
    public SelectorBuilder id(String id)
    {
        this.id = id;
        return this;
    }

    /**
     * Sets the XPath expression for this selector.
     *
     * @param expression
     *                       XPath expression to extract values from the XML document
     * @return this builder for method chaining
     */
    public SelectorBuilder expression(String expression)
    {
        this.expression = expression;
        return this;
    }

    /**
     * Sets an optional comment describing this selector.
     * <p>
     * Note: The comment is currently not stored in {@link SelectorDefinition} but can be used for documentation purposes in
     * the config file.
     * </p>
     *
     * @param comment
     *                    description of what the selector does
     * @return this builder for method chaining
     */
    public SelectorBuilder comment(String comment)
    {
        this.comment = comment;
        return this;
    }

    /**
     * Builds and returns the configured {@link SelectorDefinition}.
     * <p>
     * Note: The current SelectorDefinition constructor only uses id and expression. The comment field is not currently
     * persisted.
     * </p>
     *
     * @return a new SelectorDefinition instance
     */
    public SelectorDefinition build()
    {
        // Note: SelectorDefinition currently only stores id and expression.
        // The comment field exists for DSL documentation purposes but is not persisted.
        return new SelectorDefinition(id, expression);
    }
}
