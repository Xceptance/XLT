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

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.vfs2.FileObject;

import com.xceptance.xlt.api.engine.Data;
import com.xceptance.xlt.api.report.AbstractReportProvider;
import com.xceptance.xlt.common.XltConstants;
import com.xceptance.xlt.report.ReportGeneratorConfiguration;

/**
 * Report provider generating a report fragment about the custom data logs collected during the test run
 */
public class CustomLogsReportProvider extends AbstractReportProvider
{

    /**
     * {@inheritDoc}
     */
    @Override
    public Object createReportFragment()
    {
        final CustomLogsReport report = new CustomLogsReport();

        final Path resultsDir = ((ReportGeneratorConfiguration) getConfiguration()).getResultsDirectory().getPath();
    
        CustomLogFinder finder = new CustomLogFinder();
        try
        {
            System.out.format("Walk file tree for %s \n", resultsDir);
            Files.walkFileTree(resultsDir, finder);
        }
        catch (IOException e)
        {
            System.err.println("Failed to get walk file tree searching for custom data logs. Cause: " + e.getMessage());
        }

        // add the found scopes
        report.scopes.addAll(finder.getResult());

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
        Set<String> foundScopesDir = new HashSet<String>();
        boolean containsTimers = false;

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
                foundScopesDir.add(file.getFileName().toString());
            } 
            return CONTINUE;
        }
        
        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attr) 
        {
            containsTimers = false;
            foundScopesDir = new HashSet<String>();
            if (attr.isDirectory() && XltConstants.CONFIG_DIR_NAME.equals(dir.getFileName().toString()))
            {
                System.out.format("SKIPPING %s \n", dir.getFileName());
                return SKIP_SUBTREE;
            } 
            return CONTINUE;
        }
        
        @Override
        public FileVisitResult postVisitDirectory(Path dir, IOException ex) 
        {
            if (containsTimers)
            {
                System.out.format("Timers in %s \n", dir.getFileName());
                foundScopes.addAll(foundScopesDir);
                System.out.format("Custom data files: %s \n", foundScopesDir);
                containsTimers = false;
            } 
            return CONTINUE;
        }
        
        
        Set<String> getResult() 
        {
            return foundScopes;
        }
    }
}
