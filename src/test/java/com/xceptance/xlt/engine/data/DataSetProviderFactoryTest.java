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
package com.xceptance.xlt.engine.data;

import org.junit.Assert;
import org.junit.Test;

import com.xceptance.common.lang.ReflectionUtils;
import com.xceptance.xlt.api.data.DataSetProvider;
import com.xceptance.xlt.api.data.DataSetProviderException;
import com.xceptance.xlt.api.util.XltProperties;

/**
 * Tests the {@link DataSetProvider}.
 * 
 * @author Sebastian Oerding
 */
public class DataSetProviderFactoryTest
{
    @Test
    public void testFactoryClass()
    {
        final DataSetProviderFactory factory = DataSetProviderFactory.getInstance();
        factory.registerDataSetProvider("dummy", DummyDataSetProvider.class);
        Assert.assertTrue("Instance of wrong type returned when creating a data set provider",
                          factory.createDataSetProvider("dummy").getClass() == DummyDataSetProvider.class);
        Assert.assertTrue("Dummy provider missing", factory.getRegisteredFileExtensions().contains("dummy"));
        factory.unregisterDataSetProvider("dummy");
        Assert.assertFalse("Dummy provider not successfully removed", factory.getRegisteredFileExtensions().contains("dummy"));
        try
        {
            factory.createDataSetProvider("dummy");
            Assert.assertTrue("Provider has been removed, exception expected", false);
        }
        catch (final DataSetProviderException e)
        {
            Assert.assertEquals("Wrong exception message", "No data set provider registered for file extension: dummy", e.getMessage());
        }
    }

    @Test
    public void testConstructor()
    {
        XltProperties.getInstance().setProperty("com.xceptance.xlt.data.dataSetProviders.dummy",
                                                "com.xceptance.xlt.engine.data.DummyDataSetProvider");
        final DataSetProviderFactory newInstance = ReflectionUtils.getNewInstance(DataSetProviderFactory.class);

        Assert.assertTrue("Dummy provider not in place", newInstance.getRegisteredFileExtensions().contains("dummy"));
    }
}
