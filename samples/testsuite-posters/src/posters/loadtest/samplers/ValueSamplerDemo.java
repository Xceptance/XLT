/*
 * Copyright (c) 2005-2022 Xceptance Software Technologies GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
