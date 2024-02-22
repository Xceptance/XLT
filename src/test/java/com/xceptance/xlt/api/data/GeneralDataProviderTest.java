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
package com.xceptance.xlt.api.data;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.xceptance.xlt.AbstractXLTTestCase;
import com.xceptance.xlt.api.util.XltLogger;
import com.xceptance.xlt.api.util.XltProperties;
import com.xceptance.xlt.api.util.XltRandom;

/**
 * Test the implementation of {@link GeneralDataProvider}.
 * 
 * @author Hartmut Arlt (Xceptance Software Technologies GmbH)
 */
public class GeneralDataProviderTest extends AbstractXLTTestCase
{
    /**
     * Character constant representing a dot.
     */
    private static final char DOT = '.';

    private static final char QUESTION_MARK = '?';

    private static final char EXCLAMATION_MARK = '!';

    /**
     * Property name for XLT data directory.
     */
    protected static final String DATA_DIR_PROP = "com.xceptance.xlt.data.directory";

    /**
     * Data provider test instance.
     */
    protected GeneralDataProvider provider = null;

    /**
     * Static test fixture setup.
     * 
     * @throws Exception
     *             thrown when setup failed.
     */
    @BeforeClass
    public static void classIntro()
    {
        // get value of property 'DATA_DIR_PROP'
        final String dataDir = XltProperties.getInstance().getProperty(DATA_DIR_PROP);
        // fail if property is not set correctly
        if (dataDir == null || 0 == dataDir.length())
        {
            Assert.fail("No data directory defined! Please define the " + "property '" + DATA_DIR_PROP + "' in 'test.properties'.");
        }

        XltLogger.runTimeLogger.info("XLT data directory: " + dataDir);
    }

    /**
     * Test fixture setup.
     * 
     * @throws Exception
     *             thrown when setup failed.
     */
    @Before
    public void intro()
    {
        // get and store singleton instance of GeneralDataProvider
        provider = GeneralDataProvider.getInstance();
        Assert.assertNotNull("Data provider instance is null!", provider);
    }

    /**
     * Tests the implementations of the general data getters whose signature is <code>getXXX(boolean)</code>.
     */
    @Test
    public void testGetGeneralData()
    {
        // test general data getters with white space removing set to on
        testXStripped(true);
        // test general data getters with white space removing set to off
        testXStripped(false);

    }

    /**
     * Tests the implementation of {@link GeneralDataProvider#getText(int, boolean)}.
     */
    @Test
    public void testGetText()
    {
        // format of error message
        final String errMsgFormat = "Result of getText(%d,%s) is '%s'";

        // guess a random number between 0 and 10 inclusive
        final int noSentences = XltRandom.nextInt(10) + 1;

        // get sentences (white spaces removed)
        String result = provider.getText(noSentences, true);

        // validate
        Assert.assertNotNull(result);
        Assert.assertTrue(String.format(errMsgFormat, noSentences, true, result), noSentences <= getNoSentenceTerminators(result));

        // get sentences (white spaces retained)
        result = provider.getText(noSentences, false);

        // validate
        Assert.assertNotNull(result);
        Assert.assertTrue(String.format(errMsgFormat, noSentences, false, result), noSentences <= getNoSentenceTerminators(result));

    }

    /**
     * Tests the implementation of {@link GeneralDataProvider#getText(int, int, boolean)}.
     */
    @Test
    public void testGetText_Range()
    {
        // format of error message
        final String errMsgFormat = "Result of getText(%d,%d,%s) is: %s";

        // guess a random number between 0 and 10 inclusive
        final int noSentences = XltRandom.nextInt(11);

        // get sentences (white spaces removed)
        String result = provider.getText(noSentences, noSentences, true);

        // validate
        Assert.assertNotNull(result);
        Assert.assertTrue(String.format(errMsgFormat, noSentences, noSentences, true, result),
                          getNoSentenceTerminators(result) >= noSentences);

        // get sentences (white spaces retained)
        result = provider.getText(noSentences, noSentences, false);

        // validate
        Assert.assertNotNull(result);
        Assert.assertTrue(String.format(errMsgFormat, noSentences, noSentences, true, result),
                          noSentences <= getNoSentenceTerminators(result));

    }

    /**
     * getZip
     */
    @Test
    public void testGetZip()
    {
        Assert.assertEquals("", provider.getZip(0));
        Assert.assertTrue(provider.getZip(1).matches("[0-9]{1}"));
        Assert.assertTrue(provider.getZip(2).matches("[0-9]{2}"));
        Assert.assertTrue(provider.getZip(4).matches("[0-9]{4}"));
        Assert.assertTrue(provider.getZip(5).matches("[0-9]{5}"));
    }

    /**
     * Tests the general data getters using the given white space removal setting.
     * 
     * @param stripped
     *            If set to true, white spaces will be removed.
     */
    private void testXStripped(final boolean stripped)
    {
        Assert.assertNotNull(provider.getCompany(stripped));

        final String street = provider.getStreet(stripped);
        Assert.assertNotNull("Street is null!", street);

        final String town = provider.getTown(stripped);
        Assert.assertTrue("Name of town doesn't start with an uppercase character: " + town, startsWithUppercase(town));

        final String country = provider.getCountry(stripped);
        Assert.assertTrue("Name of country doesn't start with an uppercase character: " + country, startsWithUppercase(country));

        final String firstName = provider.getFirstName(stripped);
        Assert.assertTrue("Firstname doesn't start with an uppercase character: " + firstName, startsWithUppercase(firstName));

        final String lastName = provider.getLastName(stripped);
        Assert.assertNotNull("Lastname is null!", lastName);

        final String dePhoneNo = provider.getDEPhoneNumber();
        Assert.assertTrue(startsWithDigit(dePhoneNo, '0'));

        final String usPhoneNo = provider.getUSPhoneNumber();
        Assert.assertTrue(startsWithDigit(usPhoneNo, '1'));

        Assert.assertNotNull(provider.getEmail());

        Assert.assertNotNull(provider.getPredefinedEmail());

        final String sentence = provider.getSentence(stripped);
        Assert.assertTrue("Sentence doesn't start with an uppercase character: " + sentence, startsWithUppercase(sentence) ||
                                                                                             startsWithDigit(sentence));
    }

    /**
     * Returns the number of sentence terminators contained in the given string.
     * 
     * @param s
     *            String containing the sentences.
     * @return Number of sentence terminators.
     */
    private int getNoSentenceTerminators(final String s)
    {
        // initialize counter
        int noSentenceTerminators = 0;

        // iterate through all characters of given string...
        for (int i = 0; i < s.length(); i++)
        {
            if (isSentenceTerminator(s.charAt(i)))
            {
                noSentenceTerminators++;
            }
        }

        return noSentenceTerminators;
    }

    /**
     * Determines if given string starts with an uppercase character.
     * 
     * @param s
     *            The string to check.
     * @return True iff given string starts with an uppercase character, false otherwise.
     */
    private static boolean startsWithUppercase(final String s)
    {
        if (s == null || s.length() == 0)
        {
            return false;
        }

        return Character.isUpperCase(s.charAt(0));
    }

    /**
     * Determines if given string starts with the given digit.
     * 
     * @param s
     *            The string to check.
     * @param c
     *            The digit the string should start with.
     * @return True iff given string starts with the given digit, false otherwise.
     */
    private static boolean startsWithDigit(final String s, final char c)
    {
        // parameter validation
        if (s == null || s.length() == 0)
        {
            return false;
        }
        // just return if given character is a digit and is equal to the first
        // character of the given string
        return Character.isDigit(c) && s.charAt(0) == c;
    }

    /**
     * Determines if given string starts with a digit.
     * 
     * @param s
     *            String to check.
     * @return True iff given string starts with a digit, false otherwise.
     */
    private static boolean startsWithDigit(final String s)
    {
        // parameter validation
        if (s == null || s.length() == 0)
        {
            return false;
        }
        // simply delegate to Character.class
        return Character.isDigit(s.charAt(0));
    }

    /**
     * Test the unique email generation. Regular data creation.
     * 
     * @see GeneralDataProvider#getUniqueEmail(String, String, int)
     */
    @Test
    public void testGetUniqueEmail_NormalOperation()
    {
        Assert.assertTrue(GeneralDataProvider.getInstance().getUniqueEmail("r", "test.com", 15).matches("^r[^-]{14}@test.com$"));
        Assert.assertTrue(GeneralDataProvider.getInstance().getUniqueEmail("r12-", "test.foo.com", 10)
                                             .matches("^r12-[^-]{6}@test.foo.com$"));
        Assert.assertTrue(GeneralDataProvider.getInstance().getUniqueEmail("", "", 17).matches("^[^-]{17}@$"));
    }

    /**
     * Test the unique email generation. Brute force uniqueness check.
     * 
     * @see GeneralDataProvider#getUniqueEmail(String, String, int)
     */
    @Test
    public void testGetUniqueEmail_Uniqueness()
    {
        final int howOften = new Random().nextInt(7162) + 10101;

        final Set<String> emails = new HashSet<String>(howOften + 1);

        for (int i = 0; i < howOften; i++)
        {
            // half length
            final String email = GeneralDataProvider.getInstance().getUniqueEmail("r", "test.com", 17);
            emails.add(email);

            // full length
            final String emailFull = GeneralDataProvider.getInstance().getUniqueEmail("r", "test.com", 33);
            emails.add(emailFull);
        }

        Assert.assertTrue(2 * howOften == emails.size());
    }

    /**
     * Test the unique email generation simple method.
     * 
     * @see GeneralDataProvider#getUniqueEmail(String)
     */
    @Test
    public void testGetUniqueEmail_ConvenienceMethod()
    {
        Assert.assertTrue(GeneralDataProvider.getInstance().getUniqueEmail("test.com").matches("^x[^-]{19}@test.com$"));
    }

    /**
     * Test the email generation method. No whitespace name is given so there's nothing to do.
     * 
     * @see GeneralDataProvider#getEmail(String, boolean)
     */
    @SuppressWarnings("deprecation")
    @Test
    public void testGetEmail_whitespaceDisabled_1()
    {
        Assert.assertTrue(GeneralDataProvider.getInstance().getEmail("test", true).matches("^test@xlt\\d+\\.com$"));
    }

    /**
     * Test the email generation method. Given name contains whitespace. Whitespace should get removed.
     * 
     * @see GeneralDataProvider#getEmail(String, boolean)
     */
    @SuppressWarnings("deprecation")
    @Test
    public void testGetEmail_whitespaceDisabled_2()
    {
        Assert.assertTrue(GeneralDataProvider.getInstance().getEmail("te st", true).matches("^test@xlt\\d+\\.com$"));
    }

    /**
     * Test the email generation method. Given name contains whitespace. Whitespace should get removed even if the
     * second parameter allows whitespaces.
     * 
     * @see GeneralDataProvider#getEmail(String, boolean)
     */
    @SuppressWarnings("deprecation")
    @Test
    public void testGetEmail_whitespaceEnabled()
    {
        Assert.assertTrue(GeneralDataProvider.getInstance().getEmail("te st", false).matches("^test@xlt\\d+\\.com$"));
    }

    // /**
    // * Test the unique email generation. Brute force uniqueness check.
    // *
    // * @see GeneralDataProvider#getTrulyUniqueEmail(String, String, int)
    // */
    // @Test
    // public void speed()
    // {
    // final int howOften = 171761;
    //
    // GeneralDataProvider.getInstance().getTrulyUniqueEmail("r", "test.com", 17);
    //
    // final long start = System.currentTimeMillis();
    // for (int i = 0; i < howOften; i++)
    // {
    // // half length
    // final String email = GeneralDataProvider.getInstance().getTrulyUniqueEmail("r", "test.com", 17);
    // }
    // final long end = System.currentTimeMillis();
    //
    // System.out.println("Run: " + (end -start) + "ms");
    // }

    private static boolean isSentenceTerminator(final char c)
    {
        switch (c)
        {
            case DOT:
            case QUESTION_MARK:
            case EXCLAMATION_MARK:
                return true;
            default:
                return false;
        }
    }
}
