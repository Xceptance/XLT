/*
 * Copyright (c) 2005-2024 Xceptance Software Technologies GmbH
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
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import com.xceptance.xlt.api.engine.Data;
import com.xceptance.xlt.api.report.AbstractReportProvider;
import com.xceptance.xlt.common.XltConstants;
import com.xceptance.xlt.report.ReportGeneratorConfiguration;

/**
 * Report provider generating a report fragment about the custom data logs collected during the test run
 */
public class CustomLogsReportProvider extends AbstractReportProvider
{

    private static final String CUSTOM_DATA = "custom_data_logs";

    /**
     * {@inheritDoc}
     */
    @Override
    public Object createReportFragment()
    {
        final CustomLogsReport report = new CustomLogsReport();

        final Path resultsDir = ((ReportGeneratorConfiguration) getConfiguration()).getResultsDirectory().getPath();
        final Path targetDir = Paths.get(getConfiguration().getReportDirectory() + File.separator + CUSTOM_DATA);
    
        CustomLogFinder finder = new CustomLogFinder();
        
        finder.setBaseDir(resultsDir);
        finder.setTargetDir(targetDir);
        
        try
        {
            Files.walkFileTree(resultsDir, finder);
        }
        catch (IOException e)
        {
            System.err.println("Failed to walk file tree searching for custom data logs. Cause: " + e.getMessage());
        }
        finally 
        {
            finder.closeAllStreams(); //this closes the ZipOutputStreams, so this MUST be done
        }

        //zip up the report directories for every found scope, then add the link/size info
        Set<String> scopes = finder.getResult();
        
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
                System.err.println("Unable to collect information for custom data logs for " + scope + ". Cause: " + e.getMessage());
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
    
    public static class CustomLogFinder extends SimpleFileVisitor<Path> 
    {
        Map<String, ZipOutputStream> foundScopes = new HashMap<String, ZipOutputStream>();
        
        Path baseDir = null;
        Path targetDir = null;
        
        public void setBaseDir(Path baseDir)
        {
            this.baseDir = baseDir;
        }
        
        public void setTargetDir(Path targetDir)
        {
            this.targetDir = targetDir;
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attr) throws IOException
        {
            if (attr.isRegularFile() && file.getFileName().toString().startsWith(XltConstants.CUSTOM_LOG_PREFIX)) 
            {
                final String scopeName = file.getFileName().toString().substring(XltConstants.CUSTOM_LOG_PREFIX.length(), file.getFileName().toString().lastIndexOf('.'));
                
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
                scopeStream.putNextEntry(new ZipEntry(baseDir.relativize(file).toString()));
                Files.copy(file, scopeStream);
                scopeStream.closeEntry();
            }  
            return FileVisitResult.CONTINUE;
        }
        
        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attr) 
        {
            if (attr.isDirectory() && XltConstants.CONFIG_DIR_NAME.equals(dir.getFileName().toString()))
            {
                return FileVisitResult.SKIP_SUBTREE;
            } 
            return FileVisitResult.CONTINUE;
        }       
        
        void closeAllStreams() 
        {
            for (ZipOutputStream zos : foundScopes.values())
            {
                try {
                    // VERY IMPORTANT: CLOSE ALL OPEN STREAMS
                    zos.close();
                } 
                catch (IOException e) {
                    System.err.println("Unable to zip custom data logs to report. Cause: " + e.getMessage());
                }
            }
        }
        
        Set<String> getResult() 
        {
            if (!foundScopes.isEmpty())
            {
                System.out.format("Found custom data logs for scopes: %s \n", foundScopes.keySet());
            }
            return foundScopes.keySet();
        }
    }
}
