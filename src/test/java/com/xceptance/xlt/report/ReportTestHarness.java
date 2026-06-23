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
package com.xceptance.xlt.report;

import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

import org.apache.commons.io.FileUtils;
import org.apache.commons.vfs2.VFS;

import com.xceptance.common.util.CsvUtils;
import com.xceptance.xlt.api.engine.Data;
import com.xceptance.xlt.common.XltConstants;
import com.xceptance.xlt.engine.XltEngine;
import com.xceptance.xlt.engine.XltExecutionContext;
import com.xceptance.xlt.report.diffreport.DiffReportGeneratorMain;
import com.xceptance.xlt.report.trendreport.TrendReportGeneratorMain;

/**
 * Fluent builder for invoking the XLT report generation pipeline from within unit/integration tests.
 *
 * <p>The harness manages its own temporary directory internally — test authors never need to create or pass a temp
 * directory. All four report types are supported: load report, diff report, trend report, and scorecard.
 *
 * <p>Charts are enabled by default. Call {@link #withNoCharts()} to disable them.
 *
 * <p>Example usage:
 * <pre>
 * ReportResult result = ReportTestHarness
 *     .forReport("src/test/resources/results/my-result")
 *     .withProperty("com.xceptance.xlt.reportgenerator.renderingEngine", "freemarker")
 *     .withNoCharts()
 *     .generateReport();
 *
 * result.assertHtmlFileExists("transactions.html");
 * result.assertXmlNode("//summary/transactions");
 * </pre>
 */
public class ReportTestHarness
{
    // -------------------------------------------------------------------------
    // Internal state
    // -------------------------------------------------------------------------

    /** The primary input directory (results for load report, first report dir for diff, etc.). */
    private final File primaryInputDir;

    /** Second input directory — used for diff reports. */
    private final File secondaryInputDir;

    /** Additional input directories — used for trend reports (combined with primary). */
    private final List<File> additionalInputDirs;

    /** Which type of report to generate. */
    private final ReportType reportType;

    /** Whether to disable chart generation. Defaults to false (charts on). */
    private boolean noCharts = false;

    /** Property overrides applied on top of the config. */
    private final Properties propertyOverrides = new Properties();

    /** Recorded overlay operations (path → replacement file). */
    private final List<OverlayOp> overlays = new ArrayList<>();

    /** Recorded line-filter operations. */
    private final List<FilterLinesOp> lineFilters = new ArrayList<>();

    /** Recorded text-replace operations. */
    private final List<ReplaceTextOp> textReplacements = new ArrayList<>();

    /** Recorded full-content transform operations. */
    private final List<TransformOp> transforms = new ArrayList<>();

    /** Whether to clear all agent data files before running. */
    private boolean clearAgentData = false;

    /** Synthetic data files to write (path → records). */
    private final List<DataFileOp> dataFiles = new ArrayList<>();

    // -------------------------------------------------------------------------
    // Internal enums / operation records
    // -------------------------------------------------------------------------

    private enum ReportType
    {
        LOAD, DIFF, TREND, SCORECARD
    }

    private record OverlayOp(String relativePath, File replacement, String inlineContent)
    {
    }

    private record FilterLinesOp(String relativePath, Predicate<String> predicate)
    {
    }

    private record ReplaceTextOp(String relativePath, String search, String replacement)
    {
    }

    private record TransformOp(String relativePath, UnaryOperator<String> transform)
    {
    }

    private record DataFileOp(String relativePath, List<? extends Data> records)
    {
    }

    // -------------------------------------------------------------------------
    // Private constructor (use static factories)
    // -------------------------------------------------------------------------

    private ReportTestHarness(final ReportType type, final File primaryInputDir, final File secondaryInputDir,
                              final List<File> additionalInputDirs)
    {
        this.reportType = type;
        this.primaryInputDir = primaryInputDir;
        this.secondaryInputDir = secondaryInputDir;
        this.additionalInputDirs = additionalInputDirs == null ? new ArrayList<>() : new ArrayList<>(additionalInputDirs);
    }

    // -------------------------------------------------------------------------
    // Static factory methods
    // -------------------------------------------------------------------------

    /**
     * Creates a harness configured to generate a full load report from the given results directory.
     *
     * @param inputDir
     *            the XLT results directory (must contain a {@code config/} subdirectory)
     * @return this harness builder
     */
    public static ReportTestHarness forReport(final File inputDir)
    {
        return new ReportTestHarness(ReportType.LOAD, inputDir, null, null);
    }

    /**
     * Creates a harness configured to generate a full load report from the given results path.
     *
     * @param inputPath
     *            path to the XLT results directory
     * @return this harness builder
     */
    public static ReportTestHarness forReport(final String inputPath)
    {
        return forReport(new File(inputPath));
    }

    /**
     * Creates a harness configured to generate a diff report from two load report directories.
     *
     * @param oldReportDir
     *            the older report directory (must contain {@code testreport.xml})
     * @param newReportDir
     *            the newer report directory (must contain {@code testreport.xml})
     * @return this harness builder
     */
    public static ReportTestHarness forDiffReport(final File oldReportDir, final File newReportDir)
    {
        return new ReportTestHarness(ReportType.DIFF, oldReportDir, newReportDir, null);
    }

    /**
     * Creates a harness configured to generate a trend report from two or more report directories.
     * Each directory must contain a {@code testreport.xml}.
     *
     * @param reportDirs
     *            two or more report directories
     * @return this harness builder
     */
    public static ReportTestHarness forTrendReport(final File... reportDirs)
    {
        if (reportDirs == null || reportDirs.length < 2)
        {
            throw new IllegalArgumentException("Trend report requires at least 2 report directories");
        }
        final File primary = reportDirs[0];
        final List<File> rest = Arrays.asList(reportDirs).subList(1, reportDirs.length);
        return new ReportTestHarness(ReportType.TREND, primary, null, rest);
    }

    /**
     * Creates a harness configured to generate a trend report from a list of report directories.
     *
     * @param reportDirs
     *            two or more report directories
     * @return this harness builder
     */
    public static ReportTestHarness forTrendReport(final List<File> reportDirs)
    {
        return forTrendReport(reportDirs.toArray(new File[0]));
    }

    /**
     * Creates a harness configured to update/generate the scorecard for an existing report directory.
     * The directory must already contain a {@code testreport.xml}.
     *
     * @param reportDir
     *            an existing report directory
     * @return this harness builder
     */
    public static ReportTestHarness forScorecard(final File reportDir)
    {
        return new ReportTestHarness(ReportType.SCORECARD, reportDir, null, null);
    }

    // -------------------------------------------------------------------------
    // Fluent builder methods
    // -------------------------------------------------------------------------

    /**
     * Overrides a single configuration property for this run. Multiple calls accumulate.
     *
     * @param key
     *            the property key
     * @param value
     *            the property value
     * @return this harness builder
     */
    public ReportTestHarness withProperty(final String key, final String value)
    {
        propertyOverrides.setProperty(key, value);
        return this;
    }

    /**
     * Disables chart generation for this run. All other pipeline steps still execute.
     *
     * @return this harness builder
     */
    public ReportTestHarness withNoCharts()
    {
        noCharts = true;
        return this;
    }

    /**
     * Replaces a specific file in the working copy of the input directory before generation.
     * The original source directory is NOT modified.
     *
     * @param relativePath
     *            path relative to the input directory root (e.g., {@code "config/reportgenerator.properties"})
     * @param replacementFile
     *            the replacement file
     * @return this harness builder
     */
    public ReportTestHarness withOverlay(final String relativePath, final File replacementFile)
    {
        overlays.add(new OverlayOp(relativePath, replacementFile, null));
        return this;
    }

    /**
     * Replaces a specific file in the working copy with the given inline string content.
     * The original source directory is NOT modified.
     *
     * @param relativePath
     *            path relative to the input directory root
     * @param content
     *            the content to write
     * @return this harness builder
     */
    public ReportTestHarness withOverlayContent(final String relativePath, final String content)
    {
        overlays.add(new OverlayOp(relativePath, null, content));
        return this;
    }

    /**
     * Filters lines in a specific file in the working copy, retaining only lines for which the predicate returns true.
     *
     * @param relativePath
     *            path relative to the input directory root
     * @param predicate
     *            line-level filter — only lines returning {@code true} are kept
     * @return this harness builder
     */
    public ReportTestHarness withFilteredLines(final String relativePath, final Predicate<String> predicate)
    {
        lineFilters.add(new FilterLinesOp(relativePath, predicate));
        return this;
    }

    /**
     * Replaces all occurrences of a literal string inside a specific file in the working copy.
     *
     * @param relativePath
     *            path relative to the input directory root
     * @param search
     *            the literal string to search for
     * @param replacement
     *            the replacement string
     * @return this harness builder
     */
    public ReportTestHarness withReplacedText(final String relativePath, final String search, final String replacement)
    {
        textReplacements.add(new ReplaceTextOp(relativePath, search, replacement));
        return this;
    }

    /**
     * Applies an arbitrary transform to the full content of a specific file in the working copy.
     * The function receives the current file content as a string and its return value is written back.
     *
     * @param relativePath
     *            path relative to the input directory root
     * @param transform
     *            a function mapping current content to new content
     * @return this harness builder
     */
    public ReportTestHarness withTransform(final String relativePath, final UnaryOperator<String> transform)
    {
        transforms.add(new TransformOp(relativePath, transform));
        return this;
    }

    /**
     * Removes all agent data CSV and log files from the working copy before generation.
     * Agent data is defined as all files inside directories matching {@code ac*} in the input directory.
     * Use in combination with {@link #withDataFile} to run the full pipeline over purely synthetic data.
     *
     * @return this harness builder
     */
    public ReportTestHarness withClearedAgentData()
    {
        clearAgentData = true;
        return this;
    }

    /**
     * Creates a synthetic data file at the given path by serializing the given list of XLT data objects to CSV.
     * Each object is serialized using its {@link Data#toList()} method and encoded with {@link CsvUtils#encode}.
     * The file is created in the working copy of the input directory.
     *
     * @param relativePath
     *            path relative to the input directory root (e.g., {@code "ac0001/TOrder/0/timers.csv"})
     * @param records
     *            the data records to serialize
     * @return this harness builder
     */
    public ReportTestHarness withDataFile(final String relativePath, final List<? extends Data> records)
    {
        dataFiles.add(new DataFileOp(relativePath, records));
        return this;
    }

    // -------------------------------------------------------------------------
    // Terminal methods — trigger pipeline execution
    // -------------------------------------------------------------------------

    /**
     * Generates a full load report. The complete pipeline runs: data reading, statistics, charts, HTML/XML rendering.
     *
     * @return a {@link ReportResult} providing query and assertion methods over the generated output
     * @throws Exception
     *             if anything goes wrong during generation
     */
    public ReportResult generateReport() throws Exception
    {
        ensureLoadReportType();

        final File tempDir = Files.createTempDirectory("xlt-report-harness-").toFile();
        registerShutdownCleanup(tempDir);

        // Prepare working copy of input
        final File workingInputDir = new File(tempDir, "input");
        prepareWorkingInput(primaryInputDir, workingInputDir);

        // Ensure config tree is embedded in working input (mirrors ReportGeneratorRegressionTest)
        ensureConfigInInputDir(workingInputDir);

        // Set up output directory
        final File outputDir = new File(tempDir, "output");

        // Build generator
        final Properties overrides = buildOverrides();
        if (noCharts)
        {
            overrides.setProperty("com.xceptance.xlt.reportgenerator.charts.generate", "false");
        }

        final ReportGenerator generator = new ReportGenerator(VFS.getManager().toFileObject(workingInputDir), outputDir, noCharts, false,
                                                              null, overrides, null, null, null, null);
        generator.generateReport(false);

        return new ReportResult(outputDir, new File(outputDir, XltConstants.LOAD_REPORT_XML_FILENAME));
    }

    /**
     * Generates a diff report from the two report directories provided to {@link #forDiffReport}.
     * The complete diff report pipeline runs.
     *
     * @return a {@link ReportResult} providing query and assertion methods over the generated output
     * @throws Exception
     *             if anything goes wrong during generation
     */
    public ReportResult generateDiffReport() throws Exception
    {
        ensureDiffReportType();

        final File tempDir = Files.createTempDirectory("xlt-report-harness-").toFile();
        registerShutdownCleanup(tempDir);

        final File outputDir = new File(tempDir, "output");
        FileUtils.forceMkdir(outputDir);

        // DiffReportGeneratorMain.run(String[]) is public but calls System.exit() on completion.
        // We intercept the exit call so the JVM is not terminated.
        runWithExitInterception(() -> new DiffReportGeneratorMain().run(new String[]
            {
                "-o", outputDir.getAbsolutePath(), primaryInputDir.getAbsolutePath(),
                secondaryInputDir.getAbsolutePath()
            }));

        return new ReportResult(outputDir, new File(outputDir, XltConstants.DIFF_REPORT_XML_FILENAME));
    }

    /**
     * Generates a trend report from all report directories provided to {@link #forTrendReport}.
     * The complete trend report pipeline runs.
     *
     * @return a {@link ReportResult} providing query and assertion methods over the generated output
     * @throws Exception
     *             if anything goes wrong during generation
     */
    public ReportResult generateTrendReport() throws Exception
    {
        ensureTrendReportType();

        final File tempDir = Files.createTempDirectory("xlt-report-harness-").toFile();
        registerShutdownCleanup(tempDir);

        final File outputDir = new File(tempDir, "output");
        FileUtils.forceMkdir(outputDir);

        // TrendReportGeneratorMain.run(String[]) is private; we use the public static main() instead.
        // Both call System.exit() on completion so we intercept the exit call.
        final List<String> args = new ArrayList<>();
        args.add("-o");
        args.add(outputDir.getAbsolutePath());
        if (noCharts)
        {
            args.add("-" + XltConstants.COMMANDLINE_OPTION_NO_CHARTS);
        }
        args.add(primaryInputDir.getAbsolutePath());
        for (final File d : additionalInputDirs)
        {
            args.add(d.getAbsolutePath());
        }

        final String[] argsArray = args.toArray(new String[0]);
        runWithExitInterception(() -> TrendReportGeneratorMain.main(argsArray));

        return new ReportResult(outputDir, new File(outputDir, XltConstants.TREND_REPORT_XML_FILENAME));
    }

    /**
     * Generates (or re-generates) the scorecard for the report directory provided to {@link #forScorecard}.
     * Uses {@link ReportGenerator#updateScorecard()} — the full scorecard pipeline runs.
     *
     * @return a {@link ReportResult} providing query and assertion methods over the generated output
     * @throws Exception
     *             if anything goes wrong during generation
     */
    public ReportResult generateScorecard() throws Exception
    {
        ensureScorecardType();

        // Ensure the report directory has all required config (report-templates, xsl, etc.)
        ensureConfigInInputDir(primaryInputDir);

        XltEngine.get();
        XltExecutionContext.getCurrent().setTestSuiteHomeDir(primaryInputDir);
        XltExecutionContext.getCurrent().setTestSuiteConfigDir(new File(primaryInputDir, XltConstants.CONFIG_DIR_NAME));

        final ReportGenerator generator = new ReportGenerator(VFS.getManager().resolveFile(primaryInputDir.toURI()),
                                                              primaryInputDir, true);
        generator.updateScorecard();

        return new ReportResult(primaryInputDir, new File(primaryInputDir, XltConstants.SCORECARD_REPORT_XML_FILENAME));
    }

    // -------------------------------------------------------------------------
    // Internal helpers
    // -------------------------------------------------------------------------

    /**
     * Copies the input directory to a working copy and applies all registered overlays,
     * filters, replacements, transforms, clears, and synthetic data files in order.
     */
    private void prepareWorkingInput(final File sourceDir, final File workingDir) throws IOException
    {
        FileUtils.copyDirectory(sourceDir, workingDir);

        // Clear agent data if requested
        if (clearAgentData)
        {
            for (final File agentDir : listAgentDirs(workingDir))
            {
                for (final File f : FileUtils.listFiles(agentDir, new String[]
                    {
                        "csv", "log"
                    }, true))
                {
                    f.delete();
                }
            }
        }

        // Apply overlays
        for (final OverlayOp op : overlays)
        {
            final File target = new File(workingDir, op.relativePath());
            FileUtils.forceMkdir(target.getParentFile());
            if (op.replacement() != null)
            {
                FileUtils.copyFile(op.replacement(), target);
            }
            else
            {
                FileUtils.writeStringToFile(target, op.inlineContent(), StandardCharsets.UTF_8);
            }
        }

        // Apply line filters
        for (final FilterLinesOp op : lineFilters)
        {
            final File target = new File(workingDir, op.relativePath());
            if (target.exists())
            {
                final List<String> lines = FileUtils.readLines(target, StandardCharsets.UTF_8);
                final List<String> filtered = lines.stream().filter(op.predicate()).toList();
                FileUtils.writeLines(target, StandardCharsets.UTF_8.name(), filtered);
            }
        }

        // Apply text replacements
        for (final ReplaceTextOp op : textReplacements)
        {
            final File target = new File(workingDir, op.relativePath());
            if (target.exists())
            {
                String content = FileUtils.readFileToString(target, StandardCharsets.UTF_8);
                content = content.replace(op.search(), op.replacement());
                FileUtils.writeStringToFile(target, content, StandardCharsets.UTF_8);
            }
        }

        // Apply full-content transforms
        for (final TransformOp op : transforms)
        {
            final File target = new File(workingDir, op.relativePath());
            if (target.exists())
            {
                String content = FileUtils.readFileToString(target, StandardCharsets.UTF_8);
                content = op.transform().apply(content);
                FileUtils.writeStringToFile(target, content, StandardCharsets.UTF_8);
            }
        }

        // Write synthetic data files
        for (final DataFileOp op : dataFiles)
        {
            final File target = new File(workingDir, op.relativePath());
            FileUtils.forceMkdir(target.getParentFile());
            try (final java.io.PrintWriter pw = new java.io.PrintWriter(
                new OutputStreamWriter(new FileOutputStream(target), StandardCharsets.UTF_8)))
            {
                for (final Data record : op.records())
                {
                    pw.println(CsvUtils.encode(record.toList()));
                }
            }
        }
    }

    /**
     * Ensures the working input directory has a properly populated {@code config/} subdirectory
     * by copying the main project config into it — exactly as the regression test does.
     * Skips if the config directory already has templates (meaning the source had its own config).
     */
    private void ensureConfigInInputDir(final File workingInputDir) throws IOException
    {
        final File targetConfigDir = new File(workingInputDir, "config");
        final File reportTemplates = new File(targetConfigDir, "report-templates");

        // If report-templates already present, config is complete
        if (reportTemplates.exists())
        {
            return;
        }

        // Copy all required config subdirectories from the project root config/
        final File projectConfig = new File("config");
        FileUtils.copyDirectory(new File(projectConfig, "report-templates"), reportTemplates);
        FileUtils.copyDirectory(new File(projectConfig, "xsl"), new File(targetConfigDir, "xsl"));
        FileUtils.copyDirectory(new File(projectConfig, "testreport"), new File(targetConfigDir, "testreport"));

        // Copy all .properties files from the root config dir
        for (final File f : FileUtils.listFiles(projectConfig, new String[]
            {
                "properties"
            }, false))
        {
            FileUtils.copyFileToDirectory(f, targetConfigDir);
        }
    }

    /** Collects all agent directories (matching {@code ac*}) inside the given root. */
    private List<File> listAgentDirs(final File rootDir)
    {
        final List<File> agentDirs = new ArrayList<>();
        final File[] children = rootDir.listFiles();
        if (children != null)
        {
            for (final File f : children)
            {
                if (f.isDirectory() && f.getName().startsWith("ac"))
                {
                    agentDirs.add(f);
                }
            }
        }
        return agentDirs;
    }

    /** Returns the merged property overrides (copies so the internal state is not shared). */
    private Properties buildOverrides()
    {
        final Properties p = new Properties();
        p.putAll(propertyOverrides);
        return p;
    }

    /** Registers a JVM shutdown hook to delete the given temp directory. */
    private static void registerShutdownCleanup(final File tempDir)
    {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try
            {
                FileUtils.deleteQuietly(tempDir);
            }
            catch (final Exception ignored)
            {
            }
        }));
    }

    /**
     * Executes the given runnable, intercepting any {@link System#exit(int)} call triggered inside it.
     * If the exit code is 0 (success) the call returns normally. Any non-zero exit code throws an
     * {@link AssertionError}.
     *
     * <p>This is needed because {@link com.xceptance.xlt.report.diffreport.DiffReportGeneratorMain} and
     * {@link com.xceptance.xlt.report.trendreport.TrendReportGeneratorMain} call {@code System.exit()} directly.
     */
    @SuppressWarnings("removal")
    private static void runWithExitInterception(final ThrowingRunnable action) throws Exception
    {
        final SecurityManager oldManager = System.getSecurityManager();
        final NoExitSecurityManager noExit = new NoExitSecurityManager(oldManager);
        System.setSecurityManager(noExit);
        try
        {
            action.run();
        }
        catch (final ExitException e)
        {
            if (e.status != 0)
            {
                throw new AssertionError("Report generator exited with non-zero status: " + e.status, e);
            }
            // status 0 == success, fall through
        }
        finally
        {
            System.setSecurityManager(oldManager);
        }
    }

    @FunctionalInterface
    private interface ThrowingRunnable
    {
        void run() throws Exception;
    }

    /** Exception thrown when a System.exit() call is intercepted. */
    private static class ExitException extends SecurityException
    {
        final int status;

        ExitException(final int status)
        {
            super("Intercepted System.exit(" + status + ")");
            this.status = status;
        }
    }

    /** SecurityManager that prevents System.exit() from terminating the JVM. */
    @SuppressWarnings("removal")
    private static class NoExitSecurityManager extends SecurityManager
    {
        private final SecurityManager delegate;

        NoExitSecurityManager(final SecurityManager delegate)
        {
            this.delegate = delegate;
        }

        @Override
        public void checkExit(final int status)
        {
            throw new ExitException(status);
        }

        @Override
        public void checkPermission(final java.security.Permission perm)
        {
            if (delegate != null)
            {
                delegate.checkPermission(perm);
            }
        }
    }

    // -------------------------------------------------------------------------
    // Type-guards
    // -------------------------------------------------------------------------

    private void ensureLoadReportType()
    {
        if (reportType != ReportType.LOAD)
        {
            throw new IllegalStateException("generateReport() requires forReport() factory method");
        }
    }

    private void ensureDiffReportType()
    {
        if (reportType != ReportType.DIFF)
        {
            throw new IllegalStateException("generateDiffReport() requires forDiffReport() factory method");
        }
    }

    private void ensureTrendReportType()
    {
        if (reportType != ReportType.TREND)
        {
            throw new IllegalStateException("generateTrendReport() requires forTrendReport() factory method");
        }
    }

    private void ensureScorecardType()
    {
        if (reportType != ReportType.SCORECARD)
        {
            throw new IllegalStateException("generateScorecard() requires forScorecard() factory method");
        }
    }
}
