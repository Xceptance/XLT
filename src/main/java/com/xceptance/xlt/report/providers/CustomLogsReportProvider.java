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
package com.xceptance.xlt.report.providers;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import com.xceptance.common.io.fs.FsResource;

import com.xceptance.xlt.api.engine.Data;
import com.xceptance.xlt.api.report.AbstractReportProvider;
import com.xceptance.xlt.api.util.XltLogger;
import com.xceptance.xlt.common.XltConstants;
import com.xceptance.xlt.report.ReportGeneratorConfiguration;

/**
 * Report provider generating a report fragment about the custom data logs collected during the test run
 */
public class CustomLogsReportProvider extends AbstractReportProvider
{
    // XLT will mark custom user data files that is using a header with an extra comment row so it's predictable
    // at report creation, we will remove this extra row (in both aggregated files and grouped archive files)
    public static final String CUSTOM_DATA_HEADER_MARKER = "# Header inserted by XLT ====================";
    
    private static final String CUSTOM_DATA = "custom_data_logs";
    
    private Set<String> foundScopes = new HashSet<String>();
    
    private Map<String, ZipOutputStream> zipStreamsForScopes = new HashMap<String, ZipOutputStream>();
    
    private Map<String, List<Path>> foundScopeFiles = new HashMap<String, List<Path>>();

    private final Map<Path, BufferedWriter> scopeWriters = new HashMap<>();

    private final Set<Path> nonEmptyScopePaths = new HashSet<>();
    
    private String baseDir;
    private Path targetDir = null;
    private String cacheDirName = null;

    /**
     * {@inheritDoc}
     */
    @Override
    public Object createReportFragment()
    {
        final CustomLogsReport report = new CustomLogsReport();
        final ReportGeneratorConfiguration reportConfig = (ReportGeneratorConfiguration) getConfiguration();

        FsResource results = reportConfig.getResultsDirectory();
        baseDir = results.getName();
        targetDir = Paths.get(reportConfig.getReportDirectory() + File.separator + CUSTOM_DATA);
        cacheDirName = reportConfig.getDataCacheDirectoryName();
        
        // get property value whether or not to aggregate all recorded custom data logs into one data file per scope
        // (or else to keep the original directory structure for recorded custom data)
        // (default: true = collect all data in one file per scope)
        final boolean collectCustomDataInOneFile = reportConfig.aggregateCustomData();
        
        if (collectCustomDataInOneFile)
        {
            // traverse the results directory, find all logged data, aggregate it to one single data file 
            // per scope and zip it to the report directory
            findLogsAggregated(results);
        }
        else
        {
            // traverse the results directory, find all logged data and add it to the report directory as zip
            findLogsNonAggregated(results);
        }

        // add the link/size info for found scopes to the report
        for (String scope : foundScopes)
        {    
            CustomLogReport clr = new CustomLogReport();
            clr.scope = scope;
            
            Path targetPath = Paths.get(targetDir + File.separator + scope + ".zip");
            
            try
            {                
                clr.size = Files.size(targetPath);
                clr.path = CUSTOM_DATA + File.separator + scope + ".zip";
            }
            catch (IOException e)
            {
                XltLogger.runTimeLogger.error("Unable to collect information for custom data logs for " + scope + ". Cause: " + e.getMessage());
            }
            
            report.customLogs.add(clr);
        }
              

        return report;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void processDataRecord(final Data data)
    {
        // nothing to do here
    }

    /**
     * Tell the system that there is no need to call processDataRecord
     */
    @Override
    public boolean wantsDataRecords()
    {
        return false;
    }        
    
    private void findLogsAggregated(FsResource results)
    {
        try
        {
            final Optional<Path> localPath = results.toLocalPath();
            if (localPath.isPresent() && Files.isDirectory(localPath.get()))
            {
                findLogsLocal(localPath.get(), true);
            }
            else
            {
                findLogs(results, null, true);
            }
        }
        catch (IOException e)
        {
            XltLogger.runTimeLogger.error("Failed to walk file tree searching for custom data logs. Cause: " + e.getMessage());
        }
        finally 
        {
            closeScopeWriters();
            writeAggregatedDataToZip();
        }
    }
    
    private void findLogsNonAggregated(FsResource results)
    {
        try
        {
            final Optional<Path> localPath = results.toLocalPath();
            if (localPath.isPresent() && Files.isDirectory(localPath.get()))
            {
                findLogsLocal(localPath.get(), false);
            }
            else
            {
                findLogs(results, null, false);
            }
        }
        catch (IOException e)
        {
            XltLogger.runTimeLogger.error("Failed to walk file tree searching for custom data logs. Cause: " + e.getMessage());
        }
        finally 
        {
            finishZipStreams(); // this closes the ZipOutputStreams, so this MUST be done
        }
    }

    /**
     * Fast local-filesystem directory walk using NIO {@link Files#walkFileTree} to avoid allocating
     * {@link FsResource} wrappers and extra {@code stat()} calls for non-matching files across thousands
     * of user directories.
     */
    private void findLogsLocal(final Path rootDir, final boolean aggregated) throws IOException
    {
        final ReportGeneratorConfiguration reportConfig = (ReportGeneratorConfiguration) getConfiguration();
        final Path cacheDirPath = reportConfig.getDataCacheDirectory(rootDir.toFile()).toPath().toAbsolutePath().normalize();

        Files.walkFileTree(rootDir, new SimpleFileVisitor<Path>()
        {
            @Override
            public FileVisitResult preVisitDirectory(final Path dir, final BasicFileAttributes attrs)
            {
                if (!dir.equals(rootDir))
                {
                    final Path namePath = dir.getFileName();
                    final String dirName = namePath != null ? namePath.toString() : "";
                    if (XltConstants.CONFIG_DIR_NAME.equals(dirName) ||
                        (cacheDirName != null && cacheDirName.equals(dirName)) ||
                        dir.toAbsolutePath().normalize().equals(cacheDirPath))
                    {
                        return FileVisitResult.SKIP_SUBTREE;
                    }
                }
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException
            {
                if (!attrs.isRegularFile())
                {
                    return FileVisitResult.CONTINUE;
                }

                final Path namePath = file.getFileName();
                if (namePath == null)
                {
                    return FileVisitResult.CONTINUE;
                }

                final String filename = namePath.toString();
                if (filename.startsWith(XltConstants.CUSTOM_LOG_PREFIX))
                {
                    final int dotIdx = filename.lastIndexOf('.');
                    if (dotIdx <= XltConstants.CUSTOM_LOG_PREFIX.length())
                    {
                        return FileVisitResult.CONTINUE;
                    }

                    final String scopeName = filename.substring(XltConstants.CUSTOM_LOG_PREFIX.length(), dotIdx);
                    final String extension = filename.substring(dotIdx + 1);

                    if (foundScopes.isEmpty() && foundScopeFiles.isEmpty())
                    {
                        targetDir.toFile().mkdirs();
                    }

                    if (aggregated)
                    {
                        aggregateDataForScope(file, scopeName, extension);
                    }
                    else
                    {
                        final Path relParent = rootDir.relativize(file.getParent());
                        final String currentPath = relParent.toString().isEmpty() ? null : relParent.toString().replace('\\', '/');
                        copyCustomDataFileToZip(file, currentPath, filename.substring(XltConstants.CUSTOM_LOG_PREFIX.length()), scopeName);
                    }
                }
                return FileVisitResult.CONTINUE;
            }
        });
    }


    /**
     * Recursive method to walk the directory tree searching for custom log files. 
     * Found files are aggregated to one single log file for the corresponding custom data scope
     * and later zipped for download (default), or directly put into a zip archive (which contains
     * log files and folder structure just as recorded) for the corresponding custom data scope in 
     * the report (if property com.xceptance.xlt.reportgenerator.customDataLogs.collect = false).
     * @param file the directory or file to start from
     * @param currentPath the file path we traversed up to this point
     * @throws IOException
     */
    private void findLogs (FsResource file, String currentPath, boolean aggregated) throws IOException
    {
        final String filename = file.getName();
        if (file.isDirectory())
        {
            if (!XltConstants.CONFIG_DIR_NAME.equals(filename) &&
                (cacheDirName == null || !cacheDirName.equals(filename)))
            {
                for (final FsResource fo : file.listChildren())
                {
                    findLogs(fo, makePath(currentPath, filename), aggregated);
                }
            }
        }
        else if (file.isFile())
        {
            if (filename.startsWith(XltConstants.CUSTOM_LOG_PREFIX)) 
            {
                final String scopeName = filename.substring(XltConstants.CUSTOM_LOG_PREFIX.length(), filename.lastIndexOf('.'));
                
                if (foundScopes.isEmpty() && foundScopeFiles.isEmpty())
                {
                    //for the very first scope we have to create the directory to contain all logs in the report
                    targetDir.toFile().mkdirs();
                }
                
                if (aggregated)
                {
                    aggregateDataForScope(file, scopeName);
                }
                else
                {
                    copyCustomDataFileToZip(file, currentPath, filename.substring(XltConstants.CUSTOM_LOG_PREFIX.length()), scopeName);
                }
            }
        }
        return;
    }

    /**
     * Directly puts a found custom data log file into the zip archive for the corresponding custom data scope
     * (which contains log files and folder structure just as recorded) for the corresponding custom data scope in 
     * the report (if property com.xceptance.xlt.reportgenerator.customDataLogs.collect = false).
     * @param file
     * @param currentPath
     * @param filename
     * @param scopeName
     * @throws IOException
     */
    private void copyCustomDataFileToZip(FsResource file, String currentPath, final String filename, final String scopeName)
        throws IOException
    {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.openStream(), StandardCharsets.UTF_8)))
        {
            copyReaderToZip(reader, currentPath, filename, scopeName);
        }
    }

    private void copyCustomDataFileToZip(Path file, String currentPath, final String filename, final String scopeName)
        throws IOException
    {
        try (BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8))
        {
            copyReaderToZip(reader, currentPath, filename, scopeName);
        }
    }

    private void copyReaderToZip(BufferedReader reader, String currentPath, final String filename, final String scopeName)
        throws IOException
    {
        // if scope has already been used, there is a stream for it
        ZipOutputStream scopeStream = zipStreamsForScopes.get(scopeName);
        // if scope/stream does not exist yet, create one and add it to list
        if (scopeStream == null)
        {
            FileOutputStream fos = new FileOutputStream(targetDir.toString() + File.separator + scopeName + ".zip");
            scopeStream = new ZipOutputStream(fos);
            zipStreamsForScopes.put(scopeName, scopeStream);
            foundScopes.add(scopeName);
        }
        
        // add zip entry, copy current log file for scope   
        scopeStream.putNextEntry(new ZipEntry(makePath(currentPath, filename)));
        writeReaderToZip(reader, scopeStream);
        scopeStream.closeEntry();
    }

    /**
     * Adds the contents of a custom data log file to the aggregated single log file for the corresponding
     * custom data scope, which will be later zipped for download (see {@link #finishZipStreams()}).
     * @param file
     * @param scopeName
     */
    private void aggregateDataForScope(FsResource file, final String scopeName)
    {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.openStream(), StandardCharsets.UTF_8)))
        {
            aggregateDataFromReader(reader, scopeName, file.getExtension());
        }
        catch (IOException e)
        {
            XltLogger.runTimeLogger.error("Error reading or writing file: " + e.getMessage() + e.getClass());
        }
    }

    private void aggregateDataForScope(Path file, final String scopeName, final String extension)
    {
        try (BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8))
        {
            aggregateDataFromReader(reader, scopeName, extension);
        }
        catch (IOException e)
        {
            XltLogger.runTimeLogger.error("Error reading or writing file: " + e.getMessage() + e.getClass());
        }
    }

    private void aggregateDataFromReader(final BufferedReader reader, final String scopeName, final String extension)
        throws IOException
    {
        // every file type is aggregated separately, e.g. users.csv and users.log
        // aggregated files with same scope (see example above) are then zipped into one archive users.zip for download
        final String scopeFile = scopeName + "." + extension;
        List<Path> scopePaths = foundScopeFiles.get(scopeName);
        if (scopePaths == null)
        {
            scopePaths = new ArrayList<Path>();
            foundScopeFiles.put(scopeName, scopePaths);
        }
        final Path scopePath = targetDir.resolve(scopeFile).toAbsolutePath().normalize();
        if (!scopePaths.contains(scopePath))
        {
            scopePaths.add(scopePath);
        }

        BufferedWriter scopeWriter = scopeWriters.get(scopePath);
        if (scopeWriter == null)
        {
            Files.createDirectories(targetDir);
            if (Files.exists(scopePath) && Files.size(scopePath) > 0)
            {
                nonEmptyScopePaths.add(scopePath);
            }
            scopeWriter = Files.newBufferedWriter(scopePath, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            scopeWriters.put(scopePath, scopeWriter);
        }

        String line;
        boolean first = true;
        while ((line = reader.readLine()) != null)
        {
            if (first)
            {
                first = false;
                // check if the file uses a header
                if (CUSTOM_DATA_HEADER_MARKER.equals(line))
                {
                    // do not write the XLT specific header marker to the target file,
                    // continue to actual header line
                    line = reader.readLine();
                    if (line == null)
                    {
                        break;
                    }
                    // if aggregated file already exists (has content): ignore header
                    if (nonEmptyScopePaths.contains(scopePath))
                    {
                        continue;
                    }
                    // otherwise write header line
                }
            }
            scopeWriter.write(line);
            scopeWriter.newLine();
            nonEmptyScopePaths.add(scopePath);
        }
    }

    private void closeScopeWriters()
    {
        for (final BufferedWriter writer : scopeWriters.values())
        {
            try
            {
                writer.close();
            }
            catch (final IOException e)
            {
                XltLogger.runTimeLogger.error("Error closing aggregated custom data log writer: " + e.getMessage());
            }
        }
        scopeWriters.clear();
        nonEmptyScopePaths.clear();
    }
    
    /**
     * Zip the aggregated file(s) for each found scope.
     */
    private void writeAggregatedDataToZip()
    {
        // we only have collected data files yet, so we need to zip them now
        // every file type is aggregated seperately, e.g. users.csv and users.log
        // aggregated files with same scope (see example above) are then zipped into one archive users.zip for download
        for (String scopeName : foundScopeFiles.keySet())
        {
            try (FileOutputStream fos = new FileOutputStream(targetDir.toString() + File.separator + scopeName + ".zip");
                ZipOutputStream zos = new ZipOutputStream(fos)) 
            {

               // Create a new zip entry for the aggregated file
               // The entry name in the zip will be just the file's name
               for (Path fileForScope : foundScopeFiles.get(scopeName))
               {
                   ZipEntry zipEntry = new ZipEntry(fileForScope.getFileName().toString());
                   zos.putNextEntry(zipEntry);

                   // Read the bytes from the aggregated file and write them to the zip output stream
                   Files.copy(fileForScope, zos);

                   // Close the current zip entry
                   zos.closeEntry();
                   
                   //remove original aggregation file
                   Files.deleteIfExists(fileForScope);
               }
               
               foundScopes.add(scopeName);
            }
            catch (IOException e)
            {
                XltLogger.runTimeLogger.error("Error zipping custom data file for scope " + scopeName + ": " + e.getMessage());
                // Continue processing other files even if one fails
            }
        }
    }

    /**
     * Create a valid path for the current custom data log file that may be used in the directory structure of the 
     * zip file (in case custom data logs are not aggregated into one single data file).
     * @param currentPath
     * @param currentFileName
     * @return
     */
    private String makePath(String currentPath, String currentFileName)
    {
        if (currentPath != null)
        {
            return currentPath + "/" + currentFileName;
        }
        else if (baseDir.equals(currentFileName))
        {
            return null; //we're not using base dir name in zip, start on agent level
        }
        return currentFileName;
    }

    /**
     * Writes contents of a reader to a {@link ZipOutputStream}.
     * @param reader the reader to copy to the zip archive
     * @param scopeStream the {@link ZipOutputStream} that the file contents are written to
     * @throws IOException
     */
    private void writeReaderToZip(BufferedReader reader, ZipOutputStream scopeStream) throws IOException
    {
        String line;
        boolean first = true;
        while ((line = reader.readLine()) != null) 
        {
            if (first)
            {
                first = false;
                // check if the file uses a header: if yes, don't write the XLT-specific marker to the target file
                if (CUSTOM_DATA_HEADER_MARKER.equals(line))
                    continue;
            }
            scopeStream.write((line + System.lineSeparator()).getBytes(StandardCharsets.UTF_8));
        }
    }

    /**
     * Close all zip output streams that have been opened during file search for copying the found data to the report.
     */
    private void finishZipStreams()
    {
        for (ZipOutputStream zos : zipStreamsForScopes.values())
        {
            try {
                // VERY IMPORTANT: CLOSE ALL OPEN STREAMS
                zos.close();
            } 
            catch (IOException e) {
                XltLogger.runTimeLogger.error("Unable to zip custom data logs to report. Cause: " + e.getMessage());
            }
        }
    }
}
