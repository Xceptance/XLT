/*
 * Copyright (c) 2005-2025 Xceptance Software Technologies GmbH
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

import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

/**
 * Test the implementation of a specific GeneralDataProvider method.
 * 
 * @author Rene Schwietzke (Xceptance Software Technologies GmbH)
 */
public class GeneralDataProviderSecondTest
{
    /**
     * getUniqueUserName
     */
    @Test
    public void testGetUniqueUserName()
    {
        final GeneralDataProvider provider = GeneralDataProvider.getInstance();
        final long l = 0xffffffffffffL;
        final UUID uuid = new UUID(l, l);

        try (MockedStatic<UUID> uuidMock = Mockito.mockStatic(UUID.class))
        {
            uuidMock.when(UUID::randomUUID).thenReturn(uuid);
            final String user = provider.getUniqueUserName();
            Assert.assertEquals("user0000ffff-ffff-ff".substring(0, 20), user);
        }
    }

    @Test(expected = NoSuchFileException.class)
    public void testGetDataProvider() throws Exception
    {
        try (MockedStatic<DataProvider> dataProviderMock = Mockito.mockStatic(DataProvider.class))
        {
            dataProviderMock.when(() -> DataProvider.getInstance("default/companies.txt")).thenThrow(new IOException("HD'oh"));
            final GeneralDataProvider provider = GeneralDataProvider.getInstance();
            provider.getCompany(false);
        }
    }

    /**
     * Test the unique email generation. Regular data creation with fixed UUID
     * 
     * @see GeneralDataProvider#getUniqueEmail(String, String, int)
     */
    @Test
    public void testGetUniqueEmail_FixedUpUUID()
    {
        final GeneralDataProvider provider = GeneralDataProvider.getInstance();
        final long l = 0xffffffffffffL;
        final UUID uuid = new UUID(l, l);

        // 0000ffffffffffff0000ffffffffffff
        final String uuidString = "0000ffffffffffff0000ffffffffffff";

        try (MockedStatic<UUID> uuidMock = Mockito.mockStatic(UUID.class))
        {
            uuidMock.when(UUID::randomUUID).thenReturn(uuid);
            Assert.assertTrue(provider.getUniqueEmail("r", "test.com", 15).matches("^r" + uuidString.substring(0, 14) + "@test.com$"));
            Assert.assertTrue(provider.getUniqueEmail("r12-", "test.foo.com", 10).matches("^r12-" + uuidString.substring(0, 6) +
                                                                                              "@test.foo.com$"));
            Assert.assertTrue(provider.getUniqueEmail("", "", 17).matches("^" + uuidString.substring(0, 17) + "@$"));
        }
    }
}
