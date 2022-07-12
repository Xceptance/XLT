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
package com.xceptance.xlt.report.benchmark;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileType;

import com.xceptance.common.util.concurrent.DaemonThreadFactory;
import com.xceptance.xlt.api.util.XltLogger;
import com.xceptance.xlt.engine.util.TimerUtils;

/**
 * Processor for the chain file to log line to parsed log line via 
 * log line filter and transformation to finally the statistics part
 * where the report provider will collect there data
 * 
 * DataProcessor
 * +- file reading pool
 * +- line processing pool
 * -> StatisicsProcessor (single thread)
 *
 * @see DataReaderThread
 * @see DataParserThread
 * @see StatisticsProcessor
 */
public class DataProcessor
{
    /**
     * The executor dealing with the data record reader threads.
     */
    private final ExecutorService dataReaderExecutor;

    /**
     * Input directory.
     */
    private final FileObject inputDir;

    /**
     * Constructor.
     */
    public DataProcessor(final FileObject inputDir)
    {
        this.inputDir = inputDir;

        // create the reader executor
        dataReaderExecutor = Executors.newFixedThreadPool(ReportGeneratorBenchmarkTest.THREADS, new DaemonThreadFactory(i -> "DataReader-" + i, Thread.MAX_PRIORITY));
    }

    /**
     * Reads all the data records from the configured input directory.
     */
    public void readDataRecords()
    {
        try
        {
            List<FileObject> result = new ArrayList<>();
            for (final FileObject file : inputDir.getChildren())
            {
                if (file.getType() == FileType.FOLDER)
                {
                    result.addAll(readDataRecordsFromAgentDir(file));
                }
            }

            final long start = TimerUtils.getTime();

            final AtomicLong total = new AtomicLong();
            for (FileObject file : result)
            {
                dataReaderExecutor.execute(new DataReaderThread(file, total));
            }
            
            dataReaderExecutor.shutdown();
            while (!dataReaderExecutor.awaitTermination(60, TimeUnit.SECONDS));
            
            final long duration = TimerUtils.getTime() - start;
            final long linesPerSecond = Math.round((total.get() / (double) duration) * 1000l); 

            XltLogger.runTimeLogger.info(String.format("%,d records read - %,d ms - %,d lines/s", 
                                                       total.get(), 
                                                       duration,
                                                       linesPerSecond));
        }
        catch (final Exception e)
        {
            XltLogger.runTimeLogger.error("Failed to read data records", e);
        }
        finally
        {
        }
    }

    /**
     * Reads the timer files from the given agent directory.
     *
     * @param agentDir
     *            agent directory
     * @throws IOException
     *             thrown on I/O-Error
     */
    private List<FileObject> readDataRecordsFromAgentDir(final FileObject agentDir) throws Exception
    {
        List<FileObject> result = new ArrayList<>();
        for (final FileObject file : agentDir.getChildren())
        {
            if (file.getType() == FileType.FOLDER)
            {
                result.addAll(readDataRecordsFromTestCaseDir(file, agentDir.getName().getBaseName()));
            }
        }
        
        return result;
    }

    /**
     * Reads the timer files from the given test case directory.
     *
     * @param testCaseDir
     *            test case directory
     * @param agentName
     *            the associated agent
     * @throws Exception 
     * @throws IOException
     *             thrown on I/O-Error
     */
    private List<FileObject> readDataRecordsFromTestCaseDir(final FileObject testCaseDir, final String agentName) throws Exception
    {
        List<FileObject> result = new ArrayList<>();
        
        for (final FileObject file : testCaseDir.getChildren())
        {
            if (file.getType() == FileType.FOLDER)
            {
                result.add(file);
            }
        }
        
        return result;
    }

}
