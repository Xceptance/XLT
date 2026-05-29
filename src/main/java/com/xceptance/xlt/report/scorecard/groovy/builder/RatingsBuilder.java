package com.xceptance.xlt.report.scorecard.groovy.builder;

import java.util.ArrayList;
import java.util.List;

import com.xceptance.xlt.report.scorecard.RatingDefinition;

import groovy.lang.Closure;
import groovy.lang.DelegatesTo;

/**
 * Groovy DSL builder for collecting multiple {@link RatingDefinition} entries.
 * <p>
 * This container builder is used within the top-level {@code ratings} section to define score-to-grade mappings. Each
 * {@code rating { }} block creates a new rating that is added to the collection.
 * </p>
 * <p>
 * Ratings are evaluated in the order they are defined. The first rating whose threshold value is greater than or equal
 * to the achieved score percentage is selected as the final grade.
 * </p>
 *
 * @see RatingDefinition
 * @see RatingBuilder
 */
public class RatingsBuilder
{
    /** Accumulated list of ratings built from nested rating {} blocks */
    private final List<RatingDefinition> rattings = new ArrayList<>();

    /**
     * Defines a single rating within this ratings section.
     * <p>
     * Creates a new {@link RatingBuilder}, applies the closure to configure the rating, builds it, and registers it
     * with the configuration.
     * </p>
     *
     * @param closure
     *            the closure defining rating properties using {@link RatingBuilder}
     * @return the builder for potential method chaining
     */
    public RatingBuilder rating(@DelegatesTo(RatingBuilder.class) Closure<?> closure)
    {
        RatingBuilder builder = new RatingBuilder();
        closure.setDelegate(builder);
        closure.setResolveStrategy(Closure.DELEGATE_FIRST);
        closure.call();

        RatingDefinition rating = builder.build();
        rattings.add(rating);

        return builder;
    }

    /**
     * Builds and returns all configured ratings as an array.
     *
     * @return array of {@link RatingDefinition} instances in definition order
     */
    public RatingDefinition[] build()
    {
        return rattings.toArray(new RatingDefinition[0]);
    }
}
