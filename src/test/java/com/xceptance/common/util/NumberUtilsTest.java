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
package com.xceptance.common.util;

import org.junit.Assert;
import org.junit.Test;

import java.math.BigDecimal;
import java.math.BigInteger;

public class NumberUtilsTest
{
    @Test
    public void negateNumber_byte()
    {
        Assert.assertEquals(0, NumberUtils.negateNumber((byte) 0).byteValue());
        Assert.assertEquals(0, NumberUtils.negateNumber((byte) -0).byteValue());

        Assert.assertEquals(-1, NumberUtils.negateNumber((byte) 1).byteValue());
        Assert.assertEquals(1, NumberUtils.negateNumber((byte) -1).byteValue());

        Assert.assertEquals(-123, NumberUtils.negateNumber((byte) 123).byteValue());
        Assert.assertEquals(123, NumberUtils.negateNumber((byte) -123).byteValue());

        Assert.assertEquals(-Byte.MAX_VALUE, NumberUtils.negateNumber(Byte.MAX_VALUE).byteValue());
        Assert.assertEquals(Byte.MAX_VALUE, NumberUtils.negateNumber(-Byte.MAX_VALUE).byteValue());
    }

    @Test
    public void negateNumber_short()
    {
        Assert.assertEquals(0, NumberUtils.negateNumber((short) 0).shortValue());
        Assert.assertEquals(0, NumberUtils.negateNumber((short) -0).shortValue());

        Assert.assertEquals(-1, NumberUtils.negateNumber((short) 1).shortValue());
        Assert.assertEquals(1, NumberUtils.negateNumber((short) -1).shortValue());

        Assert.assertEquals(-123, NumberUtils.negateNumber((short) 123).shortValue());
        Assert.assertEquals(123, NumberUtils.negateNumber((short) -123).shortValue());

        Assert.assertEquals(-Short.MAX_VALUE, NumberUtils.negateNumber(Short.MAX_VALUE).shortValue());
        Assert.assertEquals(Short.MAX_VALUE, NumberUtils.negateNumber(-Short.MAX_VALUE).shortValue());
    }

    @Test
    public void negateNumber_int()
    {
        Assert.assertEquals(0, NumberUtils.negateNumber(0).intValue());
        Assert.assertEquals(0, NumberUtils.negateNumber(-0).intValue());

        Assert.assertEquals(-1, NumberUtils.negateNumber(1).intValue());
        Assert.assertEquals(1, NumberUtils.negateNumber(-1).intValue());

        Assert.assertEquals(-123, NumberUtils.negateNumber(123).intValue());
        Assert.assertEquals(123, NumberUtils.negateNumber(-123).intValue());

        Assert.assertEquals(-Integer.MAX_VALUE, NumberUtils.negateNumber(Integer.MAX_VALUE).intValue());
        Assert.assertEquals(Integer.MAX_VALUE, NumberUtils.negateNumber(-Integer.MAX_VALUE).intValue());
    }

    @Test
    public void negateNumber_long()
    {
        Assert.assertEquals(0L, NumberUtils.negateNumber(0L).longValue());
        Assert.assertEquals(0L, NumberUtils.negateNumber(-0L).longValue());

        Assert.assertEquals(-1L, NumberUtils.negateNumber(1L).longValue());
        Assert.assertEquals(1L, NumberUtils.negateNumber(-1L).longValue());

        Assert.assertEquals(-123L, NumberUtils.negateNumber(123L).longValue());
        Assert.assertEquals(123L, NumberUtils.negateNumber(-123L).longValue());

        Assert.assertEquals(-Long.MAX_VALUE, NumberUtils.negateNumber(Long.MAX_VALUE).longValue());
        Assert.assertEquals(Long.MAX_VALUE, NumberUtils.negateNumber(-Long.MAX_VALUE).longValue());
    }

    @Test
    public void negateNumber_float()
    {
        Assert.assertEquals(-0.0f, NumberUtils.negateNumber(0.0f).floatValue(), 0.0f);
        Assert.assertEquals(0.0f, NumberUtils.negateNumber(-0.0f).floatValue(), 0.0f);

        Assert.assertEquals(-1.2f, NumberUtils.negateNumber(1.2f).floatValue(), 0.0f);
        Assert.assertEquals(1.2f, NumberUtils.negateNumber(-1.2f).floatValue(), 0.0f);

        Assert.assertEquals(-123.456789f, NumberUtils.negateNumber(123.456789f).floatValue(), 0.0f);
        Assert.assertEquals(123.456789f, NumberUtils.negateNumber(-123.456789f).floatValue(), 0.0f);

        Assert.assertEquals(-Float.MAX_VALUE, NumberUtils.negateNumber(Float.MAX_VALUE).floatValue(), 0.0f);
        Assert.assertEquals(Float.MAX_VALUE, NumberUtils.negateNumber(-Float.MAX_VALUE).floatValue(), 0.0f);

        Assert.assertEquals(-Float.MIN_VALUE, NumberUtils.negateNumber(Float.MIN_VALUE).floatValue(), 0.0f);
        Assert.assertEquals(Float.MIN_VALUE, NumberUtils.negateNumber(-Float.MIN_VALUE).floatValue(), 0.0f);
    }

    @Test
    public void negateNumber_double()
    {
        Assert.assertEquals(-0.0, NumberUtils.negateNumber(0.0).doubleValue(), 0.0);
        Assert.assertEquals(0.0, NumberUtils.negateNumber(-0.0).doubleValue(), 0.0);

        Assert.assertEquals(-1.2, NumberUtils.negateNumber(1.2).doubleValue(), 0.0);
        Assert.assertEquals(1.2, NumberUtils.negateNumber(-1.2).doubleValue(), 0.0);

        Assert.assertEquals(-123.456789, NumberUtils.negateNumber(123.456789).doubleValue(), 0.0);
        Assert.assertEquals(123.456789, NumberUtils.negateNumber(-123.456789).doubleValue(), 0.0);

        Assert.assertEquals(-Double.MAX_VALUE, NumberUtils.negateNumber(Double.MAX_VALUE).doubleValue(), 0.0);
        Assert.assertEquals(Double.MAX_VALUE, NumberUtils.negateNumber(-Double.MAX_VALUE).doubleValue(), 0.0);

        Assert.assertEquals(-Double.MIN_VALUE, NumberUtils.negateNumber(Double.MIN_VALUE).doubleValue(), 0.0);
        Assert.assertEquals(Double.MIN_VALUE, NumberUtils.negateNumber(-Double.MIN_VALUE).doubleValue(), 0.0);
    }

    @Test
    public void negateNumber_BigInteger()
    {
        Assert.assertEquals(BigInteger.ZERO.negate(), NumberUtils.negateNumber(BigInteger.ZERO));
        Assert.assertEquals(BigInteger.valueOf(-0).negate(), NumberUtils.negateNumber(BigInteger.valueOf(-0)));

        Assert.assertEquals(BigInteger.ONE.negate(), NumberUtils.negateNumber(BigInteger.ONE));
        Assert.assertEquals(BigInteger.valueOf(-1).negate(), NumberUtils.negateNumber(BigInteger.valueOf(-1)));

        Assert.assertEquals(BigInteger.valueOf(123).negate(), NumberUtils.negateNumber(BigInteger.valueOf(123)));
        Assert.assertEquals(BigInteger.valueOf(-123).negate(), NumberUtils.negateNumber(BigInteger.valueOf(-123)));

        Assert.assertEquals(new BigDecimal("123E456").toBigInteger().negate(),
                            NumberUtils.negateNumber(new BigDecimal("123E456").toBigInteger()));
        Assert.assertEquals(new BigDecimal("-123E456").toBigInteger().negate(),
                            NumberUtils.negateNumber(new BigDecimal("-123E456").toBigInteger()));

    }

    @Test
    public void negateNumber_BigDecimal()
    {
        Assert.assertEquals(BigDecimal.ZERO.negate(), NumberUtils.negateNumber(BigDecimal.ZERO));
        Assert.assertEquals(new BigDecimal("-0").negate(), NumberUtils.negateNumber(new BigDecimal("-0")));

        Assert.assertEquals(BigDecimal.ONE.negate(), NumberUtils.negateNumber(BigDecimal.ONE));
        Assert.assertEquals(new BigDecimal("-1").negate(), NumberUtils.negateNumber(new BigDecimal("-1")));

        Assert.assertEquals(new BigDecimal("123.456789").negate(), NumberUtils.negateNumber(new BigDecimal("123.456789")));
        Assert.assertEquals(new BigDecimal("-123.456789").negate(), NumberUtils.negateNumber(new BigDecimal("-123.456789")));

        Assert.assertEquals(new BigDecimal("123.456789E123").negate(), NumberUtils.negateNumber(new BigDecimal("123.456789E123")));
        Assert.assertEquals(new BigDecimal("-123.456789E123").negate(), NumberUtils.negateNumber(new BigDecimal("-123.456789E123")));

        Assert.assertEquals(new BigDecimal("123.456789E-123").negate(), NumberUtils.negateNumber(new BigDecimal("123.456789E-123")));
        Assert.assertEquals(new BigDecimal("-123.456789E-123").negate(), NumberUtils.negateNumber(new BigDecimal("-123.456789E-123")));
    }

    @Test(expected = IllegalArgumentException.class)
    public void negateNumber_minByte()
    {
        NumberUtils.negateNumber(Byte.MIN_VALUE);
    }

    @Test(expected = IllegalArgumentException.class)
    public void negateNumber_minShort()
    {
        NumberUtils.negateNumber(Short.MIN_VALUE);
    }

    @Test(expected = IllegalArgumentException.class)
    public void negateNumber_minInt()
    {
        NumberUtils.negateNumber(Integer.MIN_VALUE);
    }

    @Test(expected = IllegalArgumentException.class)
    public void negateNumber_minLong()
    {
        NumberUtils.negateNumber(Long.MIN_VALUE);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void negateNumber_customNumberClassNotSupported()
    {
        NumberUtils.negateNumber(new CustomNumber());
    }

    private class CustomNumber extends Number
    {

        @Override
        public int intValue()
        {
            return 0;
        }

        @Override
        public long longValue()
        {
            return 0;
        }

        @Override
        public float floatValue()
        {
            return 0;
        }

        @Override
        public double doubleValue()
        {
            return 0;
        }
    }
}
