/*
 * Copyright (c) 2005-2026 Xceptance Software Technologies GmbH
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
package com.xceptance.xlt.report.server;

import java.io.File;
import java.util.Collections;
import java.util.Objects;
import java.util.Properties;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.lang3.StringUtils;

import com.xceptance.common.io.fs.FsResource;
import com.xceptance.common.io.fs.VirtualFileSystem;
import com.xceptance.common.util.Console;
import com.xceptance.xlt.api.util.XltLogger;
import com.xceptance.xlt.common.XltConstants;
import com.xceptance.xlt.engine.XltEngine;
import com.xceptance.xlt.engine.XltExecutionContext;
import com.xceptance.xlt.report.DataProcessor;
import com.xceptance.xlt.report.DataRecordFactory;
import com.xceptance.xlt.report.ReportGeneratorConfiguration;
import com.xceptance.xlt.report.query.ColumnarQueryEngine;
import com.xceptance.xlt.report.storage.ChunkStorage;
import com.xceptance.xlt.report.storage.cache.CacheManager;
import com.xceptance.xlt.report.storage.chunk.RequestChunkHandler;
import com.xceptance.xlt.util.XltPropertiesImpl;

/**
 * Dedicated CLI entry point for launching the interactive ChunkDB Explorer UI server.
 * <p>
 * Mounts an existing {@code xlt-cache/} directory in sub-second time if valid, or executes Phase 1
 * log ingestion into {@code xlt-cache/} without running Phase 2 XML/HTML/chart generation.
 */
public final class ChunkExplorerMain
{
    public static final int DEFAULT_PORT = 8484;

    private static final String OPTION_PORT = "port";
    private static final String OPTION_PRESERVE_ALL_DETAILS = "preserve-all-details";
    private static final String OPTION_CACHE = "cache";
    private static final String OPTION_PRELOAD = "preload";
    private static final String OPTION_PROPERTY_DEFINITION = "D";
    private static final String OPTION_HELP = "help";

    private ChunkExplorerMain()
    {
    }

    public static Options createCommandLineOptions()
    {
        final Options options = new Options();

        final Option portOption = new Option("p", OPTION_PORT, true, "HTTP port to bind on 127.0.0.1 (default: 8484)");
        portOption.setArgName("port");
        options.addOption(portOption);

        final Option preserveAllDetails = new Option(null, OPTION_PRESERVE_ALL_DETAILS, false,
                                                     "preserves forensic request details (URL, request/response IDs, form data, raw name) for 100% of requests when building cache");
        options.addOption(preserveAllDetails);

        final Option cacheOption = new Option(null, OPTION_CACHE, false,
                                              "eagerly build in-memory column pointer and chunk hint index at startup");
        options.addOption(cacheOption);

        final Option preloadOption = new Option(null, OPTION_PRELOAD, false,
                                                "eagerly build in-memory column pointer and chunk hint index at startup");
        options.addOption(preloadOption);

        final Option propertyFilename = new Option(XltConstants.COMMANDLINE_OPTION_PROPERTY_FILENAME, true,
                                                   "property file that overrides the basic properties");
        propertyFilename.setArgName("property file");
        options.addOption(propertyFilename);

        final Option propertyDefinition = new Option(OPTION_PROPERTY_DEFINITION, true, "override a property from file");
        propertyDefinition.setValueSeparator('=');
        propertyDefinition.setArgName("property=value");
        propertyDefinition.setArgs(2);
        options.addOption(propertyDefinition);

        final Option help = new Option("h", OPTION_HELP, false, "print this usage help");
        options.addOption(help);

        return options;
    }

    /**
     * Resolves or builds a {@link ChunkStorage} instance from a results directory or direct {@code xlt-cache} directory.
     */
    public static LoadedDataset loadOrIngestDataset(final File targetDir, final File overridePropertyFile,
                                                    final Properties cliProps, final boolean preserveAllDetails)
        throws Exception
    {
        if (targetDir == null || !targetDir.isDirectory())
        {
            throw new IllegalArgumentException("Input directory does not exist or is not a directory: " + targetDir);
        }

        // Case 1: Pointed directly at an xlt-cache directory containing chunks.bin
        final File directChunksFile = new File(targetDir, ChunkStorage.CHUNKS_FILE_NAME);
        if (directChunksFile.isFile())
        {
            XltLogger.reportLogger.info("Mounting ChunkDB directly from cache directory: {}", targetDir.getAbsolutePath());
            final ChunkStorage storage = ChunkStorage.loadFromDirectory(targetDir);
            final File parentDir = Objects.requireNonNullElse(targetDir.getParentFile(), targetDir);
            final String detailMode = preserveAllDetails ? "full" : RequestChunkHandler.getRequestDetailsMode();
            return new LoadedDataset(storage, parentDir, Runtime.getRuntime().availableProcessors(), detailMode);
        }

        // Case 2: Pointed at a load test results directory
        XltEngine.get();
        try (final VirtualFileSystem vfs = VirtualFileSystem.open(targetDir.getAbsolutePath()))
        {
            final FsResource inputDir = vfs.getRoot();
            XltExecutionContext.getCurrent().setTestSuiteHomeDir(inputDir);

            final FsResource configDir = inputDir.resolve(XltConstants.RESULT_CONFIG_DIR);
            if (configDir != null && configDir.isDirectory())
            {
                XltExecutionContext.getCurrent().setTestSuiteConfigDir(configDir);
            }
            else
            {
                XltExecutionContext.getCurrent().setTestSuiteConfigDir(inputDir);
            }

            XltPropertiesImpl props;
            try
            {
                props = new XltPropertiesImpl(inputDir, configDir, false, false);
                XltEngine.reset(props);
            }
            catch (final Exception e)
            {
                props = new XltPropertiesImpl(inputDir, configDir, false, true);
            }

            final Properties effectiveCliProps = new Properties();
            if (cliProps != null)
            {
                effectiveCliProps.putAll(cliProps);
            }
            // Ensure ChunkDB cache is enabled
            effectiveCliProps.setProperty("com.xceptance.xlt.reportgenerator.dataCache.enabled", "true");
            if (preserveAllDetails)
            {
                effectiveCliProps.setProperty(ReportGeneratorConfiguration.PROP_REQUEST_DETAILS_MODE, "full");
                RequestChunkHandler.setRequestDetailsMode("full");
            }

            final ReportGeneratorConfiguration config =
                new ReportGeneratorConfiguration(props.getProperties(), overridePropertyFile, effectiveCliProps);
            config.setResultsDirectory(inputDir);
            config.setResultsDirectoryName(targetDir.getName());
            RequestChunkHandler.setRequestDetailsMode(config.getRequestDetailsMode());

            final CacheManager cacheManager = new CacheManager(targetDir, config);
            ChunkStorage storage = null;
            if (cacheManager.isCacheValid())
            {
                XltLogger.reportLogger.info(Console.horizontalBar());
                XltLogger.reportLogger.info("Warm ChunkDB cache detected. Mounting without re-parsing CSV logs...");
                storage = cacheManager.loadValidated();
            }

            if (storage == null)
            {
                XltLogger.reportLogger.info(Console.horizontalBar());
                XltLogger.reportLogger.info("No valid ChunkDB cache found. Running Phase 1 log ingestion into {}...",
                                            cacheManager.getCacheDirectory().getAbsolutePath());
                final DataRecordFactory dataRecordFactory = new DataRecordFactory(config.getDataRecordClasses());
                final DataProcessor logReader = new DataProcessor(config, inputDir, dataRecordFactory, 0L, Long.MAX_VALUE,
                                                                  Collections.emptyList(), null, null, null, null);
                logReader.readDataRecords();
                storage = ChunkStorage.loadFromDirectory(cacheManager.getCacheDirectory());
            }

            return new LoadedDataset(storage, targetDir, Runtime.getRuntime().availableProcessors(), config.getRequestDetailsMode());
        }
    }

    /**
     * Parses CLI arguments, loads or ingests the target dataset, and starts the {@link ChunkExplorerServer}.
     *
     * @param args
     *            command-line arguments
     * @param blockUntilShutdown
     *            {@code true} to register a JVM shutdown hook and block until the server stops;
     *            {@code false} to start and immediately close the server (for automated testing)
     * @return process exit code ({@code 0} on success, {@code 1} on invalid CLI usage, {@code 2} on runtime failure)
     */
    public static int executeCli(final String[] args, final boolean blockUntilShutdown)
    {
        final Options options = createCommandLineOptions();
        try
        {
            final CommandLine commandLine = new DefaultParser().parse(options, args);
            if (commandLine.hasOption(OPTION_HELP))
            {
                printUsage(options);
                return 0;
            }

            final String[] remaining = commandLine.getArgs();
            if (remaining.length != 1)
            {
                printUsage(options);
                return 1;
            }

            int port = DEFAULT_PORT;
            if (commandLine.hasOption(OPTION_PORT))
            {
                port = Integer.parseInt(commandLine.getOptionValue(OPTION_PORT).trim());
            }

            final boolean preserveAllDetails = commandLine.hasOption(OPTION_PRESERVE_ALL_DETAILS);
            final boolean cacheCompressed = commandLine.hasOption(OPTION_CACHE);
            final boolean preloadUncompressed = commandLine.hasOption(OPTION_PRELOAD);
            final String overridePropFileName = commandLine.getOptionValue(XltConstants.COMMANDLINE_OPTION_PROPERTY_FILENAME);
            final File overridePropFile = StringUtils.isNotBlank(overridePropFileName) ? new File(overridePropFileName) : null;
            final Properties cliProps = commandLine.getOptionProperties(OPTION_PROPERTY_DEFINITION);

            final File targetDir = new File(remaining[0]).getCanonicalFile();
            final LoadedDataset dataset = loadOrIngestDataset(targetDir, overridePropFile, cliProps, preserveAllDetails);

            XltLogger.reportLogger.info("Building in-memory column pointer & chunk hint index...");
            final long preloadStart = System.currentTimeMillis();
            final ColumnarQueryEngine queryEngine =
                new ColumnarQueryEngine(dataset.storage(), dataset.resultsDir(), true, preloadUncompressed || cacheCompressed);
            XltLogger.reportLogger.info("Column pointer index ready in {} ms (mode={}).",
                                        System.currentTimeMillis() - preloadStart, queryEngine.getMemoryMode());
            final ChunkExplorerServer server =
                new ChunkExplorerServer(queryEngine, port);

            final Thread shutdownTask = new Thread(() -> {
                try
                {
                    server.close();
                }
                catch (final Exception ignored)
                {
                }
                try
                {
                    dataset.storage().close();
                }
                catch (final Exception ignored)
                {
                }
            }, "chunkdb-explorer-shutdown");

            server.start();
            XltLogger.reportLogger.info(Console.horizontalBar());
            XltLogger.reportLogger.info("ChunkDB Explorer UI running at: {}", server.getBaseUrl());
            XltLogger.reportLogger.info("Press Ctrl+C to stop the server.");
            XltLogger.reportLogger.info(Console.horizontalBar());

            if (blockUntilShutdown)
            {
                Runtime.getRuntime().addShutdownHook(shutdownTask);
                server.join();
            }
            else
            {
                shutdownTask.run();
            }
            return 0;
        }
        catch (final Exception e)
        {
            XltLogger.reportLogger.error("Failed to start ChunkDB Explorer: " + e.getMessage(), e);
            return 2;
        }
    }

    public static void main(final String[] args)
    {
        final int exitCode = executeCli(args, true);
        if (exitCode != 0)
        {
            System.exit(exitCode);
        }
    }

    private static void printUsage(final Options options)
    {
        final HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("chunkdb_explorer.sh [options] <results-or-cache-dir>", options);
    }

    /**
     * Value holder for a mounted {@link ChunkStorage} and its associated runtime context.
     */
    public record LoadedDataset(
        ChunkStorage storage,
        File resultsDir,
        int threadCount,
        String detailMode
    )
    {
    }
}
