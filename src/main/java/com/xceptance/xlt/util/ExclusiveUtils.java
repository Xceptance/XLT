/*
 * Copyright (c) 2005-2022 Xceptance Software Technologies GmbH
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

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;

import com.xceptance.xlt.api.engine.Session;

public class ExclusiveUtils<T>
{
    /**
     * Get the exclusive part of the data for the current agent.
     * 
     * @param data
     *            All global available data to partition
     * @return exclusive partition
     */
    public static <T>List<T> getExclusiveAgentPart(final List<T> data)
    {
        final int totalAgentcount = Session.getCurrent().getTotalAgentCount();
        final int currentAgentNumber = Session.getCurrent().getAgentNumber();

        return getExclusivePart(data, totalAgentcount, currentAgentNumber);
    }

    /**
     * Get the exclusive part of the data for the given stockholders index.
     * 
     * @param data
     *            All global available data to partition
     * @param numberOfParties
     *            total number of parties
     * @param currentPartyIndex
     *            index of the current party
     * @return
     */
    public static <T>List<T> getExclusivePart(final List<T> data, final int numberOfParties, final int currentPartyIndex)
    {
        // Precheck the parameters
        Assert.assertNotNull("Data must not be <null>.", data);
        Assert.assertFalse("There must be at least one party.", numberOfParties < 1);
        Assert.assertFalse("Current party index out of range.", currentPartyIndex < 0 || currentPartyIndex >= numberOfParties);

        // Get data size
        final int dataSize = data.size();

        // Stop if we do not have at least 1 date for each party
        Assert.assertTrue("Not enough data available.", dataSize >= numberOfParties);

        // If there's only 1 party we don't have to compute anything
        if (numberOfParties == 1)
        {
            return data;
        }
        else
        {
            // Contains all data for the current party
            final List<T> partition = new ArrayList<T>();

            // Compute the block numbers
            final int blockSize = numberOfParties;
            final int completeBlocks = (dataSize / blockSize);

            // Check for remainder
            final int mod = dataSize % blockSize;
            final int blocks = completeBlocks + (mod > 0 ? 1 : 0);

            // To get the partition data, fetch the current party's piece from each block
            for (int blockIndex = 0; blockIndex < blocks; blockIndex++)
            {
                final int dataIndex = (blockIndex * blockSize) + currentPartyIndex;
                if (dataIndex < dataSize)
                {
                    partition.add(data.get(dataIndex));
                }
                else
                {
                    break;
                }
            }

            return partition;
        }
    }
}
