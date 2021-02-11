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
package posters.loadtest.util;

import java.util.UUID;

import org.apache.commons.lang3.RandomStringUtils;

import com.xceptance.xlt.api.data.GeneralDataProvider;

/**
 * Provides a customer account.
 */
public class Account
{
    /**
     * the email address
     */
    private String email;

    /**
     * the first name
     */
    private String firstName;

    /**
     * the last name
     */
    private String lastName;

    /**
     * the password
     */
    private String password;

    /**
     * Create generic generated account.
     */
    public Account()
    {
        final GeneralDataProvider provider = GeneralDataProvider.getInstance();

        firstName = provider.getFirstName(false);
        lastName = provider.getLastName(false);
        email = RandomStringUtils.randomAlphanumeric(2) + UUID.randomUUID().toString().replaceAll("-", "").substring(0, 12) + "@varmail.de";
        password = RandomStringUtils.randomAlphanumeric(10);
    }

    public String getEmail()
    {
        return email;
    }

    public void setEmail(final String email)
    {
        this.email = email;
    }

    public String getFirstName()
    {
        return firstName;
    }

    public void setFirstName(final String firstName)
    {
        this.firstName = firstName;
    }

    public String getLastName()
    {
        return lastName;
    }

    public void setLastName(final String lastName)
    {
        this.lastName = lastName;
    }

    public String getPassword()
    {
        return password;
    }

    public void setPassword(final String password)
    {
        this.password = password;
    }
}
