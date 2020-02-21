package com.xceptance.xlt.api.util;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.xceptance.xlt.common.XltConstants;
import com.xceptance.xlt.engine.SessionImpl;
import com.xceptance.xlt.util.XltPropertiesImpl;

import gnu.trove.set.hash.TLongHashSet;

/**
 * https://lab.xceptance.de/issues/2900
 */
public class XltRandomSeedCollisionTest
{
    @BeforeClass
    public static void beforeClass()
    {
        XltProperties.getInstance().setProperty(XltConstants.RANDOM_INIT_VALUE_PROPERTY, "42");
    }

    @AfterClass
    public static void afterClass()
    {
        XltPropertiesImpl.reset();
    }

    @Test
    public void test1() throws InterruptedException
    {
        // the usual suspects
        final String[] userNames =
            {
                "TVisit", "TBrowse", "TSearch", "TAddToCart", "TCheckout", "TGuestCheckout", "TOrder", "TGuestOrder", "TRegister"
            };

        doTest(userNames, 100, 10000);
    }

    @Test
    public void test2() throws InterruptedException
    {
        // very short user names
        final String[] userNames = new String[26];
        for (int i = 0; i < userNames.length; i++)
        {
            userNames[i] = "" + (char) ('A' + i);
        }

        doTest(userNames, 100, 10000);
    }

    private void doTest(final String[] userNames, final int userCount, final int iterations) throws InterruptedException
    {
        // a set that collects all seeds used
        final int expectedSetSize = userNames.length * userCount * iterations;
        final TLongHashSet set = new TLongHashSet(expectedSetSize);

        // simulate running the virtual users
        for (final String userName : userNames)
        {
            for (int u = 0; u < userCount; u++)
            {
                final String userId = userName + "-" + u;

                final VirtualUser vu = new VirtualUser(userId, iterations, set);
                vu.start();
                vu.join();
            }
        }

        // check results, we expect no collisions
        final int collisions = expectedSetSize - set.size();

        Assert.assertTrue(String.format("Collisions: %d of %d (%d%%)", collisions, expectedSetSize, collisions * 100 / expectedSetSize),
                          collisions == 0);
    }

    private static class VirtualUser extends Thread
    {
        private final int iterations;

        private final TLongHashSet set;

        public VirtualUser(final String userId, final int iterations, final TLongHashSet set)
        {
            super(new ThreadGroup(userId), userId);
            this.iterations = iterations;
            this.set = set;
        }

        @Override
        public void run()
        {
            SessionImpl.getCurrent().setLoadTest(true);

            for (int i = 0; i < iterations; i++)
            {
                final long seed = XltRandom.getSeed();

                // store the current seed to the set
                if (!set.add(seed))
                {
                    // System.err.printf("%s: %d / %d\n", name, i, seed);
                }

                XltRandom.reseed();
            }
        }
    }
}
