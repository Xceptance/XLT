/*
 * Copyright (c) 2005-2024 Xceptance Software Technologies GmbH
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

/**
 * Provides a credit card.
 */
public class CreditCard
{
    /**
     * Credit card owner.
     */
    private String owner;

    /**
     * Credit card number.
     */
    private String number;

    /**
     * Month of credit card expiration.
     */
    private String expirationMonth;

    /**
     * Year of credit card expiration.
     */
    private String expirationYear;

    public CreditCard()
    {
        number = "4111111111111111";
        owner = "John Doe";
    }

    public CreditCard(final Account account)
    {
        number = "4111111111111111";
        owner = account.getFirstName() + " " + account.getLastName();
    }

    public String getOwner()
    {
        return owner;
    }

    public void setOwner(final String owner)
    {
        this.owner = owner;
    }

    public String getNumber()
    {
        return number;
    }

    public void setNumber(final String number)
    {
        this.number = number;
    }

    public String getExpirationMonth()
    {
        return expirationMonth;
    }

    public void setExpirationMonth(final String expirationMonth)
    {
        this.expirationMonth = expirationMonth;
    }

    public String getExpirationYear()
    {
        return expirationYear;
    }

    public void setExpirationYear(final String expirationYear)
    {
        this.expirationYear = expirationYear;
    }
}
