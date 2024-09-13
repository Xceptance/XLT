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

import static java.nio.file.FileVisitResult.CONTINUE;
import static java.nio.file.FileVisitResult.SKIP_SUBTREE;
import static java.nio.file.StandardCopyOption.COPY_ATTRIBUTES;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

import java.io.File;
import java.io.IOException;
import java.nio.file.CopyOption;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.io.FileUtils;

import com.xceptance.common.util.zip.ZipUtils;
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
    
        CustomLogFinder finder = new CustomLogFinder();
        
        finder.setResultsDir(resultsDir.toString());
        finder.setReportDir(getConfiguration().getReportDirectory().getAbsolutePath());
        try
        {
            Files.walkFileTree(resultsDir, finder);
        }
        catch (IOException e)
        {
            System.err.println("Failed to walk file tree searching for custom data logs. Cause: " + e.getMessage());
        }

        //zip up the report directories for every found scope, then add the link/size info
        Set<String> scopes = finder.getResult();
        
        for (String scope : scopes)
        {    
            CustomLogReport clr = new CustomLogReport();
            clr.scope = scope;
            
            Path sourcePath = Paths.get(getConfiguration().getReportDirectory() + File.separator + scope + File.separator);
            Path targetDir = Paths.get(getConfiguration().getReportDirectory() + File.separator + CUSTOM_DATA);
            Path targetPath = Paths.get(targetDir + File.separator + scope + ".zip");
            
            try
            {
                targetDir.toFile().mkdirs();
                ZipUtils.zipDirectory(sourcePath.toFile(), targetPath.toFile());
                FileUtils.deleteDirectory(sourcePath.toFile());
                
                clr.size = Files.size(targetPath);
                clr.path = Paths.get(CUSTOM_DATA + File.separator + scope + ".zip").toString();
            }
            catch (IOException e)
            {
                System.err.println("Unable to zip custom data logs. Cause: " + e.getMessage());
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
        Set<String> foundScopes = new HashSet<String>();
        Set<Path> foundScopeFiles = new HashSet<Path>();
        
        String resultsDir = null;
        String reportDir = null;
        
        boolean containsTimers = false;
        
        public void setResultsDir(String resultsDir)
        {
            this.resultsDir = resultsDir;
        }
        
        public void setReportDir(String reportDir)
        {
            this.reportDir = reportDir;
        }

        // Print information about each type of file.
        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attr) 
        {
            if (attr.isRegularFile() && file.getFileName().toString().startsWith(XltConstants.TIMER_FILENAME)) 
            {
                containsTimers = true;
            } 
            if (attr.isRegularFile() && !(file.getFileName().toString().startsWith(XltConstants.TIMER_FILENAME))) 
            {
                foundScopeFiles.add(file);
            } 
            return CONTINUE;
        }
        
        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attr) 
        {
            containsTimers = false;
            foundScopeFiles = new HashSet<Path>();
            if (attr.isDirectory() && XltConstants.CONFIG_DIR_NAME.equals(dir.getFileName().toString()))
            {
                return SKIP_SUBTREE;
            } 
            return CONTINUE;
        }
        
        @Override
        public FileVisitResult postVisitDirectory(Path dir, IOException ex) 
        {
            if (containsTimers && !foundScopeFiles.isEmpty())
            {
                for (Path scopeFile : foundScopeFiles)
                {
                    final String scopeName = scopeFile.getFileName().toString().substring(0, scopeFile.getFileName().toString().lastIndexOf('.'));
                    foundScopes.add(scopeName);
                    
                    String target = reportDir + File.separator + scopeName + scopeFile.toString().substring(resultsDir.length());
                    File targetf = new File(target);
                    targetf.mkdirs();
                    
                    try
                    {
                        Files.copy(scopeFile, Paths.get(target), new CopyOption[] { COPY_ATTRIBUTES, REPLACE_EXISTING });
                    }
                    catch (IOException e)
                    {
                        System.err.println("Failed to copy custom data log file to report. Cause: " + e.getMessage());
                    }
                }
                containsTimers = false;
                
                
            } 
            return CONTINUE;
        }
        
        
        Set<String> getResult() 
        {
            System.out.format("Found custom data logs for scopes: %s \n", foundScopes);
            return foundScopes;
        }
    }
}
