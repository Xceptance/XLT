/*
 * Copyright (c) 2005-2020 Xceptance Software Technologies GmbH
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
package com.xceptance.xlt.api.util;

import java.io.File;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.vfs2.FileObject;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.PropertyConfigurator;

import com.xceptance.common.util.RouteMessagesToLog4jHandler;
import com.xceptance.xlt.engine.XltExecutionContext;
import com.xceptance.xlt.util.XltPropertiesImpl;

/**
 * Class to define all global needed loggers and their properties.
 * 
 * @author Jörg Werner (Xceptance Software Technologies GmbH)
 */
public final class XltLogger
{
    /**
     * Logger category runtime for all common application related messages.
     */
    private static final String RUNTIME = "runtime";

    /**
     * The runtime logger.
     */
    public static final Logger runTimeLogger = Logger.getLogger(RUNTIME);

    static
    {
        // TODO: There is surely a better, more central place to initialize the log system!
        initLogging();
        RouteMessagesToLog4jHandler.install();

        XltPropertiesImpl.getInstance(true);
    }

    /**
     * Initializes the logging framework by loading the specified properties.
     * 
     * @param fileName
     *            the path name of the property file
     * @return <code>true</code> if setup was successful, <code>false</code> otherwise
     */
    public static boolean setupLogging(final String fileName)
    {
        // check for file existence
        if (!new File(fileName).exists())
        {
            // format error message
            final String errMsg = String.format("Problems during setup of property file '%s'. " + "This is not necessarily an error.",
                                                fileName);
            // print error message to error output stream
            System.err.println(errMsg);
            // return false to indicate failed setup
            return false;
        }

        // configure properties using given fileName
        PropertyConfigurator.configure(fileName);
        // return success code
        return true;
    }

    /**
     * Initializes the logging to a minimum before we add the properties. Ensures correct display of first runtime
     * errors.
     */
    public static void initLogging()
    {
        // get root logger
        final Logger rootLogger = Logger.getRootLogger();
        // check if in load-test mode and if log4j is already configured -> done here
        if (StringUtils.isNotEmpty(System.getProperty("log4j.configuration")) && rootLogger.getAllAppenders().hasMoreElements())
        {
            return;
        }
        // no -> maybe we run from inside eclipse
        rootLogger.removeAllAppenders();

        // try to load log4j dev settings in <config> directory of configured testsuite
        final String FILENAME1 = getFilename1();

        // log4j properties file of sample test suite
        final String FILENAME2 = "samples/testsuite-posters/config/dev-log4j.properties";

        // try to setup logging framework using 1st property file
        if (FILENAME1 != null && setupLogging(FILENAME1))
        {
            if (XltLogger.runTimeLogger.isInfoEnabled())
            {
                XltLogger.runTimeLogger.info("Logging property file location automatically set: " + FILENAME1);
            }
        }
        // try to setup logging framework using 2nd property file
        else if (setupLogging(FILENAME2))
        {
            if (XltLogger.runTimeLogger.isInfoEnabled())
            {
                XltLogger.runTimeLogger.info("Logging property file location automatically set: " + FILENAME2);
            }
        }
        // fallback -> configure logging framework using no property file
        else
        {
            setupLogging();

            XltLogger.runTimeLogger.warn("Logging property file not found. Setting defaults.");
        }

    }

    private static void setupLogging()
    {
        // configure the root logger
        Logger.getRootLogger().setLevel(Level.INFO);
        Logger.getRootLogger().addAppender(new ConsoleAppender(new PatternLayout("[%d{HH:mm:ss,SSS}] %-5p [%t] - %m\n")));

        // for easy debugging with Eclipse
        runTimeLogger.setLevel(Level.DEBUG);
    }

    private static String getFilename1()
    {
        final FileObject configDir = XltExecutionContext.getCurrent().getTestSuiteConfigDir();
        if (configDir != null)
        {
            return new File(configDir.getName().getPath(), "dev-log4j.properties").getAbsolutePath();
        }
        else
        {
            XltLogger.runTimeLogger.warn("Unable to get access to configuration directory");
        }

        return null;
    }
}
