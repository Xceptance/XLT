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
package com.xceptance.xlt.report;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.GZIPInputStream;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xceptance.xlt.common.XltConstants;

/**
 * Reads lines from the test result files of a certain test user.
 */
class DataRecordReader implements Runnable
{
    /**
     * The maximum number of lines in a chunk.
     */
    private static final int CHUNK_SIZE = 1000;

    /**
     * Class logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger(LogReader.class);

    /**
     * Maps the start time of an action to the action name. This data structure is defined here (as it is tied to a
     * certain input directory/test user), but will be maintained and used by the parser threads.
     */
    private final ConcurrentSkipListMap<Long, String> actionNames = new ConcurrentSkipListMap<Long, String>();

    /**
     * The name of the agent the test user was run on.
     */
    private final String agentName;

    /**
     * The directory with the test user's result files.
     */
    private final FileObject directory;

    /**
     * The name of the test case the test user was executing.
     */
    private final String testCaseName;

    /**
     * The global line counter.
     */
    private final AtomicInteger totalLineCounter;

    /**
     * The instance number of the test user.
     */
    private final String userNumber;

    /**
     * The dispatcher that coordinates result processing.
     */
    private final Dispatcher dispatcher;

    /**
     * Constructor.
     *
     * @param directory
     *            the directory with the test user's result files
     * @param agentName
     *            the name of the agent the test user was run on
     * @param testCaseName
     *            the name of the test case the test user was executing
     * @param userNumber
     *            the instance number of the test user.
     * @param totalLineCounter
     *            the global line counter
     * @param dispatcher
     *            the dispatcher that coordinates result processing
     */
    public DataRecordReader(final FileObject directory, final String agentName, final String testCaseName, final String userNumber,
                            final AtomicInteger totalLineCounter, final Dispatcher dispatcher)
    {
        this.directory = directory;
        this.agentName = agentName;
        this.testCaseName = testCaseName;
        this.userNumber = userNumber;
        this.totalLineCounter = totalLineCounter;
        this.dispatcher = dispatcher;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void run()
    {
        try
        {
            dispatcher.beginReading();

            readLogsFromTestUserDir();
        }
        catch (final Exception e)
        {
            final String msg = String.format("Failed to read test results from directory '%s': %s", directory, e);
            LOG.error(msg);
            System.out.println(msg);
        }
        finally
        {
            dispatcher.finishedReading();
        }
    }

    /**
     * Reads all the timer files from the test user directory.
     *
     * @throws IOException
     *             thrown on I/O-Error
     */
    private void readLogsFromTestUserDir() throws Exception
    {
        final ArrayList<FileObject> regularTimerFiles = new ArrayList<FileObject>();
        final ArrayList<FileObject> clientPerformanceTimerFiles = new ArrayList<FileObject>();

        // get all readable files
        for (final FileObject file : directory.getChildren())
        {
            if (file.getType() == FileType.FILE && file.isReadable())
            {
                final String fileName = file.getName().getBaseName();

                // timers.csv and timers.csv.gz
                if (XltConstants.TIMER_FILENAME_PATTERNS.stream().anyMatch(r -> r.asPredicate().test(fileName)))
                {
                    // remember regular timer files for later processing
                    regularTimerFiles.add(file);
                }
                // timer-wd-<sessionid>.csv[.gz] (for backward compatibility with XLT < 4.8)
                else if (XltConstants.CPT_TIMER_FILENAME_PATTERNS.stream().anyMatch(r -> r.asPredicate().test(fileName)))
                {
                    // remember client performance timer files for later processing
                    clientPerformanceTimerFiles.add(file);
                }
            }
        }

        // check whether we have client performance timer files at all
        boolean haveClientPerformanceTimerFiles = !clientPerformanceTimerFiles.isEmpty();

        // process regular timer files first (to collect action names)
        for (final FileObject file : regularTimerFiles)
        {
            // collect action names only if we have client performance data
            readTimerLog(file, haveClientPerformanceTimerFiles, false);
        }

        // process client performance timer files *after* the regular timer files
        if (haveClientPerformanceTimerFiles)
        {
            for (final FileObject file : clientPerformanceTimerFiles)
            {
                readTimerLog(file, false, true);
            }
        }
    }

    /**
     * Reads the given timer file line by line.
     *
     * @param file
     *            the file to read
     * @param collectActionNames
     *            whether action names should be collected
     * @param adjustTimerName
     *            whether timer names should be adjusted
     */
    private void readTimerLog(final FileObject file, final boolean collectActionNames, final boolean adjustTimerName)
    {
        // that costs a lot of time, no idea why... real async logger might be an option, LOG.info did not help
//        System.out.printf("Reading file '%s' ...%n", file);
//        LOG.info(String.format("Reading file '%s' ...", file));

        // if we have an gz extension, we will try to decompress it while reading
        final boolean isCompressed = "gz".equalsIgnoreCase(file.getName().getExtension());

        try (final BufferedReader reader = new BufferedReader(new InputStreamReader(isCompressed ? new GZIPInputStream(file.getContent()
                                                                                                                           .getInputStream())
                                                                                                 : file.getContent().getInputStream(),
                                                                                    XltConstants.UTF8_ENCODING)))
        {
            List<String> lines = new ArrayList<String>(CHUNK_SIZE);
            int baseLineNumber = 1;  // let line numbering start at 1
            int linesRead = 0;

            // read the file line-by-line
            String line = reader.readLine();
            while (line != null)
            {
                linesRead++;
                lines.add(line);

                if (linesRead == CHUNK_SIZE)
                {
                    // the chunk is full -> deliver it
                    buildAndSubmitLineChunk(lines, baseLineNumber, file, collectActionNames, adjustTimerName);

                    // start a new chunk
                    lines = new ArrayList<String>(CHUNK_SIZE);
                    baseLineNumber += linesRead;

                    totalLineCounter.addAndGet(linesRead);

                    linesRead = 0;
                }

                line = reader.readLine();
            }

            // deliver any remaining lines
            if (linesRead > 0)
            {
                buildAndSubmitLineChunk(lines, baseLineNumber, file, collectActionNames, adjustTimerName);
                totalLineCounter.addAndGet(linesRead);
            }
        }
        catch (final Exception ex)
        {
            final String msg = String.format("Failed to read file '%s': %s\n", file, ex.getMessage());
            LOG.error(msg);
            System.out.println(msg);
        }
    }

    private void buildAndSubmitLineChunk(final List<String> lines, final int baseLineNumber, final FileObject file,
                                         final boolean collectActionNames, final boolean adjustTimerName)
        throws InterruptedException
    {
        final LineChunk lineChunk = new LineChunk(lines, baseLineNumber, file, agentName, testCaseName, userNumber, collectActionNames,
                                                  adjustTimerName, actionNames);
        dispatcher.addNewLineChunk(lineChunk);
    }
}
