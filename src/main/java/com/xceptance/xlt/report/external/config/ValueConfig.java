/*
 * Copyright (c) 2005-2020 Xceptance Software Technologies GmbH
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

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;

/**
 * @author matthias.ullrich
 */
public class ValueConfig
{
    @XmlTransient
    private static final String DEFAULT_DATA_TYPE = "double";

    private String valueName;

    private String title;

    private String description;

    private String unit;

    private String dataType = DEFAULT_DATA_TYPE;

    /**
     * series name or index
     * 
     * @return series name or index
     */
    @XmlAttribute(name = "valueName", required = true)
    public String getValueName()
    {
        return valueName;
    }

    /**
     * series title
     * 
     * @return series title
     */
    @XmlAttribute(name = "title", required = false)
    public String getTitle()
    {
        return title != null ? title : valueName;
    }

    /**
     * series description
     * 
     * @return series description
     */
    @XmlAttribute(name = "description", required = false)
    public String getDescription()
    {
        return description;
    }

    /**
     * series measuring unit
     * 
     * @return series measuring unit
     */
    @XmlAttribute(name = "unit", required = false)
    public String getUnit()
    {
        return unit;
    }

    /**
     * data type
     * 
     * @return data type
     */
    @XmlAttribute(name = "dataType", required = false)
    public String getDataType()
    {
        return dataType;
    }

    /*
     * DO NOT REMOVE METHODS BELOW !
     */

    @SuppressWarnings("unused")
    private void setValueName(final String valueName)
    {
        this.valueName = valueName;
    }

    @SuppressWarnings("unused")
    private void setTitle(final String title)
    {
        this.title = title;
    }

    @SuppressWarnings("unused")
    private void setDescription(final String description)
    {
        this.description = description;
    }

    @SuppressWarnings("unused")
    private void setUnit(final String unit)
    {
        this.unit = unit;
    }

    @SuppressWarnings("unused")
    private void setDataType(final String dataType)
    {
        this.dataType = dataType;
    }
}
