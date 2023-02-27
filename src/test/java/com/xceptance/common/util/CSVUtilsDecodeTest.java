package com.xceptance.common.util;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.xceptance.xlt.api.util.XltCharBuffer;

public class CSVUtilsDecodeTest
{
    private void test_noQuoteConversion(String s, String... expected) 
    {
        final List<XltCharBuffer> result = CsvUtilsDecode.parse(s);
        
        Assert.assertEquals(expected.length, result.size());
        for (int i = 0; i < expected.length; i++)
        {
            Assert.assertEquals(expected[i], result.get(i).toString());
        }
    }

    private void test(String s, String... expected) 
    {
        final String _s = s.replace("'", "\"");
        test_noQuoteConversion(_s, expected);
    }

    /**
     * All test cases use ' for definition but will run them with ", just
     * to aid the visuals here
     */
    
    @Test
    public void empty() 
    {
        test("", "");
        test(" ", " ");
    }

    @Test
    public void col1() 
    {
        test("a", "a");
        test("ab", "ab");
        test("abc", "abc");
        test("abc def", "abc def");
        test("foobar", "foobar");
    }

    @Test
    public void col1Quoted() 
    {
        test("''", "");
        test("' '", " ");
        test("'a'", "a");
        test("'ab'", "ab");
        test("'abcd'", "abcd");
    }
    @Test
    public void col1Quoted_long() 
    {
        test("\"foobar\"", "foobar");
    }

    @Test
    public void emptyStart() 
    {
        test(",a", "", "a");
        test(",a,b", "", "a", "b");
    }

    @Test
    public void emptyEnd() 
    {
        test("a,", "a", "");
        test("'a',", "a", "");
        test("a,b,", "a", "b", "");
        test("aaa,bbb,", "aaa", "bbb", "");
    }
    
    @Test
    public void happy() 
    {
        test("a,foo,bar,123,1232,7,true", "a", "foo", "bar", "123", "1232", "7", "true");
    }

    @Test
    public void happyAndEmpty() 
    {
        test("a,foo,bar,123,1232,,,7,true,,", "a", "foo", "bar", "123", "1232", "", "", "7", "true", "", "");
    }

    @Test
    public void col2() 
    {
        test("a,b", "a", "b");
        test("aa,bb", "aa", "bb");
    }
    @Test
    public void col2Quoted() 
    {
        test("'a','b'", "a", "b");
        test("'a',''", "a", "");
        test("'aa','bb'", "aa", "bb");
    }

    @Test
    public void quotedQuotes() 
    {
        test("'a''a'", "a\"a");
//        test("a''a", "a\"a"); // invalid
        test("''", "");
        test("''''", "\"");
    }

    @Test
    public void simpleQuotes() 
    {
        test_noQuoteConversion("'a'", "'a'");
        test_noQuoteConversion("'a',\"foobar 'quotes' s\"", "'a'", "foobar 'quotes' s");
    }
    
    @Test
    public void emptyCols() 
    {
        test("a,,,b,", "a", "", "", "b", "");
        test("a,,b", "a", "", "b");
        test("a,,,b", "a", "", "", "b");
        test(",", "", "");
    }

    @Test
    public void parseLongLine()
    {
        test_noQuoteConversion("T,TBrowse,1571766200603,12786,true,\"java.lang.AssertionError: Response code does not match expected:<200> but was:<410> (user: 'TBrowse-165', output: '1571766200603')\\   at org.junit.Assert.fail(Assert.java:88)\\   at org.junit.Assert.failNotEquals(Assert.java:834)\\ at org.junit.Assert.assertEquals(Assert.java:645)\\  at com.xceptance.xlt.api.validators.HttpResponseCodeValidator.validate(HttpResponseCodeValidator.java:51)\\  at com.xceptance.xlt.api.validators.StandardValidator.validate(StandardValidator.java:28)\\  at com.xceptance.xlt.loadtest.validators.Validator.validateBasics(Validator.java:79)\\   at com.xceptance.xlt.loadtest.validators.Validator.validateCommonPage(Validator.java:40)\\   at com.xceptance.xlt.loadtest.validators.Validator.validateCategoryPage(Validator.java:276)\\    at com.xceptance.xlt.loadtest.actions.catalog.RefineByCategory.postValidate(RefineByCategory.java:86)\\  at com.xceptance.xlt.api.actions.AbstractAction.run(AbstractAction.java:383)\\   at com.xceptance.xlt.api.actions.AbstractWebAction.run(AbstractWebAction.java:136)\\ at com.xceptance.xlt.api.actions.AbstractHtmlPageAction.run(AbstractHtmlPageAction.java:124)\\   at com.xceptance.xlt.loadtest.actions.AbstractHtmlPageAction.runIfPossible(AbstractHtmlPageAction.java:297)\\    at com.xceptance.xlt.loadtest.flows.CategoryFlow.refineCategory(CategoryFlow.java:85)\\  at com.xceptance.xlt.loadtest.flows.CategoryFlow.run(CategoryFlow.java:43)\\ at com.xceptance.xlt.loadtest.tests.TBrowse.test(TBrowse.java:31)\\  at com.xceptance.xlt.loadtest.tests.AbstractTestCase.run(AbstractTestCase.java:59)\\ ...\",RefineByCategory",
             "T", "TBrowse", "1571766200603", "12786", "true", 
             "java.lang.AssertionError: Response code does not match expected:<200> but was:<410> (user: 'TBrowse-165', output: '1571766200603')\\   at org.junit.Assert.fail(Assert.java:88)\\   at org.junit.Assert.failNotEquals(Assert.java:834)\\ at org.junit.Assert.assertEquals(Assert.java:645)\\  at com.xceptance.xlt.api.validators.HttpResponseCodeValidator.validate(HttpResponseCodeValidator.java:51)\\  at com.xceptance.xlt.api.validators.StandardValidator.validate(StandardValidator.java:28)\\  at com.xceptance.xlt.loadtest.validators.Validator.validateBasics(Validator.java:79)\\   at com.xceptance.xlt.loadtest.validators.Validator.validateCommonPage(Validator.java:40)\\   at com.xceptance.xlt.loadtest.validators.Validator.validateCategoryPage(Validator.java:276)\\    at com.xceptance.xlt.loadtest.actions.catalog.RefineByCategory.postValidate(RefineByCategory.java:86)\\  at com.xceptance.xlt.api.actions.AbstractAction.run(AbstractAction.java:383)\\   at com.xceptance.xlt.api.actions.AbstractWebAction.run(AbstractWebAction.java:136)\\ at com.xceptance.xlt.api.actions.AbstractHtmlPageAction.run(AbstractHtmlPageAction.java:124)\\   at com.xceptance.xlt.loadtest.actions.AbstractHtmlPageAction.runIfPossible(AbstractHtmlPageAction.java:297)\\    at com.xceptance.xlt.loadtest.flows.CategoryFlow.refineCategory(CategoryFlow.java:85)\\  at com.xceptance.xlt.loadtest.flows.CategoryFlow.run(CategoryFlow.java:43)\\ at com.xceptance.xlt.loadtest.tests.TBrowse.test(TBrowse.java:31)\\  at com.xceptance.xlt.loadtest.tests.AbstractTestCase.run(AbstractTestCase.java:59)\\ ...", 
             "RefineByCategory");
    }
}
