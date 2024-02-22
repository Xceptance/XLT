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
package com.xceptance.xlt.report.external.config;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlTransient;

/**
 * @author matthias.ullrich
 */
public class DataFileConfig
{
    @XmlTransient
    private static final String DEFAULT_ENCODING = "UTF-8";

    private String fileName;

    private String encoding = DEFAULT_ENCODING;

    private String parserClassName;

    private String headline;

    private String description;

    private final List<ChartConfig> charts = new ArrayList<ChartConfig>();

    private final List<TableConfig> tables = new ArrayList<TableConfig>();

    private final List<Property> properties = new ArrayList<Property>();

    /**
     * data file name
     * 
     * @return resource data file name
     */
    @XmlAttribute(name = "source", required = true)
    public String getFileName()
    {
        return fileName;
    }

    /**
     * Get file encoding type.
     * 
     * @return file encoding type
     */
    @XmlAttribute(name = "encoding")
    public String getEncoding()
    {
        return encoding != null ? encoding : DEFAULT_ENCODING;
    }

    /**
     * Get parser class name.
     * 
     * @return parser class name
     */
    @XmlAttribute(name = "parserClass")
    public String getParserClassName()
    {
        return parserClassName;
    }

    /**
     * Get file report headline.
     * 
     * @return file report headline
     */
    @XmlElement(name = "headline")
    public String getHeadline()
    {
        return headline;
    }

    /**
     * Get file report description.
     * 
     * @return file report headline
     */
    @XmlElement(name = "description")
    public String getDescription()
    {
        return description;
    }

    /**
     * Get the report chart configurations.
     * 
     * @return report chart configurations
     */
    @XmlElementWrapper(name = "charts")
    @XmlElement(name = "chart")
    public List<ChartConfig> getCharts()
    {
        return charts;
    }

    /**
     * Get the report table configurations.
     * 
     * @return report table configurations
     */
    @XmlElementWrapper(name = "tables")
    @XmlElement(name = "table")
    public List<TableConfig> getTables()
    {
        return tables;
    }

    /**
     * Get the properties.
     * 
     * @return properties
     */
    @XmlElementWrapper(name = "properties")
    @XmlElement(name = "property")
    public List<Property> getProperties()
    {
        return properties;
    }

    /*
     * DO NOT REMOVE METHODS BELOW !
     */

    @SuppressWarnings("unused")
    private void setFileName(final String fileName)
    {
        this.fileName = fileName;
    }

    @SuppressWarnings("unused")
    private void setEncoding(final String encoding)
    {
        this.encoding = encoding;
    }

    @SuppressWarnings("unused")
    private void setParserClassName(final String parserClassName)
    {
        this.parserClassName = parserClassName;
    }

    @SuppressWarnings("unused")
    private void setHeadline(final String headline)
    {
        this.headline = headline;
    }

    @SuppressWarnings("unused")
    private void setDescription(final String description)
    {
        this.description = description;
    }
}
