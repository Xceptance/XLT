package posters.loadtest.samplers;

import com.xceptance.xlt.api.engine.AbstractCustomSampler;
import com.xceptance.xlt.api.util.XltRandom;

/**
 * To demonstrate custom samplers this class generates and logs just random values.
 */
public class ValueSamplerDemo extends AbstractCustomSampler
{
    public ValueSamplerDemo()
    {
        super();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initialize()
    {
        // initialize sampler
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double execute()
    {
        // generate random value based on the configured limits

        // get properties
        final String lowerLimitProp = getProperties().getProperty("generatedValueLowerLimit");
        final String upperLimitProp = getProperties().getProperty("generatedValueUpperLimit");

        // convert to integer
        try
        {
            final int lowerLimit = Integer.valueOf(lowerLimitProp);
            final int upperLimit = Integer.valueOf(upperLimitProp);

            return XltRandom.nextInt(lowerLimit, upperLimit) + XltRandom.nextDouble();

        }
        catch (final NumberFormatException e)
        {
            return 0;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void shutdown()
    {
        // clean up sampler
    }
}
