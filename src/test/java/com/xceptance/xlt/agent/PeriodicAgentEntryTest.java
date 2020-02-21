package com.xceptance.xlt.agent;

import java.util.PriorityQueue;

import org.junit.Assert;
import org.junit.Test;

import com.xceptance.xlt.agent.PeriodicExecutionTimer.AgentEntry;

public class PeriodicAgentEntryTest
{
    /**
     * test one Agent
     */
    @Test
    public void testOneAgentSimpleWeightFunction()
    {
        final double[] weightFunction =
            {
                0.1
            };
        PriorityQueue<AgentEntry> agentQueue = createAgentEntries(weightFunction);
        agentQueue = startUser(agentQueue, 0, 0);
        agentQueue = startUser(agentQueue, 0, 1);
        agentQueue = startUser(agentQueue, 0, 2);
        agentQueue = startUser(agentQueue, 0, 5);
    }

    /**
     * test one Agent
     */
    @Test
    public void testOneAgentMoreComplexWeightFunction()
    {
        final double[] weightFunction =
            {
                15.98756
            };
        PriorityQueue<AgentEntry> agentQueue = createAgentEntries(weightFunction);
        agentQueue = startUser(agentQueue, 0, 0);
        agentQueue = startUser(agentQueue, 0, 1);
        agentQueue = startUser(agentQueue, 0, 2);
        agentQueue = startUser(agentQueue, 0, 5);
    }

    /**
     * test what happens, if all properties are equal --> the agentIndex should decide
     */
    @Test
    public void testTwoAgentsSameWeightSameUserFunction()
    {
        final double[] weightFunction =
            {
                0.5, 0.5
            };
        PriorityQueue<AgentEntry> agentQueue = createAgentEntries(weightFunction);
        agentQueue = startUser(agentQueue, 0, 5);
        agentQueue = startUser(agentQueue, 1, 5);
        agentQueue = startUser(agentQueue, 0, 10);
        agentQueue = startUser(agentQueue, 1, 10);
        agentQueue = startUser(agentQueue, 0, 10);
        agentQueue = startUser(agentQueue, 1, 10);
        agentQueue = startUser(agentQueue, 0, 15);
        agentQueue = startUser(agentQueue, 1, 15);
    }

    /**
     * test what happens, if the weightedInvocations and the agentWeight are equal --> the lastStart should decide
     */
    @Test
    public void testTwoAgentsSameWeight()
    {
        final double[] weightFunction =
            {
                0.5, 0.5
            };
        PriorityQueue<AgentEntry> agentQueue = createAgentEntries(weightFunction);
        agentQueue = startUser(agentQueue, 0, 3);
        agentQueue = startUser(agentQueue, 1, 5);
        agentQueue = startUser(agentQueue, 0, 5);
        agentQueue = startUser(agentQueue, 1, 7);
        agentQueue = startUser(agentQueue, 0, 12);
        agentQueue = startUser(agentQueue, 1, 10);
        agentQueue = startUser(agentQueue, 1, 13);
        agentQueue = startUser(agentQueue, 0, 15);
        agentQueue = startUser(agentQueue, 1, 18);
        agentQueue = startUser(agentQueue, 0, 18);
    }

    /**
     * test what happens, if the weightedInvocations are equal --> the agentWeight should decide
     */
    @Test
    public void testTwoAgentsDifferentWeight()
    {
        final double[] weightFunction =
            {
                0.25, 0.75
            };
        PriorityQueue<AgentEntry> agentQueue = createAgentEntries(weightFunction);
        agentQueue = startUser(agentQueue, 1, 3);
        agentQueue = startUser(agentQueue, 0, 5);
        agentQueue = startUser(agentQueue, 1, 5);
        agentQueue = startUser(agentQueue, 1, 7);
        agentQueue = startUser(agentQueue, 1, 9);
        agentQueue = startUser(agentQueue, 0, 15);
        agentQueue = startUser(agentQueue, 1, 15);
        agentQueue = startUser(agentQueue, 1, 17);
        agentQueue = startUser(agentQueue, 1, 19);
        agentQueue = startUser(agentQueue, 0, 25);
    }

    /**
     * returns a priority queue of agent entries in dependency of the weight function
     * 
     * @param weightFunction
     * @return the priority queue of agent entries
     */
    private PriorityQueue<AgentEntry> createAgentEntries(final double[] weightFunction)
    {
        final PriorityQueue<AgentEntry> agentQueue = new PriorityQueue<AgentEntry>();

        for (int i = 0; i < weightFunction.length; i++)
        {
            agentQueue.add(new AgentEntry(i, weightFunction[i]));
        }
        return agentQueue;
    }

    /**
     * starts a user at an agent of the agent queue and check, if the expected agent starts the user
     * 
     * @param agentQueue
     *            priority queue of agent entries
     * @param expectedEntry
     *            expected index of the agent, that starts the user
     * @param time
     *            in which second should the user start
     * @return the adjusted agent queue
     */
    private PriorityQueue<AgentEntry> startUser(PriorityQueue<AgentEntry> agentQueue, final int expectedEntry, final int time)
    {
        // get the "lowest" Entry
        final AgentEntry entry = agentQueue.poll();
        // check, if the right Agent start a user
        Assert.assertEquals("The actual AgentEntry is not right: ", expectedEntry, entry.getAgentIndex());
        // adjust the queue
        agentQueue = increaseAgentEntry(agentQueue, entry, time);
        return agentQueue;
    }

    /**
     * increases the user count of an agent entry and adds this to the queue
     * 
     * @param agentQueue
     * @param entry
     * @param time
     * @return the adjusted agent queue
     */
    private PriorityQueue<AgentEntry> increaseAgentEntry(final PriorityQueue<AgentEntry> agentQueue, final AgentEntry entry, final int time)
    {
        entry.incrementInvocations(time);
        agentQueue.add(entry);
        return agentQueue;
    }
}
