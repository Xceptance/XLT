package it.unimi.dsi.util;

/*
 * DSI utilities
 *
 * Copyright (C) 2013-2020 Sebastiano Vigna
 *
 * This program and the accompanying materials are made available under the
 * terms of the GNU Lesser General Public License v2.1 or later,
 * which is available at
 * http://www.gnu.org/licenses/old-licenses/lgpl-2.1-standalone.html,
 * or the Apache Software License 2.0, which is available at
 * https://www.apache.org/licenses/LICENSE-2.0.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * SPDX-License-Identifier: LGPL-2.1-or-later OR Apache-2.0
 */

import java.security.SecureRandom;
import java.util.List;
import java.util.Random;

/** A fast, all-purpose, rock-solid {@linkplain Random pseudorandom number generator}. It has excellent speed, a state space (256 bits) that is large enough for
 * any parallel application, and it passes all tests we are aware of.
 * In Java, it is slightly faster than a XoShiRo256StarStarRandom.
 * More information can be found at our <a href="http://prng.di.unimi.it/">PRNG page</a>.
 *
 * <p>If you need to generate just floating-point numbers, XoShiRo256PlusRandom is slightly faster. If you are tight on space,
 * you might try XoRoShiRo128PlusPlusRandom.
 *
 * <p>Note that this is not a {@linkplain SecureRandom secure generator}.
 *
 * IMPORTANT: This class has been extended for the purpose to be used here. It also has been stripped of methods
 * not needed to get rid off dependencies. Full credit to http://prng.di.unimi.it/ and the original name is XoShiRo256PlusPlus.
 *
 * @version 2.0 by Rene Schwietzke, Xceptance
 * @see it.unimi.dsi.util
 */
public class FastRandom
{
    /** The internal state of the algorithm. */
    private long s0, s1, s2, s3;

    protected FastRandom(final long s0, final long s1, final long s2, final long s3)
    {
        this.s0 = s0;
        this.s1 = s1;
        this.s2 = s2;
        this.s3 = s3;
    }

    /** Creates a new generator using a given seed.
     *
     * @param seed a seed for the generator.
     */
    public FastRandom(final long seed)
    {
        setSeed(seed);
    }

    public long nextLong()
    {
        final long t0 = s0;
        final long result = Long.rotateLeft(t0 + s3, 23) + t0;

        final long t = s1 << 17;

        s2 ^= t0;
        s3 ^= s1;
        s1 ^= s2;
        s0 ^= s3;

        s2 ^= t;

        s3 = Long.rotateLeft(s3, 45);

        return result;
    }

    public int nextInt()
    {
        return (int)nextLong();
    }

    public int nextInt(final int n)
    {
        return (int)nextLong(n);
    }

    /**
     * From and to, inclusive of the range
     *
     * @return
     */
    public int nextInt(int from, int to)
    {
        return from + nextInt(to - from + 1);
    }

    /** Returns a pseudorandom uniformly distributed {@code long} value
     * between 0 (inclusive) and the specified value (exclusive), drawn from
     * this random number generator's sequence. The algorithm used to generate
     * the value guarantees that the result is uniform, provided that the
     * sequence of 64-bit values produced by this generator is.
     *
     * @param n the positive bound on the random number to be returned.
     * @return the next pseudorandom {@code long} value between {@code 0} (inclusive) and {@code n} (exclusive).
     */
    public long nextLong(final long n)
    {
        if (n <= 0) throw new IllegalArgumentException("illegal bound " + n + " (must be positive)");
        long t = nextLong();
        final long nMinus1 = n - 1;
        // Rejection-based algorithm to get uniform integers in the general case
        for (long u = t >>> 1; u + nMinus1 - (t = u % n) < 0; u = nextLong() >>> 1);
        return t;
    }

    public double nextDouble()
    {
        return (nextLong() >>> 11) * 0x1.0p-53;
    }

    /**
     * Returns the next pseudorandom, uniformly distributed
     * {@code double} value between {@code 0.0} and
     * {@code 1.0} from this random number generator's sequence,
     * using a fast multiplication-free method which, however,
     * can provide only 52 significant bits.
     *
     * <p>This method is faster than {@link #nextDouble()}, but it
     * can return only dyadic rationals of the form <var>k</var> / 2<sup>&minus;52</sup>,
     * instead of the standard <var>k</var> / 2<sup>&minus;53</sup>. Before
     * version 2.4.1, this was actually the standard implementation of
     * {@link #nextDouble()}, so you can use this method if you need to
     * reproduce exactly results obtained using previous versions.
     *
     * <p>The only difference between the output of this method and that of
     * {@link #nextDouble()} is an additional least significant bit set in half of the
     * returned values. For most applications, this difference is negligible.
     *
     * @return the next pseudorandom, uniformly distributed {@code double}
     * value between {@code 0.0} and {@code 1.0} from this
     * random number generator's sequence, using 52 significant bits only.
     */
    public double nextDoubleFast()
    {
        return Double.longBitsToDouble(0x3FFL << 52 | nextLong() >>> 12) - 1.0;
    }

    public float nextFloat()
    {
        return (nextLong() >>> 40) * 0x1.0p-24f;
    }

    public boolean nextBoolean()
    {
        return nextLong() < 0;
    }

    public void nextBytes(final byte[] bytes)
    {
        int i = bytes.length, n = 0;
        while(i != 0) {
            n = Math.min(i, 8);
            for (long bits = nextLong(); n-- != 0; bits >>= 8) bytes[--i] = (byte)bits;
        }
    }

    /** Sets the seed of this generator.
     *
     * <p>The argument will be used to seed a {@link SplitMix64RandomGenerator}, whose output
     * will in turn be used to seed this generator. This approach makes &ldquo;warmup&rdquo; unnecessary,
     * and makes the probability of starting from a state
     * with a large fraction of bits set to zero astronomically small.
     *
     * @param seed a seed for this generator.
     */
    public void setSeed(final long seed)
    {
        final SplitMix64RandomGenerator r = new SplitMix64RandomGenerator(seed);
        s0 = r.nextLong();
        s1 = r.nextLong();
        s2 = r.nextLong();
        s3 = r.nextLong();
    }

    /**
     * Returns a new FastRandom from various inputs
     */
    public static FastRandom get(final long seed, final long... additionalSeeds)
    {
        long l = SplitMix64RandomGenerator.murmurHash3(seed);
        for (int i = 0; i < additionalSeeds.length; i++)
        {
            // we need the 31 multiplication to make the order of things matter
            l = 31 * l + SplitMix64RandomGenerator.murmurHash3(additionalSeeds[i]);
        }

        return new FastRandom(l);
    }

    /**
     * Returns a new FastRandom from various inputs
     */
    public static FastRandom get(final int seed, final int... additionalSeeds)
    {
        final long[] longArray = new long[additionalSeeds.length];
        for (int i = 0; i < additionalSeeds.length; i++)
        {
            longArray[i] = int2significantLong(additionalSeeds[i]);
        }

        return get(int2significantLong(seed), longArray);
    }

    /**
     * Extends an integer to a long by using all 64 bit instead of the initial 32 bit
     */
    public static long int2significantLong(final int i)
    {
        // parentheses are important and the cast too
        final long r = ((long)i << 32) + i;
        return r;
    }

    public final static String LOWERCHARS = "abcdefghijklmnopqrstuvwxyz";
    public final static String UPPERCHARS = LOWERCHARS.toUpperCase();
    public final static String CHARS = LOWERCHARS + UPPERCHARS;
    public final static String NUMBERS = "0123456789";
    public final static String ALPHANUMERIC_ALL = CHARS + "0123456789";
    public final static String ALPHANUMERIC_LOWER = LOWERCHARS + "0123456789";
    public final static String ALPHANUMERIC_UPPER = UPPERCHARS + "0123456789";

    /**
     * Fixed length random string
     *
     * @param source the data to use to get chars from
     * @param length the desired length
     * @return
     */
    public String randomString(final String source, final int length)
    {
        return randomString(source, length, length);
    }

    /**
     * Variable length random string
     *
     * @param source
     * @param from
     * @param to
     * @return
     */
    public String randomString(final String source, final int from, final int to)
    {
        final int length = nextInt(to - from + 1) + from;

        final StringBuilder sb = new StringBuilder(to);

        for (int i = 0; i < length; i++)
        {
            final int pos = nextInt(source.length());
            sb.append(source.charAt(pos));
        }

        return sb.toString();
    }

    /**
     * Gets a random value from the list with equal weight on the full list using XltRandom
     *
     * @param list
     *            the list to get entries from
     *
     * @return a list entry or null if the list is empty, compare this to LookUpResult.random()
     */
    public <T> T randomEntry(final List<T> list)
    {
        if (list.isEmpty() == false)
        {
            return list.get(nextInt(list.size()));
        }
        else
        {
            return null;
        }
    }

}
