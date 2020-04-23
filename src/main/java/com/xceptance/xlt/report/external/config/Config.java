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

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author matthias.ullrich
 */
@XmlRootElement(name = "config")
public class Config
{
    private final List<DataFileConfig> files = new ArrayList<DataFileConfig>();

    /**
     * Get the data file configuration.
     * 
     * @return data file configuration
     */
    @XmlElementWrapper(name = "files", required = true)
    @XmlElement(name = "file")
    public List<DataFileConfig> getFiles()
    {
        return files;
    }
}
