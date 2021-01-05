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
package com.xceptance.xlt.util;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.apache.commons.io.output.TeeOutputStream;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ProgressBarTest
{
    private PrintStream originalOut;

    private ByteArrayOutputStream out;

    @Before
    public void setStream()
    {
        originalOut = System.out;
        out = new ByteArrayOutputStream();
        System.setOut(new PrintStream(new TeeOutputStream(System.out, out)));
    }

    @After
    public void resetStream()
    {
        System.setOut(originalOut);
    }

    @Test
    public final void testProgressBar_5()
    {
        final ProgressBar p = new ProgressBar(5);

        Assert.assertEquals(0, p.getCurrentCount());
        Assert.assertEquals(5, p.getTotal());

        String currentOut = "";
        Assert.assertEquals(currentOut, out.toString());

        p.start();
        currentOut = currentOut + "0% ... ";
        Assert.assertEquals(currentOut, out.toString());
        Assert.assertEquals(0, p.getCurrentCount());

        p.increaseCount();
        currentOut = currentOut + "20% ... ";
        Assert.assertEquals(currentOut, out.toString());
        Assert.assertEquals(1, p.getCurrentCount());

        p.increaseCount();
        currentOut = currentOut + "40% ... ";
        Assert.assertEquals(currentOut, out.toString());
        Assert.assertEquals(2, p.getCurrentCount());

        p.increaseCount();
        currentOut = currentOut + "60% ... ";
        Assert.assertEquals(currentOut, out.toString());
        Assert.assertEquals(3, p.getCurrentCount());

        p.increaseCount();
        currentOut = currentOut + "80% ... ";
        Assert.assertEquals(currentOut, out.toString());
        Assert.assertEquals(4, p.getCurrentCount());

        p.increaseCount();
        currentOut = currentOut + "100% ";
        Assert.assertEquals(currentOut, out.toString());
        Assert.assertEquals(5, p.getCurrentCount());
    }

    @Test
    public final void testProgressBar_1()
    {
        final ProgressBar p = new ProgressBar(1);

        Assert.assertEquals(0, p.getCurrentCount());

        String currentOut = "";
        Assert.assertEquals(currentOut, out.toString());

        p.start();
        currentOut = currentOut + "0% ... ";
        Assert.assertEquals(currentOut, out.toString());
        Assert.assertEquals(0, p.getCurrentCount());

        p.increaseCount();
        currentOut = currentOut + "100% ";
        Assert.assertEquals(currentOut, out.toString());
        Assert.assertEquals(1, p.getCurrentCount());
    }

    @Test
    public final void testProgressBar_2()
    {
        final ProgressBar p = new ProgressBar(2);

        Assert.assertEquals(0, p.getCurrentCount());

        String currentOut = "";
        Assert.assertEquals(currentOut, out.toString());

        p.start();
        currentOut = currentOut + "0% ... ";
        Assert.assertEquals(currentOut, out.toString());
        Assert.assertEquals(0, p.getCurrentCount());

        p.increaseCount();
        currentOut = currentOut + "50% ... ";
        Assert.assertEquals(currentOut, out.toString());
        Assert.assertEquals(1, p.getCurrentCount());

        p.increaseCount();
        currentOut = currentOut + "100% ";
        Assert.assertEquals(currentOut, out.toString());
        Assert.assertEquals(2, p.getCurrentCount());
    }

    @Test
    public final void testProgressBar_3()
    {
        final ProgressBar p = new ProgressBar(3);

        Assert.assertEquals(0, p.getCurrentCount());

        String currentOut = "";
        Assert.assertEquals(currentOut, out.toString());

        p.start();
        currentOut = currentOut + "0% ... ";
        Assert.assertEquals(currentOut, out.toString());
        Assert.assertEquals(0, p.getCurrentCount());

        p.increaseCount();
        currentOut = currentOut + "30% ... ";
        Assert.assertEquals(currentOut, out.toString());
        Assert.assertEquals(1, p.getCurrentCount());

        p.increaseCount();
        currentOut = currentOut + "60% ... ";
        Assert.assertEquals(currentOut, out.toString());
        Assert.assertEquals(2, p.getCurrentCount());

        p.increaseCount();
        currentOut = currentOut + "100% ";
        Assert.assertEquals(currentOut, out.toString());
        Assert.assertEquals(3, p.getCurrentCount());
    }

    @Test
    public final void testProgressBar_10()
    {
        final ProgressBar p = new ProgressBar(10);

        Assert.assertEquals(0, p.getCurrentCount());

        String currentOut = "";
        Assert.assertEquals(currentOut, out.toString());

        p.start();
        currentOut = currentOut + "0% ... ";
        Assert.assertEquals(currentOut, out.toString());
        Assert.assertEquals(0, p.getCurrentCount());

        p.increaseCount();
        currentOut = currentOut + "10% ... ";
        Assert.assertEquals(currentOut, out.toString());
        Assert.assertEquals(1, p.getCurrentCount());

        p.increaseCount();
        currentOut = currentOut + "20% ... ";
        Assert.assertEquals(currentOut, out.toString());
        Assert.assertEquals(2, p.getCurrentCount());

        p.increaseCount();
        currentOut = currentOut + "30% ... ";
        Assert.assertEquals(currentOut, out.toString());
        Assert.assertEquals(3, p.getCurrentCount());

        p.increaseCount();
        currentOut = currentOut + "40% ... ";
        Assert.assertEquals(currentOut, out.toString());
        Assert.assertEquals(4, p.getCurrentCount());

        p.increaseCount();
        p.increaseCount();
        p.increaseCount();
        p.increaseCount();
        p.increaseCount();
        p.increaseCount();
        currentOut = currentOut + "50% ... 60% ... 70% ... 80% ... 90% ... 100% ";
        Assert.assertEquals(currentOut, out.toString());
        Assert.assertEquals(10, p.getCurrentCount());
    }

    @Test
    public final void testProgressBar_20()
    {
        String currentOut = "";

        final ProgressBar p = new ProgressBar(20);
        p.start();

        for (int i = 0; i < 20; i++)
        {
            p.increaseCount();
        }

        currentOut = currentOut + "0% ... 10% ... 20% ... 30% ... 40% ... 50% ... 60% ... 70% ... 80% ... 90% ... 100% ";
        Assert.assertEquals(currentOut, out.toString());
        Assert.assertEquals(20, p.getCurrentCount());
    }

    @Test(expected = IllegalArgumentException.class)
    public final void testProgressBar_Exception()
    {
        new ProgressBar(-1);
    }

    @Test(expected = IllegalArgumentException.class)
    public final void testOverRunning()
    {
        final ProgressBar p = new ProgressBar(3);
        Assert.assertEquals(0, p.getCurrentCount());

        p.start();
        Assert.assertEquals(0, p.getCurrentCount());

        p.increaseCount();
        Assert.assertEquals(1, p.getCurrentCount());

        p.increaseCount();
        Assert.assertEquals(2, p.getCurrentCount());

        p.increaseCount();
        Assert.assertEquals(3, p.getCurrentCount());

        p.increaseCount();
    }

    @Test
    public final void testIncreaseCountInt()
    {
        final ProgressBar p = new ProgressBar(4);
        Assert.assertEquals(0, p.getCurrentCount());

        p.start();
        Assert.assertEquals(0, p.getCurrentCount());

        p.increaseCount(2);
        Assert.assertEquals("0% ... 50% ... ", out.toString());
        Assert.assertEquals(2, p.getCurrentCount());

        p.increaseCount(1);
        Assert.assertEquals("0% ... 50% ... 70% ... ", out.toString());
        Assert.assertEquals(3, p.getCurrentCount());
    }

    @Test(expected = IllegalArgumentException.class)
    public final void testIncreaseCountInt_Negative()
    {
        final ProgressBar p = new ProgressBar(4);

        p.start();
        p.increaseCount(-2);
    }

    @Test
    public final void testSetCompleted()
    {
        final ProgressBar p = new ProgressBar(3);

        p.start();
        p.setCompleted();

        Assert.assertEquals("0% ... 100% ", out.toString());
        Assert.assertEquals(3, p.getCurrentCount());
    }

    @Test
    public final void testSetPercent()
    {
        final ProgressBar p = new ProgressBar(3);

        p.start();

        p.setPercent(25);
        Assert.assertEquals("0% ... 20% ... ", out.toString());

        p.setPercent(77);
        Assert.assertEquals("0% ... 20% ... 70% ... ", out.toString());

        p.setPercent(100);
        Assert.assertEquals("0% ... 20% ... 70% ... 100% ", out.toString());
    }

    @Test(expected = IllegalArgumentException.class)
    public final void testSetPercent_ExceptionLower()
    {
        final ProgressBar p = new ProgressBar(3);

        p.start();
        p.setPercent(-25);
    }

    @Test(expected = IllegalArgumentException.class)
    public final void testSetPercent_ExceptionUpper()
    {
        final ProgressBar p = new ProgressBar(3);

        p.start();
        p.setPercent(101);
    }

}
