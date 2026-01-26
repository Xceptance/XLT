package com.xceptance.xlt.report.scorecard.builder;

import com.xceptance.xlt.report.scorecard.Configuration;
import com.xceptance.xlt.report.scorecard.RatingDefinition;

import groovy.lang.Closure;
import groovy.lang.DelegatesTo;

/**
 * Groovy DSL builder for collecting multiple {@link RatingDefinition} entries.
 * <p>
 * This container builder is used within the top-level {@code ratings} section to define score-to-grade mappings. Each
 * {@code rating { }} block creates a new rating that is immediately registered with the {@link Configuration}.
 * </p>
 * <p>
 * Ratings are evaluated in the order they are defined. The first rating whose threshold value is greater than or equal
 * to the achieved score percentage is selected as the final grade.
 * </p>
 *
 * @see RatingDefinition
 * @see RatingBuilder
 * @see ScorecardBuilder
 */
public class RatingsBuilder
{
    /** Configuration to register ratings with */
    private final Configuration config;

    /**
     * Creates a new RatingsBuilder that adds ratings to the given configuration.
     *
     * @param config
     *                   the configuration to populate with ratings
     */
    public RatingsBuilder(Configuration config)
    {
        this.config = config;
    }

    /**
     * Defines a single rating within this ratings section.
     * <p>
     * Creates a new {@link RatingBuilder}, applies the closure to configure the rating, builds it, and registers it with
     * the configuration.
     * </p>
     *
     * @param closure
     *                    the closure defining rating properties using {@link RatingBuilder}
     * @return the builder for potential method chaining
     * @throws RuntimeException
     *                              if the rating cannot be added
     */
    public RatingBuilder rating(@DelegatesTo(RatingBuilder.class) Closure<?> closure)
    {
        RatingBuilder builder = new RatingBuilder();
        closure.setDelegate(builder);
        closure.setResolveStrategy(Closure.DELEGATE_FIRST);
        closure.call();
        RatingDefinition rating = builder.build();
        try
        {
            config.addRating(rating);
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
        return builder;
    }
}
