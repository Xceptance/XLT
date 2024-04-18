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
package com.xceptance.xlt.showcases.flow;

import java.util.LinkedList;
import java.util.List;

/**
 * Base class for storing crawler flow configuration.
 */
public class CrawlerFlowConfig
{
    /**
     * The depth of recursion.
     */
    private int depthOfRecursion = 1;

    /**
     * The indicator for the XSS check
     */
    private boolean checkXSS = false;

    /**
     * Indicators for duplicate urls
     */
    private String[] urlIndicators = {};

    /**
     * Exclude patterns for urls
     */
    private List<String> excludePatterns = new LinkedList<String>();

    /**
     * Include patterns for urls
     */
    private List<String> includePatterns = new LinkedList<String>();

    /**
     * Runtime of crawler
     */
    private int runtime = 5;

    /**
     * Should we check links to external pages
     */
    private boolean proceedExternals = false;

    /**
     * Text patterns which are required on each page
     */
    private List<String> requiredText = new LinkedList<String>();

    /**
     * Text patterns which are disallowed on each page
     */
    private List<String> disallowedText = new LinkedList<String>();

    /**
     * Configuration for the xss check
     */
    private XSSCheckFlowConfig xssCheckConfig = new XSSCheckFlowConfig();

    public CrawlerFlowConfig()
    {
        // TODO Auto-generated constructor stub
    }

    /**
     * @return the depthOfRecursion
     */
    public int getDepthOfRecursion()
    {
        return depthOfRecursion;
    }

    /**
     * @param depthOfRecursion
     *            the depthOfRecursion to set
     */
    public void setDepthOfRecursion(final int depthOfRecursion)
    {
        this.depthOfRecursion = depthOfRecursion;
    }

    /**
     * @return the checkXSS
     */
    public boolean isCheckXSS()
    {
        return checkXSS;
    }

    /**
     * @param checkXSS
     *            the checkXSS to set
     */
    public void setCheckXSS(final boolean checkXSS)
    {
        this.checkXSS = checkXSS;
    }

    /**
     * @return the urlIndicators
     */
    public String[] getUrlIndicators()
    {
        return urlIndicators;
    }

    /**
     * @param urlIndicators
     *            the urlIndicators to set
     */
    public void setUrlIndicators(final String[] urlIndicators)
    {
        this.urlIndicators = urlIndicators;
    }

    /**
     * @return the excludePatterns
     */
    public List<String> getExcludePatterns()
    {
        return excludePatterns;
    }

    /**
     * @param excludePatterns
     *            the excludePatterns to set
     */
    public void setExcludePatterns(final List<String> excludePatterns)
    {
        this.excludePatterns = excludePatterns;
    }

    /**
     * @return the includePatterns
     */
    public List<String> getIncludePatterns()
    {
        return includePatterns;
    }

    /**
     * @param includePatterns
     *            the includePatterns to set
     */
    public void setIncludePatterns(final List<String> includePatterns)
    {
        this.includePatterns = includePatterns;
    }

    /**
     * @return the runtime
     */
    public int getRuntime()
    {
        return runtime;
    }

    /**
     * @param runtime
     *            the runtime to set
     */
    public void setRuntime(final int runtime)
    {
        this.runtime = runtime;
    }

    /**
     * @return the proceedExternals
     */
    public boolean isProceedExternals()
    {
        return proceedExternals;
    }

    /**
     * @param proceedExternals
     *            the proceedExternals to set
     */
    public void setProceedExternals(final boolean proceedExternals)
    {
        this.proceedExternals = proceedExternals;
    }

    /**
     * @return the xssCheckConfig
     */
    public XSSCheckFlowConfig getXssCheckConfig()
    {
        return xssCheckConfig;
    }

    /**
     * @param xssCheckConfig
     *            the xssCheckConfig to set
     */
    public void setXssCheckConfig(final XSSCheckFlowConfig xssCheckConfig)
    {
        this.xssCheckConfig = xssCheckConfig;
    }

    /**
     * @return the requiredText
     */
    public List<String> getRequiredText()
    {
        return requiredText;
    }

    /**
     * @param requiredText
     *            the requiredText to set
     */
    public void setRequiredText(final List<String> requiredText)
    {
        this.requiredText = requiredText;
    }

    /**
     * @return the disallowedText
     */
    public List<String> getDisallowedText()
    {
        return disallowedText;
    }

    /**
     * @param disallowedText
     *            the disallowedText to set
     */
    public void setDisallowedText(final List<String> disallowedText)
    {
        this.disallowedText = disallowedText;
    }
}
