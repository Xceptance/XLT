/*
 * Copyright (c) 2005-2021 Xceptance Software Technologies GmbH
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
package com.xceptance.xlt.clientperformance;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.SystemUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.xceptance.common.lang.ThreadUtils;
import com.xceptance.common.util.ParseUtils;
import com.xceptance.xlt.api.engine.Session;
import com.xceptance.xlt.api.util.XltProperties;
import com.xceptance.xlt.engine.SessionImpl;

/**
 * Client-performance utility class.
 *
 * @author Hartmut Arlt (Xceptance Software Technologies GmbH)
 */
public final class ClientPerformanceUtils
{
    /**
     * Property used to configure the Xvfb screen.
     */
    private static final String XVFB_SCREEN_PROPERTY = "xlt.clientperformance.xvfb.screen";

    /**
     * Xvfb screen configuration (WxHxD).
     */
    private static final String XVFB_SCREEN_CONFIG;

    /**
     * Class logger.
     */
    private static final Log LOG = LogFactory.getLog(ClientPerformanceUtils.class);

    /**
     * Mutex used for lookup of unused X server display.
     */
    private static final Object mutex = new Object();

    /**
     * Internal storage of all Xvfb processes.
     */
    private static final List<Process> XVFB_PROCESSES = new ArrayList<>();

    /**
     * Maps the group of the currently running thread to the given X server display.
     */
    private static final ConcurrentHashMap<Thread, String> DISPLAYS = new ConcurrentHashMap<>();

    /**
     * Path to <em>Xvfb</em> executable.
     */
    private static final String PATH_TO_XVFB_EXE;

    static
    {
        // look up the xvfb binary
        final File exe = checkForExecutable("Xvfb");
        PATH_TO_XVFB_EXE = (exe != null) ? exe.getAbsolutePath() : null;

        XVFB_SCREEN_CONFIG = getScreenConfig();

        // register shutdown handler
        Runtime.getRuntime().addShutdownHook(new Thread(ClientPerformanceUtils.class.getSimpleName() + "-shutdown")
        {
            @Override
            public void run()
            {
                cleanUp();
            }
        });
    }

    /**
     * Returns the display of the current thread.
     *
     * @return this thread's display, or <code>null</code> if no virtual display is available
     */
    public static String getDisplay()
    {
        if (PATH_TO_XVFB_EXE == null)
        {
            return null;
        }

        final Thread thread = Thread.currentThread();
        String display = DISPLAYS.get(thread);
        if (display == null)
        {
            display = getFreeDisp();
            DISPLAYS.put(thread, display);
            LOG.debug("Using X display '" + display + "' for thread '" + thread.getName() + "'");
        }
        else
        {
            LOG.debug("Reusing X display '" + display + "' for thread '" + thread.getName() + "'");
        }

        return display;
    }

    /**
     * Retrieves the Xvfb screen configuration.
     * 
     * @return screen configuration in case it is valid and default Xvfb screen configuration (1600x1200x24) otherwise
     */
    private static String getScreenConfig()
    {
        final String defaultScreenConfig = "1600x1200x24";
        final String screenConfig = XltProperties.getInstance().getProperty(XVFB_SCREEN_PROPERTY);

        if (screenConfig != null)
        {
            final Matcher matcher = Pattern.compile("(\\d+)x(\\d+)x(\\d+)").matcher(screenConfig.trim());
            if (matcher.matches() && matcher.groupCount() == 3)
            {
                final int width = ParseUtils.parseInt(matcher.group(1), -1);
                final int height = ParseUtils.parseInt(matcher.group(2), -1);
                final int depth = ParseUtils.parseInt(matcher.group(3), -1);

                if (width > 0 && height > 0 && depth > 0 && depth % 8 == 0 && depth <= 32)
                {
                    return screenConfig;
                }

                LOG.error("Specified Xvfb screen configuration '" + screenConfig + "' is invalid; will use default of '" +
                          defaultScreenConfig + "'");
            }
        }
        return defaultScreenConfig;
    }

    /**
     * Checks for an executable file with the given name and returns its absolute path if found and <code>null</code>
     * otherwise.
     *
     * @param executable
     *            the name of the executable file to get the absolute path for
     * @return absolute path of executable file if found and <code>null</code> otherwise
     */
    private static File checkForExecutable(final String executable)
    {
        if (SystemUtils.IS_OS_UNIX)
        {
            try (final ByteArrayOutputStream bos = new ByteArrayOutputStream())
            {
                final Process p = Runtime.getRuntime().exec("which " + executable);
                final int returnValue = p.waitFor();
                IOUtils.copy(p.getInputStream(), bos);

                if (returnValue == 0)
                {
                    final String found = new String(bos.toByteArray()).trim();
                    final File exe = new File(found);
                    if (exe.exists() && exe.canRead() && exe.canExecute())
                    {
                        return exe;
                    }
                }
            }
            catch (final Exception e)
            {
                LOG.error("Failed to check for executable: " + executable, e);
            }
        }

        return null;
    }

    /**
     * Returns the next free display.
     *
     * @return next free display
     */
    private static String getFreeDisp()
    {
        final File tmpDir = new File(SystemUtils.JAVA_IO_TMPDIR);
        synchronized (mutex)
        {
            int disp = 39;
            while (true)
            {
                ++disp;

                final File xlock = new File(tmpDir, ".X" + disp + "-lock");
                if (xlock.exists())
                {
                    continue;
                }

                try
                {
                    final Process p = startXvfb(disp, xlock);
                    if (p != null)
                    {
                        XVFB_PROCESSES.add(p);
                        LOG.info("Started new Xvfb process using display " + disp);

                        break;
                    }
                    else
                    {
                        LOG.info("Failed to start Xvfb process using display " + disp);
                    }
                }
                catch (final IOException e)
                {
                    LOG.error("Failed to start new Xvfb process", e);
                }
            }

            return ":".concat(Integer.toString(disp));
        }
    }

    /**
     * Attempt to start a new <em>Xvfb</em> process using the given number.
     *
     * @param display
     *            the display number to use
     * @param xlock
     *            the lock file to check for existence
     * @return the handle to the <em>Xvfb</em> process it could be started successfully, <code>null</code> otherwise
     * @throws IOException
     *             thrown when process failed to start at all
     */
    private static Process startXvfb(final int display, final File xlock) throws IOException
    {
        final File xvfbOut = new File(((SessionImpl) Session.getCurrent()).getResultsDirectory(), "xvfb-out.log");
        final Process p = new ProcessBuilder(PATH_TO_XVFB_EXE, ":" + display, "-ac", "-noreset", "-screen", "0",
                                             XVFB_SCREEN_CONFIG).redirectErrorStream(true).redirectOutput(xvfbOut).start();
        // wait some time
        ThreadUtils.sleep(1500);
        // ... before requesting the process' exit status
        try
        {
            p.exitValue();
            LOG.error("Xvfb process exited prematurely (display: " + display + ")!");

            return null;
        }
        // Process should still be running!
        catch (final IllegalThreadStateException itse)
        {
            // Eat it.
        }

        final long endTime = System.currentTimeMillis() + 5000;
        do
        {
            if (xlock.exists())
            {
                return p;
            }

            ThreadUtils.sleep(250);
        }
        while (System.currentTimeMillis() < endTime);

        p.destroy();
        LOG.error("Xvfb process was not ready to accept connections within 5s (display: " + display + ")!");

        return null;
    }

    /**
     * Clean-up procedure.
     */
    private static void cleanUp()
    {
        LOG.debug("xvfb cleanup");
        for (final Process p : XVFB_PROCESSES)
        {
            try
            {
                p.destroy();
            }
            catch (final Throwable t)
            {
                LOG.error("Failed to destroy xvfb process during shutdown", t);
            }
        }
    }

    /**
     * Default constructor. Declared private to prevent external instantiation.
     */
    private ClientPerformanceUtils()
    {
        // Empty
    }
}
