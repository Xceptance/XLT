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

/**
 * Special structure to hold user data for universal use.
 * 
 * @author René Schwietzke (Xceptance Software Technologies GmbH)
 */
public class Account
{
    private String login;

    private String password;

    private String zip;

    private String firstName;

    private String lastName;

    private String email;

    private String birthday;

    private String country;

    private String city;

    private String phone;

    private String address1;

    private String address2;

    /**
     * Returns the value of the 'login' attribute.
     * 
     * @return the value of login
     */
    public String getLogin()
    {
        return login;
    }

    /**
     * Returns the value of the 'password' attribute.
     * 
     * @return the value of password
     */
    public String getPassword()
    {
        return password;
    }

    /**
     * Returns the value of the 'zip' attribute.
     * 
     * @return the value of zip
     */
    public String getZip()
    {
        return zip;
    }

    /**
     * Returns the value of the 'firstName' attribute.
     * 
     * @return the value of firstName
     */
    public String getFirstName()
    {
        return firstName;
    }

    /**
     * Returns the value of the 'lastName' attribute.
     * 
     * @return the value of lastName
     */
    public String getLastName()
    {
        return lastName;
    }

    /**
     * Returns the value of the 'email' attribute.
     * 
     * @return the value of email
     */
    public String getEmail()
    {
        return email;
    }

    /**
     * Returns the value of the 'birthday' attribute.
     * 
     * @return the value of birthday
     */
    public String getBirthday()
    {
        return birthday;
    }

    /**
     * Returns the value of the 'country' attribute.
     * 
     * @return the value of country
     */
    public String getCountry()
    {
        return country;
    }

    /**
     * Returns the value of the 'city' attribute.
     * 
     * @return the value of city
     */
    public String getCity()
    {
        return city;
    }

    /**
     * Returns the value of the 'phone' attribute.
     * 
     * @return the value of phone
     */
    public String getPhone()
    {
        return phone;
    }

    /**
     * Returns the value of the 'address1' attribute.
     * 
     * @return the value of address1
     */
    public String getAddress1()
    {
        return address1;
    }

    /**
     * Returns the value of the 'address2' attribute.
     * 
     * @return the value of address2
     */
    public String getAddress2()
    {
        return address2;
    }

    /**
     * Sets the new value of the 'login' attribute.
     * 
     * @param login
     *            the new login value
     */
    public void setLogin(final String login)
    {
        this.login = login;
    }

    /**
     * Sets the new value of the 'password' attribute.
     * 
     * @param password
     *            the new password value
     */
    public void setPassword(final String password)
    {
        this.password = password;
    }

    /**
     * Sets the new value of the 'zip' attribute.
     * 
     * @param zip
     *            the new zip value
     */
    public void setZip(final String zip)
    {
        this.zip = zip;
    }

    /**
     * Sets the new value of the 'firstName' attribute.
     * 
     * @param firstName
     *            the new firstName value
     */
    public void setFirstName(final String firstName)
    {
        this.firstName = firstName;
    }

    /**
     * Sets the new value of the 'lastName' attribute.
     * 
     * @param lastName
     *            the new lastName value
     */
    public void setLastName(final String lastName)
    {
        this.lastName = lastName;
    }

    /**
     * Sets the new value of the 'email' attribute.
     * 
     * @param email
     *            the new email value
     */
    public void setEmail(final String email)
    {
        this.email = email;
    }

    /**
     * Sets the new value of the 'birthday' attribute.
     * 
     * @param birthday
     *            the new birthday value
     */
    public void setBirthday(final String birthday)
    {
        this.birthday = birthday;
    }

    /**
     * Sets the new value of the 'country' attribute.
     * 
     * @param country
     *            the new country value
     */
    public void setCountry(final String country)
    {
        this.country = country;
    }

    /**
     * Sets the new value of the 'city' attribute.
     * 
     * @param city
     *            the new city value
     */
    public void setCity(final String city)
    {
        this.city = city;
    }

    /**
     * Sets the new value of the 'phone' attribute.
     * 
     * @param phone
     *            the new phone value
     */
    public void setPhone(final String phone)
    {
        this.phone = phone;
    }

    /**
     * Sets the new value of the 'address1' attribute.
     * 
     * @param address1
     *            the new address1 value
     */
    public void setAddress1(final String address1)
    {
        this.address1 = address1;
    }

    /**
     * Sets the new value of the 'address2' attribute.
     * 
     * @param address2
     *            the new address2 value
     */
    public void setAddress2(final String address2)
    {
        this.address2 = address2;
    }
}
