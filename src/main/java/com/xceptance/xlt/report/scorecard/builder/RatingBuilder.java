package com.xceptance.xlt.report.scorecard.builder;

import com.xceptance.xlt.report.scorecard.RatingDefinition;

/**
 * Groovy DSL builder for constructing {@link RatingDefinition} objects.
 * <p>
 * Ratings define score thresholds that map achieved point percentages to grade labels (e.g., "A", "B", "C"). They are
 * evaluated in order, and the first rating whose threshold value is greater than or equal to the achieved percentage is
 * selected.
 * </p>
 * <p>
 * Example usage in Groovy DSL:
 * </p>
 * 
 * <pre>{@code
 * rating {
 *     id "grade-a"
 *     name "Excellent"
 *     description "Achieved 90% or more"
 *     value 90.0
 *     failsTest false
 * }
 * }</pre>
 *
 * @see RatingDefinition
 * @see RatingsBuilder
 */
public class RatingBuilder
{
    /** Unique identifier for this rating (e.g., "grade-a", "grade-f") */
    private String id;

    /** Human-readable display name (e.g., "Excellent", "Needs Improvement") */
    private String name;

    /** Optional detailed description of what this rating means */
    private String description;

    /** Threshold percentage value; rating applies if score percentage <= this value */
    private double value;

    /** Whether this rating is active; disabled ratings are skipped during evaluation */
    private boolean enabled = true;

    /** If true, achieving this rating causes the overall test to fail */
    private boolean failsTest = false;

    /** If true, manually selects this rating bypassing point-based evaluation */
    private boolean active = false;

    /**
     * Sets the unique identifier for this rating.
     *
     * @param id
     *               the rating identifier, used for referencing and reporting
     */
    public void id(String id)
    {
        this.id = id;
    }

    /**
     * Sets the display name for this rating.
     *
     * @param name
     *                 human-readable name shown in reports
     */
    public void name(String name)
    {
        this.name = name;
    }

    /**
     * Sets an optional description for this rating.
     *
     * @param description
     *                        detailed explanation of the rating
     */
    public void description(String description)
    {
        this.description = description;
    }

    /**
     * Sets the percentage threshold for this rating.
     * <p>
     * Ratings are evaluated in ascending order by value. The first rating where the achieved score percentage is less than
     * or equal to this value will be selected.
     * </p>
     *
     * @param value
     *                  threshold percentage (0.0 to 100.0)
     */
    public void value(double value)
    {
        this.value = value;
    }

    /**
     * Enables or disables this rating.
     *
     * @param enabled
     *                    if false, this rating is skipped during evaluation
     */
    public void enabled(boolean enabled)
    {
        this.enabled = enabled;
    }

    /**
     * Sets whether achieving this rating should cause the test to fail.
     * <p>
     * This is typically set to true for low ratings (e.g., grade "F") to indicate that the scorecard result is not
     * acceptable.
     * </p>
     *
     * @param failsTest
     *                      if true, the overall test fails when this rating is assigned
     */
    public void failsTest(boolean failsTest)
    {
        this.failsTest = failsTest;
    }

    /**
     * Manually activates this rating, bypassing point-based evaluation.
     * <p>
     * When a rating is marked as active, it will be selected regardless of the achieved points percentage. If multiple
     * ratings are marked active, the first one in definition order is used.
     * </p>
     *
     * @param active
     *                   if true, this rating is manually selected
     */
    public void active(boolean active)
    {
        this.active = active;
    }

    /**
     * Builds and returns the configured {@link RatingDefinition}.
     *
     * @return a new RatingDefinition instance with all configured properties
     */
    public RatingDefinition build()
    {
        return new RatingDefinition(id, name, description, value, enabled, failsTest, active);
    }
}
