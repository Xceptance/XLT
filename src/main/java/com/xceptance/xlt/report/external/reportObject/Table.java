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
package com.xceptance.xlt.report.external.reportObject;

import java.util.ArrayList;
import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * @author matthias.ullrich
 */
@XStreamAlias("table")
public class Table
{
    @XStreamAlias("title")
    private String title;

    @XStreamAlias("headRow")
    private Row headRow;

    @XStreamAlias("bodyRows")
    private List<Row> bodyRows;

    @XStreamAlias("maxCols")
    private int maxCols = -1;

    /**
     * Adds a body row to the end of the table.
     * 
     * @param row
     *            the row to add
     */
    public void addRow(final Row row)
    {
        if (row != null)
        {
            if (bodyRows == null)
            {
                bodyRows = new ArrayList<Row>();
            }

            bodyRows.add(row);
        }
    }

    /**
     * Sets all body rows at once.
     * 
     * @param bodyRows
     *            the body rows
     */
    public void setBodyRows(final List<Row> bodyRows)
    {
        this.bodyRows = bodyRows;
    }

    /**
     * Returns all the body rows as a list.
     * 
     * @return the body rows
     */
    public List<Row> getBodyRows()
    {
        return bodyRows;
    }

    /**
     * Sets the head row.
     * 
     * @param row
     *            the head row
     */
    public void setHeadRow(final Row row)
    {
        if (row != null)
        {
            headRow = row;
        }
    }

    /**
     * Returns the head row.
     * 
     * @return the head row
     */
    public Row getHeadRow()
    {
        return headRow;
    }

    /**
     * Sets the table title.
     * 
     * @param title
     *            the title
     */
    public void setTitle(final String title)
    {
        this.title = title;
    }

    /**
     * Returns the table title.
     * 
     * @return the title
     */
    public String getTitle()
    {
        return title;
    }

    /**
     * Returns the highest cell count in all the table rows.
     * 
     * @return highest cell count in table rows
     */
    public int getMaxCols()
    {
        int maxCols_tmp = 0;

        if (headRow != null)
        {
            maxCols_tmp = Math.max(headRow.size(), maxCols_tmp);
        }

        if (bodyRows != null)
        {
            for (final Row row : bodyRows)
            {
                maxCols_tmp = Math.max(row.size(), maxCols_tmp);
            }
        }

        return maxCols_tmp;
    }

    /**
     * Call this method when table building is finished. Will internally store the highest cell count in all the table
     * rows.
     */
    public void finish()
    {
        if (maxCols < 0)
        {
            maxCols = getMaxCols();
        }
    }
}
