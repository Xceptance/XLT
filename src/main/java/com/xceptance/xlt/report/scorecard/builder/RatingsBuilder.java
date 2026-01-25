package com.xceptance.xlt.report.scorecard.builder;

import com.xceptance.xlt.report.scorecard.Configuration;
import com.xceptance.xlt.report.scorecard.RatingDefinition;

import groovy.lang.Closure;
import groovy.lang.DelegatesTo;

/**
 * Builder for a list of ratings.
 */
public class RatingsBuilder
{
    private final Configuration config;

    public RatingsBuilder(Configuration config)
    {
        this.config = config;
    }

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
