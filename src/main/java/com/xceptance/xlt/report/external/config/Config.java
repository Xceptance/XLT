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
