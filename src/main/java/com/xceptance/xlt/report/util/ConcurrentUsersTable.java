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
package com.xceptance.xlt.report.util;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

/**
 * A data structure to track when a virtual user has executed its iterations, and to calculate the exact number of
 * concurrent users afterwards.
 * <p>
 * For each virtual user, a separate bit set is maintained. This bit set is usually as long as the test took in seconds.
 * A 1-bit will be set for each second the virtual user was running. From the individual bit sets, the total number of
 * concurrent users and the concurrent users of a certain user type can be calculated easily.
 */
public final class ConcurrentUsersTable
{
    /**
     * The one and only {@link ConcurrentUsersTable} instance.
     */
    private static ConcurrentUsersTable singleton = new ConcurrentUsersTable();

    /**
     * Returns the one and only {@link ConcurrentUsersTable} instance.
     * 
     * @return the {@link ConcurrentUsersTable} singleton
     */
    public static ConcurrentUsersTable getInstance()
    {
        return singleton;
    }

    /**
     * Maps a user ID (e.g. "TAuthor-0") to its corresponding bit set.
     */
    private final Map<String, BitSet> bitSetsByUserId = new TreeMap<String, BitSet>();

    /**
     * Maps a user name (e.g. "TAuthor") to the list of bit sets for users with this name.
     */
    private final Map<String, List<BitSet>> bitSetsByUserName = new HashMap<String, List<BitSet>>();

    /**
     * The earliest second for which a value is available.
     */
    private long start = Long.MAX_VALUE;

    /**
     * Private constructor.
     */
    private ConcurrentUsersTable()
    {
    }

    /**
     * Resets this table to initial values.
     */
    public void clear()
    {
        bitSetsByUserId.clear();
        bitSetsByUserName.clear();
        start = Long.MAX_VALUE;
    }

    /**
     * Marks the given virtual user as active in the passed time period.
     * 
     * @param fromTimestamp
     *            the timestamp [ms] when the user started to run
     * @param toTimestamp
     *            the timestamp [ms] when the user finished to run
     * @param userName
     *            the user's name (e.g. "TAuthor")
     * @param userNumber
     *            the user's number (e.g. "0")
     */
    public void recordUserActivity(long fromTimestamp, long toTimestamp, final String userName, final String userNumber)
    {
        final String userId = userName + "-" + userNumber;

        // get or create/register the bit set for the given user ID
        BitSet bitSet = bitSetsByUserId.get(userId);
        if (bitSet == null)
        {
            // create a new one and register it with its user ID
            bitSet = new BitSet();
            bitSetsByUserId.put(userId, bitSet);

            // register the bit set also with its user name
            List<BitSet> bitSets = bitSetsByUserName.get(userName);
            if (bitSets == null)
            {
                bitSets = new ArrayList<BitSet>();
                bitSetsByUserName.put(userName, bitSets);
            }

            bitSets.add(bitSet);
        }

        // we calculate with seconds only
        fromTimestamp /= 1000;
        toTimestamp /= 1000;

        // rearrange bit sets in case the timestamp is smaller than any we had before
        shiftAllBitSetsIfRequired(fromTimestamp);

        // set the bits
        final int offset = (int) (fromTimestamp - start);
        final int range = (int) (toTimestamp - fromTimestamp);
        for (int i = 0; i <= range; i++)
        {
            bitSet.set(offset + i);
        }
    }

    /**
     * Returns a value set with all the concurrent users, independent of the user name.
     * 
     * @return the value set with the concurrent users per second
     */
    public ValueSet getConcurrentUsersValueSet()
    {
        return getConcurrentUsersValueSet(null);
    }

    /**
     * Returns a value set with the concurrent users for the given user name.
     * 
     * @param userName
     *            the user name
     * @return the value set with the concurrent users per second
     */
    public ValueSet getConcurrentUsersValueSet(final String userName)
    {
        final Collection<BitSet> bitSets;

        // filter the bit sets if required
        if (userName == null || userName.equals("All Transactions"))
        {
            // use all bit sets
            bitSets = bitSetsByUserId.values();
        }
        else
        {
            bitSets = bitSetsByUserName.get(userName);
        }

        // build the value set from the selected bit sets
        final ValueSet valueSet = new ValueSet();
        if (bitSets != null)
        {
            for (final BitSet bitSet : bitSets)
            {
                for (int i = bitSet.nextSetBit(0); i >= 0; i = bitSet.nextSetBit(i + 1))
                {
                    final long timeMS = (i + start) * 1000;
                    valueSet.addOrUpdateValue(timeMS, 1);
                }
            }
        }
        return valueSet;
    }

    /**
     * Expands all known bit sets to the left if the passed time is before the current start time.
     * 
     * @param fromTimestamp
     *            the timestamp
     */
    private void shiftAllBitSetsIfRequired(final long fromTimestamp)
    {
        if (fromTimestamp < start)
        {
            final int shift = (int) (start - fromTimestamp);

            for (final BitSet bitSet : bitSetsByUserId.values())
            {
                shiftBitSet(bitSet, shift);
            }

            start = fromTimestamp;
        }
    }

    /**
     * Expands the given bit set to the left by right-shifting all bits by shift bits.
     * 
     * @param bitSet
     *            the bit set to expand
     * @param shift
     *            the number of bits to shift the entries in the bit set
     */
    private void shiftBitSet(final BitSet bitSet, final int shift)
    {
        // only non-empty bit sets need to be shifted
        if (!bitSet.isEmpty())
        {
            final BitSet tmpBitSet = (BitSet) bitSet.clone();

            bitSet.clear();
            for (int i = tmpBitSet.nextSetBit(0); i >= 0; i = tmpBitSet.nextSetBit(i + 1))
            {
                bitSet.set(shift + i);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString()
    {
        if (bitSetsByUserId.size() == 0)
        {
            return "Bit table is empty.";
        }
        else
        {
            final StringBuilder sb = new StringBuilder();

            for (final Entry<String, BitSet> entry : bitSetsByUserId.entrySet())
            {
                final String userId = entry.getKey();
                final BitSet bitSet = entry.getValue();

                sb.append(userId);
                for (int i = 0; i < bitSet.length(); i++)
                {
                    if (i % 5 == 0)
                    {
                        sb.append("|");
                    }
                    sb.append(bitSet.get(i) ? '1' : '0');
                }
                sb.append("\n");
            }

            return sb.toString();
        }
    }
}
