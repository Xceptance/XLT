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
    public List<TestCaseLoadProfileConfiguration> loadProfile = new ArrayList<TestCaseLoadProfileConfiguration>();

    /**
     * The test comments.
     */
    public List<String> comments = new ArrayList<String>();

    /**
     * The name of the (test) project.
     */
    public String projectName;
}
