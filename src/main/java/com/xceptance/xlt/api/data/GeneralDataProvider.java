/*
 * Copyright (c) 2005-2023 Xceptance Software Technologies GmbH
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
import java.io.IOException;
import java.util.UUID;
import java.util.regex.Pattern;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;

import com.xceptance.xlt.api.engine.GlobalClock;
import com.xceptance.xlt.api.util.XltRandom;

/**
 * The {@link GeneralDataProvider} class is a general data provider for commonly needed test data objects that comes in
 * handy all the time. It provides access to several fixed sets of data objects, which are backed by special predefined
 * data files in the XLT data directory ("[testsuite]/config/data/default"):
 * <ul>
 * <li>first names (firstnames.txt)</li>
 * <li>last names (lastnames.txt)</li>
 * <li>company names (companies.txt)</li>
 * <li>streets (streets.txt)</li>
 * <li>towns (towns.txt)</li>
 * <li>countries (countries.txt)</li>
 * </ul>
 * Each time this data provider is asked for data, a data object is chosen randomly from the respective data set. Note:
 * If the default data sets do not meet your needs, you may simply modify the data files.
 * <p>
 * This provider is also able to generate random data in special formats, such as:
 * <ul>
 * <li>phone numbers</li>
 * <li>domain names</li>
 * <li>email addresses</li>
 * </ul>
 * This data is not served from a data file, but generated on the fly.
 * <p>
 * Since this provider is implemented as a singleton, you first have to obtain the singleton instance via
 * {@link #getInstance()} before you can use this provider.
 * 
 * @see DataProvider
 * @author René Schwietzke (Xceptance Software Technologies GmbH)
 */
public class GeneralDataProvider
{
    /**
     * A single space character.
     */
    private static final char SPACE = ' ';

    /**
     * The at sign
     */
    private static final char AT = '@';

    /**
     * Regex to clean the UUID from -
     */
    private static final Pattern uuidCleanerPattern = Pattern.compile("-");

    /**
     * Private constructor for a singleton instance.
     */
    private GeneralDataProvider()
    {
    }

    /**
     * Returns the data provider for the given category. If no data provider exists yet, it will be created.
     */
    private DataProvider getDataProvider(final DataCategory name)
    {
        try
        {
            return DataProvider.getInstance(name.getFileName());
        }
        catch (final IOException e)
        {
            throw new RuntimeException("Failed to initialize data provider", e);
        }
    }

    /**
     * Returns the next random item of the given data category.
     * 
     * @param category
     *            the data category
     * @param removeWhitespace
     *            whether or not any whitespace is to be removed from the string
     * @return next data row (randomly chosen)
     */
    private String getNextItem(final DataCategory category, final boolean removeWhitespace)
    {
        final DataProvider provider = getDataProvider(category);

        return provider.getRandomRow(removeWhitespace);
    }

    /**
     * Returns a company name randomly chosen from the pool of company names.
     * 
     * @param removeWhitespace
     *            whether or not any whitespace is to be removed from the string
     * @return a company name
     */
    public String getCompany(final boolean removeWhitespace)
    {
        return getNextItem(DataCategory.COMPANIES, removeWhitespace);
    }

    /**
     * Returns a country name randomly chosen from the pool of country names.
     * 
     * @param removeWhitespace
     *            whether or not any whitespace is to be removed from the string
     * @return a country name
     */
    public String getCountry(final boolean removeWhitespace)
    {
        return getNextItem(DataCategory.COUNTRIES, removeWhitespace);
    }

    /**
     * Returns a first name randomly chosen from the pool of first names.
     * 
     * @param removeWhitespace
     *            whether or not any whitespace is to be removed from the string
     * @return a first name
     */
    public String getFirstName(final boolean removeWhitespace)
    {
        return getNextItem(DataCategory.FIRSTNAMES, removeWhitespace);
    }

    /**
     * Returns a town randomly chosen from the pool of towns.
     * 
     * @param removeWhitespace
     *            whether or not any whitespace is to be removed from the string
     * @return a town
     */
    public String getTown(final boolean removeWhitespace)
    {
        return getNextItem(DataCategory.TOWNS, removeWhitespace);
    }

    /**
     * Returns a last name randomly chosen from the pool of last names.
     * 
     * @param removeWhitespace
     *            whether or not any whitespace is to be removed from the string
     * @return a last name
     */
    public String getLastName(final boolean removeWhitespace)
    {
        return getNextItem(DataCategory.LASTNAMES, removeWhitespace);
    }

    /**
     * Returns a sentence.
     * 
     * @param removeWhitespace
     *            whether or not any whitespace is to be removed from the string
     * @return a sentence
     */
    public String getSentence(final boolean removeWhitespace)
    {
        return getNextItem(DataCategory.SENTENCES, removeWhitespace);
    }

    /**
     * Returns a text composed of several sentences.
     * 
     * @param sentenceCount
     *            the number of sentences to use
     * @param removeWhitespace
     *            whether or not any whitespace is to be removed from the string
     * @return a text
     */
    public String getText(final int sentenceCount, final boolean removeWhitespace)
    {
        final DataProvider provider = getDataProvider(DataCategory.SENTENCES);

        if (provider.getSize() == 0)
        {
            return StringUtils.EMPTY;
        }
        else
        {
            final StringBuilder sb = new StringBuilder(1024);

            for (int i = 0; i < sentenceCount; i++)
            {
                if (i > 0)
                {
                    sb.append(SPACE);
                }

                sb.append(provider.getRandomRow(false));
            }

            return removeWhitespace ? StringUtils.deleteWhitespace(sb.toString()) : sb.toString();
        }
    }

    /**
     * Returns a text composed of several sentences.
     * 
     * @param minSentenceCount
     *            the minimum number of sentences to return
     * @param maxSentenceCount
     *            the maximum number of sentences to return
     * @param removeWhitespace
     *            whether or not any whitespace is to be removed from the string
     * @return a text
     */
    public String getText(final int minSentenceCount, final int maxSentenceCount, final boolean removeWhitespace)
    {
        final int sentenceCount = XltRandom.nextInt(minSentenceCount, maxSentenceCount);

        return getText(sentenceCount, removeWhitespace);
    }

    /**
     * Creates an artificial Internet domain name in the format "xlt&lt;digits&gt;.com". The random part is the current
     * time stamp. Therefore the randomness is limited because a new name will appear every millisecond and concurrent
     * requests might create the same domain name.
     * 
     * @return an artificial domain name
     */
    private String getDynamicDomain()
    {
        final long now = GlobalClock.get().millis();
        return "xlt" + now + ".com";
    }

    /**
     * Returns a randomly generated email address. This email address does not necessarily exists. This email address is
     * not guaranteed to be unique.
     * 
     * @return an email address
     */
    public String getEmail()
    {
        return (getLastName(true) + AT + getDynamicDomain()).toLowerCase();
    }

    /**
     * Returns an email from the provided list without modifications. This functionality is needed, when the software
     * under test requires real email addresses to function or to prevent clogging of email systems under test.
     * 
     * @return an existing email address from the file, it returns null if no such email is available
     */
    public String getPredefinedEmail()
    {
        return getNextItem(DataCategory.EMAILS, false);
    }

    /**
     * Returns a random email with the given user name.
     * 
     * @param name
     *            local part of email address
     * @param removeWhitespace
     *            whether or not any whitespace is to be removed from the string.
     * @return an email address
     * @deprecated since 4.3.0, replaced by {@link #getEmail(String)} because parameter 'removeWhitespace' is not
     *             evaluated at all.
     */
    @Deprecated
    public String getEmail(final String name, final boolean removeWhitespace)
    {
        return getEmail(name);
    }

    /**
     * Returns a random email with the given user name. Whitespaces get removed (if any).
     * 
     * @param name
     *            local part of email address
     * @return an email address
     */
    public String getEmail(final String name)
    {
        final String cleanName = StringUtils.deleteWhitespace(name);
        final String emailAddress = new StringBuilder().append(cleanName).append(AT).append(getDynamicDomain()).toString().toLowerCase();

        return emailAddress;
    }

    /**
     * Returns a truly unique email address by generating a globally unique local part for a given domain. The local
     * part is created by utilizing the java.util.UUID method to get an immutable universally unique identifier. The -
     * characters form the UUID are stripped. The max length of the uuid part is 32 characters (example:
     * fd385d38126145948983629bc63f46d2). The format of email addresses is local-part@domain where the local-part may be
     * up to 64 characters long and the domain name may have a maximum of 253 characters – but the maximum 256
     * characters length of a forward or reverse path restricts the entire email address to be no more than 254
     * characters. (Source: http://en.wikipedia.org/wiki/Email_address)
     * 
     * @param prefix
     *            a prefix to be used for the local part. This should be short to avoid real long emails. This should be
     *            mostly be something like an r or s to avoid email starting with numbers.
     * @param domain
     *            the domain part of the email address. Should not contain the '@' sign.
     * @param length
     *            the max length of the local part. Please be advised that too short local parts will render this method
     *            useless. At least 12 characters are recommended.
     * @return a truly globally unique email address
     * @see java.util.UUID#randomUUID()
     * @since 4.3.0
     */
    public String getUniqueEmail(final String prefix, final String domain, final int length)
    {
        final String p = StringUtils.defaultString(StringUtils.deleteWhitespace(prefix));
        final int remainingLength = Math.min(Math.max(0, length - p.length()), 32);

        // the own pattern is faster then String.replaceAll because the compiled pattern is reused
        final String uuid = uuidCleanerPattern.matcher(UUID.randomUUID().toString()).replaceAll("");
        return new StringBuilder().append(p).append(uuid.substring(0, remainingLength)).append(AT).append(domain).toString();
    }

    /**
     * Convenience method for getting a truly unique email with a local part length of 20 characters. Only the domain
     * part is required. Example: xfd385d3812614594898@varmail.de
     * 
     * @param domain
     *            the domain part of the email address. Should not contain the '@' sign.
     * @return a truly globally unique email address
     * @see GeneralDataProvider#getUniqueEmail(String, String, int)
     * @since 4.3.0
     */
    public String getUniqueEmail(final String domain)
    {
        return getUniqueEmail("x", domain, 20);
    }

    /**
     * Returns a street name randomly chosen from the pool of street names.
     * 
     * @param removeWhitespace
     *            whether or not any whitespace is to be removed from the string
     * @return a street name
     */
    public String getStreet(final boolean removeWhitespace)
    {
        return getNextItem(DataCategory.STREETS, removeWhitespace);
    }

    /**
     * Returns a random zip code.
     * 
     * @param length
     *            length of the zip code
     * @return the zip code
     */
    public String getZip(final int length)
    {
        return RandomStringUtils.randomNumeric(length);
    }

    /**
     * Returns a random user name for login purposes. Should be unique in time and space.
     * 
     * @return a unique user name
     */
    public String getUniqueUserName()
    {
        final UUID uuid = UUID.randomUUID();
        return StringUtils.substring("user" + uuid.toString(), 0, 20);
    }

    /**
     * Returns a random US phone number in the format "1-xxx-yyy-zzzz".
     * 
     * @return a random US number
     */
    public String getUSPhoneNumber()
    {
        final int prefix = XltRandom.nextInt(200, 999);
        final int infix = XltRandom.nextInt(200, 999);
        final String suffix = RandomStringUtils.randomNumeric(4);

        return "1-" + prefix + "-" + infix + "-" + suffix;
    }

    /**
     * Returns a random DE phone number in the format "0xxx-yyyyyy".
     * 
     * @return a random DE number
     */
    public String getDEPhoneNumber()
    {
        final String prefix = RandomStringUtils.randomNumeric(3);
        final String suffix = RandomStringUtils.randomNumeric(6);

        return "0" + prefix + "-" + suffix;
    }

    /**
     * Returns the one and only GeneralDataProvider instance.
     * 
     * @return the GeneralDataProvider singleton
     */
    public static GeneralDataProvider getInstance()
    {
        return SingletonHolder.INSTANCE;
    }

    private enum DataCategory
    {
        COMPANIES, COUNTRIES, FIRSTNAMES, LASTNAMES, EMAILS, SENTENCES, STREETS, TOWNS;

        private String fileName = null;

        DataCategory(final String fileName)
        {
            this.fileName = fileName;
        }

        DataCategory()
        {
            this(null);
        }

        String getFileName()
        {
            if (fileName != null)
            {
                return fileName;
            }
            else
            {
                return DataProvider.DEFAULT + File.separator + name().toLowerCase() + ".txt";
            }
        }
    }

    /**
     * Internal class that implements the singleton.
     */
    private static class SingletonHolder
    {
        private static final GeneralDataProvider INSTANCE = new GeneralDataProvider();
    }
}
