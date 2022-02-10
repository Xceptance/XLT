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
package com.xceptance.xlt.api.data;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * A {@link DataSetProvider} implementation reads one or more test data sets from a data file and returns them to the
 * XLT framework, which in turn executes a certain test case once for each data set (data-driven tests). Note that this
 * feature is currently supported for script test cases only. The framework searches for test data files that are
 * associated with script test cases and reads them in using one of the known data set providers.
 * <p>
 * The XLT framework already knows some default data set providers for common file formats:
 * <ul>
 * <li>.csv - data sets are read from CSV files</li>
 * <li>.sql - data sets are read from a JDBC data source</li>
 * <li>.xml - data sets are read from an XML file</li>
 * </ul>
 * Custom implementations might override existing providers or add providers for new file formats. This is done by
 * configuration, for example in file "project.properties". Use the following property syntax:
 * 
 * <pre>
 * {@code
 * com.xceptance.xlt.data.dataSetProviders.<extension> = <class>
 * }
 * </pre>
 * 
 * To register class "com.yourcompany.FooDataSetProvider" for data set files with the extension ".foo", you need to
 * configure the following property:
 * 
 * <pre>
 * com.xceptance.xlt.data.dataSetProviders.foo = com.yourcompany.FooDataSetProvider
 * </pre>
 */
public interface DataSetProvider
{
    /**
     * Returns all data sets managed by this data set provider.
     * 
     * @param dataFile
     *            the data file
     * @return the list of data sets
     * @throws DataSetProviderException
     *             if an error occurred
     */
    public List<Map<String, String>> getAllDataSets(File dataFile) throws DataSetProviderException;
}
