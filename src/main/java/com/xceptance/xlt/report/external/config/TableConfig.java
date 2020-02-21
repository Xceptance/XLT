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
public class TableConfig
{
    public enum TableType
    {
        minmaxavg, plain
    };

    @XmlTransient
    private static final String DEFAULT_TABLE_TITLE = "Unnamed";

    @XmlTransient
    private static final TableType DEFAULT_TABLE_TYPE = TableType.minmaxavg;

    private String title = DEFAULT_TABLE_TITLE;

    private TableType type = DEFAULT_TABLE_TYPE;

    private final List<ValueConfig> rows = new ArrayList<ValueConfig>();

    private final List<ValueConfig> columns = new ArrayList<ValueConfig>();

    /**
     * table title
     * 
     * @return table title
     */
    @XmlAttribute(name = "title", required = false)
    public String getTitle()
    {
        return title;
    }

    /**
     * table type
     * 
     * @return table type
     */
    @XmlAttribute(name = "type", required = false)
    public TableType getType()
    {
        return type;
    }

    /**
     * @return table row configuration
     */
    @XmlElementWrapper(name = "rows")
    @XmlElement(name = "row", required = false)
    public List<ValueConfig> getRows()
    {
        return rows;
    }

    /**
     * @return table column configuration
     */
    @XmlElementWrapper(name = "cols")
    @XmlElement(name = "col", required = false)
    public List<ValueConfig> getColumns()
    {
        return columns;
    }

    /*
     * DO NOT REMOVE METHODS BELOW !
     */

    @SuppressWarnings("unused")
    private void setTitle(final String title)
    {
        this.title = title;
    }

    @SuppressWarnings("unused")
    private void setType(final TableType type)
    {
        this.type = type;
    }
}
