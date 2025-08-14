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
package com.xceptance.xlt.report.providers;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileType;

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
    
    private String baseDir;
    private Path targetDir = null;

    /**
     * {@inheritDoc}
     */
    @Override
    public Object createReportFragment()
    {
        final CustomLogsReport report = new CustomLogsReport();

        FileObject results = ((ReportGeneratorConfiguration) getConfiguration()).getResultsDirectory();
        baseDir = results.getName().getBaseName();
        targetDir = Paths.get(getConfiguration().getReportDirectory() + File.separator + CUSTOM_DATA);
        
        // get property value whether or not to aggregate all recorded custom data logs into one data file per scope
        // (or else to keep the original directory structure for recorded custom data)
        // (default: true = collect all data in one file per scope)
        final boolean collectCustomDataInOneFile = ((ReportGeneratorConfiguration) getConfiguration()).aggregateCustomData();
        
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
    
    private void findLogsAggregated (FileObject results)
    {
        try
        {
            findLogs(results, null, true);
        }
        catch (IOException e)
        {
            XltLogger.runTimeLogger.error("Failed to walk file tree searching for custom data logs. Cause: " + e.getMessage());
        }
        finally 
        {
            writeAggregatedDataToZip();
        }
        return;
    }
    
    private void findLogsNonAggregated (FileObject results)
    {
        try
        {
            findLogs(results, null, false);
        }
        catch (IOException e)
        {
            XltLogger.runTimeLogger.error("Failed to walk file tree searching for custom data logs. Cause: " + e.getMessage());
        }
        finally 
        {
            finishZipStreams(); // this closes the ZipOutputStreams, so this MUST be done
        }
        return;
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
    private void findLogs (FileObject file, String currentPath, boolean aggregated) throws IOException
    {
        final String filename = file.getName().getBaseName();
        if (file.getType() == FileType.FOLDER)
        {
            if (!XltConstants.CONFIG_DIR_NAME.equals(filename))
            {
                for (final FileObject fo : file.getChildren())
                {
                    findLogs(fo, makePath(currentPath, filename), aggregated);
                }
            }
        }
        else if (file.getType() == FileType.FILE)
        {
            if (file.getName().getBaseName().toString().startsWith(XltConstants.CUSTOM_LOG_PREFIX)) 
            {
                final String scopeName = filename.substring(XltConstants.CUSTOM_LOG_PREFIX.length(), filename.lastIndexOf('.'));
                
                if (foundScopes.isEmpty())
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
    private void copyCustomDataFileToZip(FileObject file, String currentPath, final String filename, final String scopeName)
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
        writeDataToZip(file, scopeStream);
        scopeStream.closeEntry();
    }

    /**
     * Adds the contents of a custom data log file to the aggregated single log file for the corresponding
     * custom data scope, which will be later zipped for download (see {@link #finishZipStreams()}).
     * @param file
     * @param scopeName
     */
    private void aggregateDataForScope(FileObject file, final String scopeName)
    {
        // every file type is aggregated seperately, e.g. users.csv and users.log
        // aggregated files with same scope (see example above) are then zipped into one archive users.zip for download

        String scopeFile = scopeName + "." + file.getName().getExtension();
        List<Path> scopePaths = foundScopeFiles.get(scopeName);
        if (scopePaths == null)
        {
            scopePaths = new ArrayList<Path>();
            foundScopeFiles.put(scopeName, scopePaths);
        }
        Path scopePath = targetDir.resolve(scopeFile).toAbsolutePath().normalize();
        if (!scopePaths.contains(scopePath))
        {
            scopePaths.add(scopePath);
        }
        
        try
        {
            if (!Files.exists(scopePath))
            {
                Files.createFile(scopePath);
            }
        }
        catch (IOException e)
        {
            
        }
        
        try (BufferedWriter scopeWriter = Files.newBufferedWriter(scopePath, StandardOpenOption.APPEND))
        {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getContent().getInputStream(), StandardCharsets.UTF_8)))
            {
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
                            // if aggregated file already exists (has content): ignore header
                            if (Files.size(scopePath) > 0)
                                continue;
                            // otherwise write header line
                        }
                    }
                    scopeWriter.write(line);
                    scopeWriter.newLine();
                }
            }
        }
        catch (IOException e) 
        {
            XltLogger.runTimeLogger.error("Error reading or writing file: " + e.getMessage() + e.getClass());
            // Continue processing other files even if one fails
        }
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
     * Writes contents of a file (that might be inside a zip archive) to a {@link ZipOutputStream}.
     * @param file the file to copy to the zip archive
     * @param scopeStream the {@link ZipOutputStream} that the file contents are written to
     * @throws IOException
     */
    private void writeDataToZip(FileObject file, ZipOutputStream scopeStream) throws IOException
    {
        // InputStream in = /*isCompressed ? 
        //    new GZIPInputStream(file.getContent().getInputStream(), 1024 * 16) :*/ //is my data EVER zipped? not right now, but maybe in the future?
        //    file.getContent().getInputStream();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getContent().getInputStream(), StandardCharsets.UTF_8)))
        {
            //final boolean isCompressed = "gz".equalsIgnoreCase(file.getName().getExtension());
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
                scopeStream.write((line + System.lineSeparator()).getBytes("UTF-8"));
                //TODO count lines while reading for additional info?
            }
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
