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

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.xceptance.common.util.ProductInformation;
import com.xceptance.xlt.mastercontroller.TestCaseLoadProfileConfiguration;

/**
 * Represents the configuration values used during the test run.
 */
@XStreamAlias("configuration")
public class ConfigurationReport
{
    /**
     * The plain properties.
     */
    public Properties properties = new Properties();

    /**
     * The version information
     */
    public ProductInformation version;

    /**
     * The custom command line settings for the agent's JVM.
     */
    public List<String> customJvmArgs = new ArrayList<String>();

    /**
     * The detailed load profile.
     */
    public List<LoadProfileConfigurationReport> loadProfile = new ArrayList<LoadProfileConfigurationReport>();

    /**
     * The test comments.
     */
    public List<String> comments = new ArrayList<String>();

    /**
     * The name of the (test) project.
     */
    public String projectName;

    /**
     * The target height for charts.
     */
    public int chartHeight;

    /**
     * The target width for charts.
     */
    public int chartWidth;
}
