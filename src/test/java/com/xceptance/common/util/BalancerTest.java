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
package com.xceptance.common.util;

import org.junit.Test;

import com.xceptance.xlt.AbstractXLTTestCase;

/**
 * Tests the implementation of {@link Balancer}.
 * 
 * @author Hartmut Arlt (Xceptance Software Technologies GmbH)
 */
public class BalancerTest extends AbstractXLTTestCase
{
    /**
     * Tests the implementation of {@link Balancer#check(String)} by passing an unbalanced string that misses one or
     * more closing delimiters.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testCheck_UnbalancedMissingClosing()
    {
        final Balancer instance = new Balancer("++", "-+-");
        instance.check("All++work and-+-no++play++makes-+-++joe-+-a dull boy-+-++");
    }

    // /**
    // * Tests the implementation of {@link Balancer#check(String)} by passing an
    // * unbalanced string that misses one or more opening delimiters.
    // */
    // @Test(expected = IllegalArgumentException.class)
    // public void testCheck_UnbalancedMissingOpening()
    // {
    // instance.check("Howdy,-+-partner, ++What's going on-+-?");
    // }

    /**
     * Tests the implementation of {@link Balancer#check(String)} by passing a balanced string.
     */
    @Test
    public void testCheck_Balanced()
    {
        final Balancer instance = new Balancer("++", "-+-");
        instance.check("To++be++or not++ to -+- be, " + "that ++ is -+- here-+- the question.-+-");
    }

    /**
     * Testing our standard pattern for properties ${}
     */
    @Test
    public void testCheck_PropertyExampleValid()
    {
        final Balancer balancer = new Balancer("${", "}");
        balancer.check("That is a ${Test}.");
        balancer.check("That is a ${Test} with more data ${Test}.");
    }

    /**
     * Testing our standard pattern for properties. Some errors: Just start tag.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testCheck_PropertyExampleInvalid_JustStart()
    {
        final Balancer balancer = new Balancer("${", "}");
        balancer.check("That is a ${Test.");
    }

    /**
     * Testing our standard pattern for properties. Some errors: Two start tags.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testCheck_PropertyExampleInvalid_TwoStart()
    {
        final Balancer balancer = new Balancer("${", "}");
        balancer.check("That is a ${${Test}.");
    }

    // /**
    // * Testing our standard pattern for properties.
    // * Some errors: Two end tags.
    // */
    // @Test(expected = IllegalArgumentException.class)
    // public void testCheck_PropertyExampleInvalid_TwoEnd()
    // {
    // final Balancer balancer = new Balancer("${", "}");
    // balancer.check("That is a Test}}.");
    // }

    /**
     * Testing our standard pattern for properties. Some errors: Delimiters only
     */
    @Test
    public void testCheck_PropertyExampleValid_DelimitersOnly()
    {
        final Balancer balancer = new Balancer("${", "}");
        balancer.check("${}");
    }

    /**
     * Testing our standard pattern for properties. Some errors: Wrong order.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testCheck_PropertyExampleValid_WrongOrder()
    {
        final Balancer balancer = new Balancer("${", "}");
        balancer.check("}${");
    }

    /**
     * Testing our standard pattern for properties. Some errors: Pattern in pattern
     */
    @Test
    public void testCheck_PropertyExampleValid_PatternInPattern()
    {
        final Balancer balancer = new Balancer("${", "}");
        balancer.check("This is a ${Test with ${a} Test} with a Test.");
    }

    /**
     * Testing our standard pattern for properties. Some errors: Pattern in pattern
     */
    @Test
    public void testCheck_EmptyString()
    {
        final Balancer balancer = new Balancer("{", "}");
        balancer.check(null);
        balancer.check("");
    }

    /**
     * Tests the implementation of {@link Balancer#Balancer(String, String)} by passing the null reference as opening
     * delimiter.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_OpeningDelimiterNull()
    {
        new Balancer(null, ")");
    }

    /**
     * Tests the implementation of {@link Balancer#Balancer(String, String)} by passing an empty string as opening
     * delimiter.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_OpeningDelimiterInvalid()
    {
        new Balancer("", ")");
    }

    /**
     * Tests the implementation of {@link Balancer#Balancer(String, String)} by passing the null reference as closing
     * delimiter.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_ClosingDelimiterNull()
    {
        new Balancer("(", null);
    }

    /**
     * Tests the implementation of {@link Balancer#Balancer(String, String)} by passing an empty string as closing
     * delimiter.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_ClosingDelimiterInvalid()
    {
        new Balancer("(", "");
    }
}
