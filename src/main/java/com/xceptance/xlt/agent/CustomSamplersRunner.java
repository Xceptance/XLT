/*
 * Copyright (c) 2005-2024 Xceptance Software Technologies GmbH
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
package com.xceptance.xlt.agent;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xceptance.xlt.api.engine.AbstractCustomSampler;
import com.xceptance.xlt.api.engine.Session;
import com.xceptance.xlt.api.util.XltProperties;
import com.xceptance.xlt.engine.SessionImpl;
import com.xceptance.xlt.util.PropertyHierarchy;

/**
 * @author Matthias Ullrich (Xceptance Software Technologies GmbH)
 */
public class CustomSamplersRunner extends Thread
{
    private static final Logger LOG = LoggerFactory.getLogger(CustomSamplersRunner.class);

    private final String CUSTOM_SAMPLER_DOMAIN = "com.xceptance.xlt.customSamplers";

    private final String CUSTOM_SAMPLER_CLASS = "class";

    private final String CUSTOM_SAMPLER_NAME = "name";

    private final String CUSTOM_SAMPLER_PROPERTIES = "property";

    private final String CUSTOM_SAMPLER_RUN_INTERVAL = "interval";

    private static final String THREAD_NAME_PREFIX = "CustomSampler";

    public static final String RESULT_DIRECTORY_NAME = THREAD_NAME_PREFIX;

    private final List<AbstractCustomSampler> samplers = new ArrayList<AbstractCustomSampler>();

    private final AgentInfo agentInfo;

    public CustomSamplersRunner(final AgentInfo agentInfo)
    {
        super(new ThreadGroup(THREAD_NAME_PREFIX), "CustomSamplersRunner");

        this.agentInfo = agentInfo;
    }

    /**
     * Initialize all samplers and run them.
     */
    @Override
    public void run()
    {
        // initialize all samplers
        init();

        // initialize the current thread's session
        final SessionImpl session = (SessionImpl) Session.getCurrent();
        session.setUserName(THREAD_NAME_PREFIX);
        session.setUserNumber(0);
        session.setAbsoluteUserNumber(0);
        session.setTotalUserCount(1);
        session.setUserCount(1);

        session.setAgentID(agentInfo.getAgentID());
        session.setAgentNumber(agentInfo.getAgentNumber());
        session.setTotalAgentCount(agentInfo.getTotalAgentCount());

        session.setLoadTest(true);

        // start all samplers
        for (final AbstractCustomSampler sampler : samplers)
        {
            final Thread t = new CustomSamplerRunner(sampler, Thread.currentThread().getThreadGroup());
            t.setDaemon(true);
            t.start();
        }
    }

    /**
     * read configuration and set up custom samplers
     */
    private void init()
    {
        // read configuration
        final PropertyHierarchy samplerConfigurations = new PropertyHierarchy(CUSTOM_SAMPLER_DOMAIN);
        samplerConfigurations.set(XltProperties.getInstance().getPropertiesForKey(CUSTOM_SAMPLER_DOMAIN));

        // initialize each sampler with its corresponding configuration properties
        final Set<String> samplerKeys = samplerConfigurations.getChildKeyFragments();
        for (final String samplerKey : samplerKeys)
        {
            final PropertyHierarchy property = samplerConfigurations.get(samplerKey);
            initSampler(property);
        }
    }

    /**
     * instantiate and initialize sampler
     * 
     * @param samplerConfiguration
     *            sampler configuration
     */
    private void initSampler(final PropertyHierarchy samplerConfiguration)
    {
        // identify the custom sampler class
        final String samplerClassName = samplerConfiguration.get(CUSTOM_SAMPLER_CLASS).getValue();

        // get the custom sampler properties from configuration
        final Properties samplerProperties = new Properties();
        {
            final PropertyHierarchy customSamplerProperties = samplerConfiguration.get(CUSTOM_SAMPLER_PROPERTIES);
            if (customSamplerProperties != null)
            {
                final Set<String> propertyKeys = customSamplerProperties.getChildKeyFragments();
                for (final String propertyKey : propertyKeys)
                {
                    samplerProperties.setProperty(propertyKey, customSamplerProperties.get(propertyKey).getValue());
                }
            }
        }

        // instantiate sampler
        AbstractCustomSampler customSampler = null;
        try
        {
            final Class<?> c = Class.forName(samplerClassName);
            customSampler = (AbstractCustomSampler) c.getDeclaredConstructor().newInstance();
        }
        catch (final Exception e)
        {
            LOG.error("Failed to create custom sampler", e);
        }

        if (customSampler != null)
        {
            // add configured properties
            customSampler.setProperties(samplerProperties);
            customSampler.setInterval(samplerConfiguration.get(CUSTOM_SAMPLER_RUN_INTERVAL).getValue());
            customSampler.setName(samplerConfiguration.get(CUSTOM_SAMPLER_NAME).getValue());

            samplers.add(customSampler);
        }
    }
}
