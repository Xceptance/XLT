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
package com.xceptance.xlt.api.data;

import org.junit.Assert;
import org.junit.Test;

public class AccountTest
{
    @Test
    public void testGetterSetter()
    {
        final Account account = new Account();

        final String ADDRESS1 = "addr1";
        account.setAddress1(ADDRESS1);
        Assert.assertEquals(ADDRESS1, account.getAddress1());
        final String ADDRESS2 = "addr2";
        account.setAddress2(ADDRESS2);
        Assert.assertEquals(ADDRESS2, account.getAddress2());
        final String ZIP = "07743";
        account.setZip(ZIP);
        Assert.assertEquals(ZIP, account.getZip());
        final String CITY = "Jena";
        account.setCity(CITY);
        Assert.assertEquals(CITY, account.getCity());
        final String LOGIN = "dude";
        account.setLogin(LOGIN);
        Assert.assertEquals(LOGIN, account.getLogin());
        final String PASSWORD = "dude8161!!";
        account.setPassword(PASSWORD);
        Assert.assertEquals(PASSWORD, account.getPassword());
        final String FIRSTNAME = "Hugo";
        account.setFirstName(FIRSTNAME);
        Assert.assertEquals(FIRSTNAME, account.getFirstName());
        final String LASTNAME = "Balder";
        account.setLastName(LASTNAME);
        Assert.assertEquals(LASTNAME, account.getLastName());
        final String COUNTRY = "Germany";
        account.setCountry(COUNTRY);
        Assert.assertEquals(COUNTRY, account.getCountry());
        final String BIRTHDAY = "10.12.1121";
        account.setBirthday(BIRTHDAY);
        Assert.assertEquals(BIRTHDAY, account.getBirthday());
        final String EMAIL = "test@test.com";
        account.setEmail(EMAIL);
        Assert.assertEquals(EMAIL, account.getEmail());
        final String PHONE = "555-111-22322";
        account.setPhone(PHONE);
        Assert.assertEquals(PHONE, account.getPhone());
    }
}
