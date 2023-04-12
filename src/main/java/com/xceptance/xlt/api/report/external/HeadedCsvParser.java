/*
 * Copyright (c) 2005-2022 Xceptance Software Technologies GmbH
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
package com.xceptance.xlt.api.report.external;

import java.util.List;

import com.xceptance.common.util.CsvUtils;

/**
 * Parses lines of a CSV file. Addresses columns by their column headline. It is assumed that the first line in file is
 * the head-line. For any values to be parsed the {@link #setValueNames(java.util.Set)} has to be called with the names
 * of the columns which should be parsed (the time is stored in an extra field, so you do not have to give it as value
 * name. The time column has to be the first one in the csv file).
 *
 * @author Matthias Ullrich (Xceptance Software Technologies GmbH)
 */
public class HeadedCsvParser extends SimpleCsvParser
{
    /**
     * Column headlines.
     */
    private List<String> heads = null;

    /**
     * {@inheritDoc}
     */
    @Override
    public ValueSet parse(final String line)
    {
        if (heads == null)
        {
            // first line is header line
            heads = CsvUtils.decode(line, getFieldSeparator());
            return null;
        }

        return super.parse(line);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getName(final int i)
    {
        return (heads != null && i < heads.size()) ? heads.get(i) : Integer.toString(i);
    }
}
