package com.xceptance.xlt.report.external.reportObject;

import java.util.ArrayList;
import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * @author matthias.ullrich
 */
@XStreamAlias("row")
public class Row
{
    @XStreamAlias("description")
    public String description;

    @XStreamAlias("unit")
    public String unit;

    @XStreamAlias("cells")
    private List<Object> cells = null;

    /**
     * add a cell with goven content to the end of the row
     * 
     * @param o
     *            cell content
     */
    public void addCell(final Object o)
    {
        if (o != null)
        {
            if (cells == null)
            {
                cells = new ArrayList<Object>();
            }

            cells.add(o);
        }
    }

    /**
     * get all cells of the row
     * 
     * @return all cells of the row
     */
    public List<Object> getCells()
    {
        return cells;
    }

    /**
     * set the row cells
     * 
     * @param cells
     *            row cells
     */
    public void setCells(final List<Object> cells)
    {
        this.cells = cells;
    }

    /**
     * get the row size
     * 
     * @return row size
     */
    public int size()
    {
        return cells != null ? cells.size() : 0;
    }
}
