/*
 * Copyright (c) 2005-2025 Xceptance Software Technologies GmbH
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

import java.util.Timer;
import java.util.TimerTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xceptance.xlt.api.engine.AbstractCustomSampler;
import com.xceptance.xlt.api.engine.CustomValue;
import com.xceptance.xlt.api.engine.GlobalClock;
import com.xceptance.xlt.api.engine.Session;
import com.xceptance.xlt.api.util.XltLogger;

/**
 * @author Matthias Ullrich (Xceptance Software Technologies GmbH)
 */
public class CustomSamplerRunner extends Thread
{
    private static final Logger LOG = LoggerFactory.getLogger(CustomSamplerRunner.class);

    private final AbstractCustomSampler sampler;

    private boolean isClosed = false;

    /**
     * @param sampler
     *            sampler to run
     */
    public CustomSamplerRunner(final AbstractCustomSampler sampler, final ThreadGroup threadGroup)
    {
        super(threadGroup, threadGroup.getName() + "_" + sampler.getName());
        this.sampler = sampler;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void run()
    {
        // start wake up timer
        final Timer t = new Timer();
        t.schedule(new WakeUpService(this), 0, sampler.getInterval());

        sampler.initialize();

        // register the shutdown hook
        final ShutdownHook shutdownHook = new ShutdownHook();
        shutdownHook.setPriority(Thread.MAX_PRIORITY);
        Runtime.getRuntime().addShutdownHook(shutdownHook);

        while (!isClosed())
        {
            try
            {
                // get sample
                final CustomValue sample = new CustomValue(sampler.getName());
                sample.setTime(GlobalClock.millis());
                sample.setValue(sampler.execute());

                // log sample
                Session.getCurrent().getDataManager().logDataRecord(sample);
            }
            catch (final Exception e)
            {
                LOG.error("Failed to invoke custom sampler", e);
            }

            // sleep
            try
            {
                synchronized (this)
                {
                    this.wait();
                }

            }
            catch (final InterruptedException e)
            {
                XltLogger.runTimeLogger.error("I can't get no sleep.", e);
            }
        }
    }

    /**
     * Set sampler closed.
     */
    private synchronized void setClosed()
    {
        isClosed = true;
    }

    /**
     * Get sampler close status.
     *
     * @return <code>true</code> if sampler was closed, <code>false</code> otherwise
     */
    private synchronized boolean isClosed()
    {
        return isClosed;
    }

    /**
     * Wakes up this sampler.
     */
    protected class WakeUpService extends TimerTask
    {
        private final Thread samplerThread;

        /**
         * Create a {@link WakeUpService} instance.
         *
         * @param samplerThread
         */
        public WakeUpService(final Thread samplerThread)
        {
            this.samplerThread = samplerThread;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void run()
        {
            synchronized (samplerThread)
            {
                samplerThread.notify();
            }
        }
    }

    /**
     * Shutdown hook that aborts a running load test.
     */
    class ShutdownHook extends Thread
    {
        /**
         * {@inheritDoc}
         */
        @Override
        public void run()
        {
            setClosed();
            sampler.shutdown();
        }
    }
}
