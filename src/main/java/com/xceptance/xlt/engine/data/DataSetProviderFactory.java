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
package com.xceptance.xlt.engine.data;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Level;

import com.xceptance.common.util.AbstractConfiguration;
import com.xceptance.common.util.ParameterCheckUtils;
import com.xceptance.xlt.api.data.DataSetProvider;
import com.xceptance.xlt.api.data.DataSetProviderException;
import com.xceptance.xlt.api.util.XltLogger;
import com.xceptance.xlt.api.util.XltProperties;

/**
 *  
 */
public class DataSetProviderFactory
{
    /**
     * Singleton implementation of {@link DataSetProviderFactory}.
     */
    private static class SingletonHolder
    {
        private static final DataSetProviderFactory factory = new DataSetProviderFactory();
    }

    /**
     * Returns the one and only {@link DataSetProviderFactory} instance.
     * 
     * @return the factory singleton
     */
    public static DataSetProviderFactory getInstance()
    {
        return SingletonHolder.factory;
    }

    /**
     * The registered data set providers.
     */
    private final Map<String, Class<? extends DataSetProvider>> providers = new LinkedHashMap<String, Class<? extends DataSetProvider>>();

    /**
     * Constructor.
     */
    @SuppressWarnings("unchecked")
    private DataSetProviderFactory()
    {
        // register the default data set providers
        registerDataSetProvider("csv", CsvDataSetProvider.class);
        registerDataSetProvider("xml", DomXmlDataSetProvider.class);
        registerDataSetProvider("sql", JdbcDataSetProvider.class);

        // register the custom data set providers
        final AbstractConfiguration config = new AbstractConfiguration();
        config.addProperties(XltProperties.getInstance().getProperties());

        final Set<String> extensions = config.getPropertyKeyFragment("com.xceptance.xlt.data.dataSetProviders.");
        for (final String extension : extensions)
        {
            final Class<?> clazz = config.getClassProperty("com.xceptance.xlt.data.dataSetProviders." + extension, null);

            if (clazz != null)
            {
                if (DataSetProvider.class.isAssignableFrom(clazz))
                {
                    registerDataSetProvider(extension, (Class<? extends DataSetProvider>) clazz);
                }
                else
                {
                    if (XltLogger.runTimeLogger.isEnabledFor(Level.ERROR))
                    {
                        XltLogger.runTimeLogger.error(String.format("Data set provider class '%s' registered for file extension '%s' does not implement interface '%s'",
                                                                    clazz.getName(), extension, DataSetProvider.class.getName()));
                    }
                }
            }
        }
    }

    /**
     * Registers a data set provider implementation class for a file extension.
     * 
     * @param fileExtension
     *            the file extension
     * @param dataSetProviderClass
     *            the provider class
     */
    public void registerDataSetProvider(final String fileExtension, final Class<? extends DataSetProvider> dataSetProviderClass)
    {
        ParameterCheckUtils.isNonEmptyString(fileExtension, "fileExtension");

        if (XltLogger.runTimeLogger.isDebugEnabled())
        {
            XltLogger.runTimeLogger.debug(String.format("Registering data set provider '%s' for file extension '%s'",
                                                        dataSetProviderClass.getName(), fileExtension));
        }

        providers.put(fileExtension, dataSetProviderClass);
    }

    /**
     * Unregisters a data set provider implementation class for a file extension.
     * 
     * @param fileExtension
     *            the file extension
     */
    public void unregisterDataSetProvider(final String fileExtension)
    {
        ParameterCheckUtils.isNonEmptyString(fileExtension, "fileExtension");

        providers.remove(fileExtension);
    }

    /**
     * Returns all file extensions for which a data set provider has been registered.
     * 
     * @return the set of file extensions
     */
    public Set<String> getRegisteredFileExtensions()
    {
        return providers.keySet();
    }

    /**
     * Creates a data set provider implementation for the given data file extension.
     * 
     * @param fileExtension
     *            the file extension
     * @return the {@link DataSetProvider} instance
     * @throws DataSetProviderException
     *             if the provider cannot be created
     */
    public DataSetProvider createDataSetProvider(final String fileExtension) throws DataSetProviderException
    {
        ParameterCheckUtils.isNonEmptyString(fileExtension, "fileExtension");

        final Class<? extends DataSetProvider> dataSetProviderClass = providers.get(fileExtension);
        if (dataSetProviderClass == null)
        {
            throw new DataSetProviderException("No data set provider registered for file extension: " + fileExtension);
        }
        else
        {
            try
            {
                return dataSetProviderClass.newInstance();
            }
            catch (final Exception e)
            {
                throw new DataSetProviderException("Failed to instantiate data set provider", e);
            }
        }
    }
}
