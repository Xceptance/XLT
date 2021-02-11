/*
 * Copyright (c) 2005-2021 Xceptance Software Technologies GmbH
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
package com.xceptance.xlt.engine.data;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;

import com.xceptance.xlt.api.data.DataSetProvider;
import com.xceptance.xlt.api.data.DataSetProviderException;
import com.xceptance.xlt.api.util.XltProperties;

/**
 * A {@link DataSetProvider} implementation that reads data sets from a JDBC data source. The respective SQL query is
 * read from the data file. The structure of the data file is as follows:
 * 
 * <pre>
 * select login as "userName", password as "password" from users;
 * </pre>
 */
public class JdbcDataSetProvider implements DataSetProvider
{
    private static final String PROP_JDBC_PASSWORD = "com.xceptance.xlt.data.dataSetProviders.jdbc.password";

    private static final String PROP_JDBC_USER_NAME = "com.xceptance.xlt.data.dataSetProviders.jdbc.userName";

    private static final String PROP_JDBC_URL = "com.xceptance.xlt.data.dataSetProviders.jdbc.url";

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Map<String, String>> getAllDataSets(final File dataFile) throws DataSetProviderException
    {
        final String url = XltProperties.getInstance().getProperty(PROP_JDBC_URL, "");
        final String userName = XltProperties.getInstance().getProperty(PROP_JDBC_USER_NAME, "");
        final String password = XltProperties.getInstance().getProperty(PROP_JDBC_PASSWORD, "");

        if (url.length() == 0)
        {
            throw new DataSetProviderException("JDBC data source URL is not configured");
        }

        if (userName.length() == 0)
        {
            throw new DataSetProviderException("JDBC user name is not configured");
        }

        Connection connection = null;

        try
        {
            // open the connection
            connection = DriverManager.getConnection(url, userName, password);

            // execute the SQL query
            final String sqlQuery = FileUtils.readFileToString(dataFile, StandardCharsets.UTF_8);
            final PreparedStatement statement = connection.prepareStatement(sqlQuery);
            final ResultSet resultSet = statement.executeQuery();

            // create the data sets from the returned rows
            final List<Map<String, String>> dataSets = new ArrayList<Map<String, String>>();

            while (resultSet.next())
            {
                final Map<String, String> dataSet = new LinkedHashMap<String, String>();
                dataSets.add(dataSet);

                final ResultSetMetaData metaData = resultSet.getMetaData();
                for (int i = 1; i <= metaData.getColumnCount(); i++)
                {
                    final String key = metaData.getColumnLabel(i);
                    final String value = resultSet.getString(i);

                    dataSet.put(key, value);
                }
            }

            return dataSets;
        }
        catch (final Exception ex)
        {
            throw new DataSetProviderException("Failed to retrieve data from database", ex);

        }
        finally
        {
            if (connection != null)
            {
                try
                {
                    connection.close();
                }
                catch (final SQLException ex)
                {
                    throw new DataSetProviderException("Failed to close database connection", ex);
                }
            }
        }
    }
}
