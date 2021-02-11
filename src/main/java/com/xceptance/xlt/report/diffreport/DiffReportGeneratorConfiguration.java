/*
 * Copyright (c) 2005-2021 Xceptance Software Technologies GmbH
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
package com.xceptance.xlt.report.diffreport;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.xceptance.common.util.AbstractConfiguration;
import com.xceptance.xlt.common.XltConstants;
import com.xceptance.xlt.engine.XltExecutionContext;
import com.xceptance.xlt.report.util.ElementSpecification;
import com.xceptance.xlt.report.util.ReportUtils;

/**
 *
 */
public class DiffReportGeneratorConfiguration extends AbstractConfiguration
{
    private static final String PROP_PREFIX = XltConstants.XLT_PACKAGE_PATH + ".diffreportgenerator.";

    private static final String PROP_REPORTS_ROOT_DIR = PROP_PREFIX + "reports";

    private final File configDirectory;

    private final File homeDirectory;

    private final File reportsRootDirectory;

    public DiffReportGeneratorConfiguration() throws IOException
    {
        homeDirectory = XltExecutionContext.getCurrent().getXltHomeDir();
        configDirectory = XltExecutionContext.getCurrent().getXltConfigDir();

        loadProperties(new File(configDirectory, XltConstants.DIFF_REPORT_PROPERTY_FILENAME));

        File reportsRootDir = getFileProperty(PROP_REPORTS_ROOT_DIR, new File(homeDirectory, XltConstants.REPORT_ROOT_DIR));
        if (!reportsRootDir.isAbsolute())
        {
            reportsRootDir = new File(homeDirectory, reportsRootDir.getPath());
        }

        reportsRootDirectory = reportsRootDir;

    }

    /**
     * Returns the directory where the master controller's configuration is located.
     * 
     * @return the config directory
     */
    public File getConfigDirectory()
    {
        return configDirectory;
    }

    public List<ElementSpecification> getDiffElementSpecifications()
    {
        final List<ElementSpecification> specs = new ArrayList<ElementSpecification>();

        // HACK: read from configuration instead
        specs.add(new ElementSpecification("/testreport/transactions/transaction", "name"));
        specs.add(new ElementSpecification("/testreport/actions/action", "name"));
        specs.add(new ElementSpecification("/testreport/requests/request", "name"));
        specs.add(new ElementSpecification("/testreport/pageLoadTimings/pageLoadTiming", "name"));
        specs.add(new ElementSpecification("/testreport/customTimers/customTimer", "name"));
        specs.add(new ElementSpecification("/testreport/summary/*", "name"));

        return specs;
    }

    public List<ElementSpecification> getCopyElementSpecifications()
    {
        final List<ElementSpecification> specs = new ArrayList<ElementSpecification>();

        // HACK: read from configuration instead
        specs.add(new ElementSpecification("/testreport/general", null));
        specs.add(new ElementSpecification("/testreport/configuration/loadProfile", null));
        specs.add(new ElementSpecification("/testreport/configuration/comments", null));
        specs.add(new ElementSpecification("/testreport/configuration/properties", null));
        specs.add(new ElementSpecification(ReportUtils.XPATH_PROJECT_NAME, null));

        return specs;
    }

    /**
     * Returns the master controller's home directory.
     * 
     * @return the home directory
     */
    public File getHomeDirectory()
    {
        return homeDirectory;
    }

    /**
     * Returns the root report directory
     * 
     * @return the root report directory
     */
    public File getReportsRootDirectory()
    {
        return reportsRootDirectory;
    }
}
