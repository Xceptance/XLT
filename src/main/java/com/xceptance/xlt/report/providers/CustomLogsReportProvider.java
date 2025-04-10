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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
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

    private static final String CUSTOM_DATA = "custom_data_logs";
    
    private Map<String, ZipOutputStream> foundScopes = new HashMap<String, ZipOutputStream>();
    
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
        
        try
        {
            findLogs(results, null);
        }
        catch (IOException e)
        {
            XltLogger.runTimeLogger.error("Failed to walk file tree searching for custom data logs. Cause: " + e.getMessage());
        }
        finally 
        {
            closeAllStreams(); // this closes the ZipOutputStreams, so this MUST be done
        }

        // add the link/size info for found scopes
        Set<String> scopes = getResult();
        
        for (String scope : scopes)
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

    /**
     * Recursive method to walk the directory tree searching for custom log files. 
     * Found files are directly put into a zip archive for the corresponding custom data scope in the report.
     * @param file the directory or file to start from
     * @throws IOException
     */
    private void findLogs (FileObject file, String currentPath) throws IOException
    {
        final String filename = file.getName().getBaseName();
        if (file.getType() == FileType.FOLDER)
        {
            if (!XltConstants.CONFIG_DIR_NAME.equals(filename))
            {
                for (final FileObject fo : file.getChildren())
                {
                    findLogs(fo, makePath(currentPath, filename));
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
                
                // if scope has already been used, there is a stream for it
                ZipOutputStream scopeStream = foundScopes.get(scopeName);
                // if scope/stream does not exist yet, create one and add it to list
                if (scopeStream == null)
                {
                    FileOutputStream fos = new FileOutputStream(targetDir.toString() + File.separator + scopeName + ".zip");
                    scopeStream = new ZipOutputStream(fos);
                    foundScopes.put(scopeName, scopeStream);
                }
                
                // add zip entry, copy current log file for scope   
                scopeStream.putNextEntry(new ZipEntry(makePath(currentPath, filename)));
                writeDataToZip(file, scopeStream);
                scopeStream.closeEntry();
            }
        }
        return;
    }

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

    private void writeDataToZip(FileObject file, ZipOutputStream scopeStream) throws IOException
    {
        //final boolean isCompressed = "gz".equalsIgnoreCase(file.getName().getExtension());
        
        InputStream in = /*isCompressed ? 
            new GZIPInputStream(file.getContent().getInputStream(), 1024 * 16) :*/ //is my data EVER zipped? not right now, but maybe in the future?
            file.getContent().getInputStream();
        byte[] buffer = new byte[1024];

        int len;
        while ((len = in.read(buffer)) > 0) 
        {
            scopeStream.write(buffer, 0, len);
        }
        //TODO see DataReaderThread for how to count lines while reading for additional info
        
        in.close();
    }
    
    /**
     * Close all output streams that have been opened during file search for copying the found data to the report.
     */
    private void closeAllStreams() 
    {
        for (ZipOutputStream zos : foundScopes.values())
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
    
    /**
     * @return A collection of the found custom data scope names.
     */
    private Set<String> getResult() 
    {
        if (!foundScopes.isEmpty())
        {
            XltLogger.runTimeLogger.info("Found custom data logs for scopes: " + foundScopes.keySet());
        }
        return foundScopes.keySet();
    }
}
