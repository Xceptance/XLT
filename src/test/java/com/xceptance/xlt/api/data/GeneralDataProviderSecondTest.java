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
package com.xceptance.xlt.api.data;

import static org.easymock.EasyMock.expect;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

/**
 * Test the implementation of a specific GeneralDataProvider method.
 * 
 * @author Rene Schwietzke (Xceptance Software Technologies GmbH)
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(
    {
        GeneralDataProvider.class
    })
public class GeneralDataProviderSecondTest
{
    /**
     * getUniqueUserName
     */
    @Test
    public void testGetUniqueUserName()
    {
        final GeneralDataProvider provider = GeneralDataProvider.getInstance();

        PowerMock.mockStatic(UUID.class);
        final long l = 0xffffffffffffL;
        final UUID uuid = new UUID(l, l);

        expect(UUID.randomUUID()).andReturn(uuid);
        PowerMock.replay(UUID.class);

        final String user = provider.getUniqueUserName();
        Assert.assertEquals("user0000ffff-ffff-ff".substring(0, 20), user);
    }

    @Test(expected = FileNotFoundException.class)
    public void testGetDataProvider() throws Exception
    {
        PowerMock.mockStatic(DataProvider.class);

        expect(DataProvider.getInstance("default/companies.txt")).andThrow(new IOException("HD'oh"));
        PowerMock.replay(DataProvider.class);

        final GeneralDataProvider provider = GeneralDataProvider.getInstance();
        provider.getCompany(false);
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

        PowerMock.mockStatic(UUID.class);
        final long l = 0xffffffffffffL;
        final UUID uuid = new UUID(l, l);

        // 0000ffffffffffff0000ffffffffffff
        final String uuidString = "0000ffffffffffff0000ffffffffffff";

        expect(UUID.randomUUID()).andReturn(uuid).times(3);

        PowerMock.replay(UUID.class);
        Assert.assertTrue(provider.getUniqueEmail("r", "test.com", 15).matches("^r" + uuidString.substring(0, 14) + "@test.com$"));

        PowerMock.replay(UUID.class);
        Assert.assertTrue(provider.getUniqueEmail("r12-", "test.foo.com", 10).matches("^r12-" + uuidString.substring(0, 6) +
                                                                                          "@test.foo.com$"));

        PowerMock.replay(UUID.class);
        Assert.assertTrue(provider.getUniqueEmail("", "", 17).matches("^" + uuidString.substring(0, 17) + "@$"));
    }

}
