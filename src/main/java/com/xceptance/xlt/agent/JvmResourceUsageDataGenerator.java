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
package com.xceptance.xlt.agent;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.math3.util.Precision;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xceptance.common.lang.ThreadUtils;
import com.xceptance.common.util.ProcessUtils;
import com.xceptance.xlt.api.engine.DataManager;
import com.xceptance.xlt.api.engine.GlobalClock;
import com.xceptance.xlt.api.engine.Session;
import com.xceptance.xlt.api.util.XltProperties;
import com.xceptance.xlt.engine.SessionImpl;

/**
 * The {@link JvmResourceUsageDataGenerator} is responsible to periodically log the resource usage of the agent JVM.
 *
 * @author Jörg Werner (Xceptance Software Technologies GmbH)
 */
public class JvmResourceUsageDataGenerator extends Thread
{
    /**
     * The name of the directory to which observations will be stored.
     */
    public static final String RESULT_DIRECTORY_NAME = "Agent-JVM-Monitor";

    /**
     * The names of GCs which perform a full GC.
     */
    private static final String[] KNOWN_FULL_GC_NAMES =
        {
            "MarkSweepCompact", "ConcurrentMarkSweep", "PS MarkSweep", "G1 Old Generation"
        };

    /**
     * The names of GCs which perform a minor GC.
     */
    private static final String[] KNOWN_MINOR_GC_NAMES =
        {
            "Copy", "ParNew", "PS Scavenge", "G1 Young Generation"
        };

    /**
     * The logger.
     */
    private static final Logger log = LoggerFactory.getLogger(JvmResourceUsageDataGenerator.class);

    /**
     * The default logging interval.
     */
    private static final int DEFAULT_LOG_INTERVAL = 10000;

    /**
     * The number of processors available to the current JVM.
     */
    private final int cpuCount;

    /**
     * The process's CPU time value measured last.
     */
    private long lastCpuTime;

    /**
     * The last measured time spent in full GC.
     */
    private long lastFullGcTime;

    /**
     * The last measured count of full GC.
     */
    private long lastFullGcCount;

    /**
     * The last measured time spent in minor GC.
     */
    private long lastMinorGcTime;

    /**
     * The last measured count of minor GC.
     */
    private long lastMinorGcCount;

    /**
     * The CPU times per thread.
     */
    private Map<Long, Long> lastThreadCpuTimes = new HashMap<Long, Long>();

    /**
     * The last up-time value.
     */
    private long lastUptime;

    /**
     * The logging interval.
     */
    private final long logInterval = XltProperties.getInstance().getProperty("com.xceptance.xlt.agent.monitoring.samplingInterval",
                                                                             DEFAULT_LOG_INTERVAL);

    /**
     * The statistics manager used to store resource usage data.
     */
    private final DataManager dataManager;

    /**
     * Indicates whether we can safely cast an {@link OperatingSystemMXBean} object to
     * com.sun.management.OperatingSystemMXBean, which offers more functionality.
     */
    private final boolean sunApiAvailable;

    /**
     * The current up-time value, i.e. the number of milliseconds the JVM is already running.
     */
    private long uptime;

    /**
     * The {@link JvmResourceUsageData} object to update and log regularly.
     */
    private final JvmResourceUsageData usageData;

    /**
     * Constructor.
     *
     * @param name
     *            the name of the agent
     * @param hostName
     *            the host name
     * @param port
     *            the port the agent controller listens at (used to generate a unique agent name)
     * @param startOfLoggingPeriod
     *            the time to start logging resource usage data
     * @param endOfLoggingPeriod
     *            the time to stop logging resource usage data
     */
    public JvmResourceUsageDataGenerator(final String name, final String hostName, final int port, final long startOfLoggingPeriod,
                                         final long endOfLoggingPeriod)
    {
        // set the thread's name
        final SessionImpl session = (SessionImpl) Session.getCurrent();
        session.setAgentID(name);
        session.setUserName(RESULT_DIRECTORY_NAME);
        session.setLoadTest(true);

        setName(session.getUserID());

        // generate an artificial agent name
        final String jvmName = "Agent-" + name + "-" + hostName + "-" + port;
        usageData = new JvmResourceUsageData(jvmName);

        // determine the number of CPUs available
        cpuCount = ManagementFactory.getOperatingSystemMXBean().getAvailableProcessors();

        // check whether a special SUN mbean is available
        // (do not use instanceof -> will break on a non-Sun JVM)
        sunApiAvailable = checkIfClassImplementsInterface(ManagementFactory.getOperatingSystemMXBean().getClass(),
                                                          "com.sun.management.OperatingSystemMXBean");

        // setup statistics manager
        dataManager = session.getDataManager();
        dataManager.setStartOfLoggingPeriod(startOfLoggingPeriod);
        dataManager.setEndOfLoggingPeriod(endOfLoggingPeriod);
    }

    /**
     * Logs the resource usage data periodically.
     */
    @Override
    public void run()
    {
        log.debug("JVM resource usage monitoring thread started.");

        while (true)
        {
            ThreadUtils.sleep(logInterval);

            updateStats(usageData);
            dataManager.logDataRecord(usageData);
        }
    }

    /**
     * Checks whether the given class implements an interface with the given fully-qualified name. We have to use the
     * interface's name here since this method is supposed to work also on JDKs where the interface is not available.
     *
     * @param cls
     *            the class
     * @param interfaceName
     *            the interface's name
     * @return <code>true</code> if the class implements the interface, <code>false</code> otherwise
     */
    private boolean checkIfClassImplementsInterface(final Class<?> cls, final String interfaceName)
    {
        for (final Class<?> iface : cls.getInterfaces())
        {
            // first check the current interface's name, then recursively its super interfaces
            if (iface.getName().equals(interfaceName) || checkIfClassImplementsInterface(iface, interfaceName))
            {
                return true;
            }
        }

        return false;
    }

    /**
     * Returns an estimation of the time that the current process used the CPU. This value is derived from the CPU times
     * of the currently running threads. Since threads may come and go, and we look at them in certain intervals only,
     * we can easily miss one thread or the other, so the CPU time calculated this way will always be lower than the
     * real CPU time.
     *
     * @return the CPU time (in ns)
     */
    private long getProcessCpuTime()
    {
        long cpuTime = 0;

        final Map<Long, Long> newThreadCpuTimes = new HashMap<Long, Long>();

        final ThreadMXBean threadMxBean = ManagementFactory.getThreadMXBean();
        for (final long id : threadMxBean.getAllThreadIds())
        {
            final long newValue = threadMxBean.getThreadCpuTime(id);
            if (newValue != -1)
            {
                // remember this id -> thread is still alive
                newThreadCpuTimes.put(id, newValue);

                // check whether this thread is known
                final Long last = lastThreadCpuTimes.get(id);
                if (last == null)
                {
                    // no -> add the whole CPU time
                    cpuTime += newValue;
                }
                else
                {
                    // yes -> just add the difference CPU time
                    cpuTime += newValue - last;
                }
            }
        }

        // replace last values with new ones (dead threads not included anymore)
        lastThreadCpuTimes = newThreadCpuTimes;

        return lastCpuTime + cpuTime;
    }

    /**
     * Updates the passed resource usage data with the current CPU usage values.
     *
     * @param usageData
     *            the usage data object
     */
    @SuppressWarnings("restriction")
    private void setCpuStats(final JvmResourceUsageData usageData)
    {
        long newCpuTime = 0;

        final OperatingSystemMXBean osMxBean = ManagementFactory.getOperatingSystemMXBean();

        // check whether we run on a Sun JVM
        if (sunApiAvailable)
        {
            // this value is accurate (maintained by the OS)
            newCpuTime = ((com.sun.management.OperatingSystemMXBean) osMxBean).getProcessCpuTime();
        }
        else
        {
            // this value is an estimation only
            newCpuTime = getProcessCpuTime();
        }

        // do not take implausible CPU times into account (#1093)
        if (newCpuTime >= lastCpuTime)
        {
            // derive the current CPU usage from the last CPU time and the time
            // passed since last call
            final long diffCpuTime = (newCpuTime - lastCpuTime) / 1000000;
            final long diffTime = uptime - lastUptime;
            double usage = diffCpuTime * 100.0 / diffTime / cpuCount;
            usage = Precision.round(usage, 2);

            // alternative way to determine the usage (but doesn't work reliably on JDK7/Win)
            // usage = ((com.sun.management.OperatingSystemMXBean) osMxBean).getProcessCpuLoad() * 100;

            usage = limitUsageValue(usage);

            usageData.setCpuUsage(usage);

            // remember this value for the next update cycle
            lastCpuTime = newCpuTime;
        }

        // determine the system's total CPU usage
        double totalCpuUsage;
        if (sunApiAvailable)
        {
            // this value is accurate (maintained by the OS)
            totalCpuUsage = ((com.sun.management.OperatingSystemMXBean) osMxBean).getSystemCpuLoad() * 100;
            totalCpuUsage = Precision.round(totalCpuUsage, 2);
            totalCpuUsage = limitUsageValue(totalCpuUsage);
        }
        else
        {
            totalCpuUsage = 0;
        }

        usageData.setTotalCpuUsage(totalCpuUsage);
    }

    /**
     * Updates the passed resource usage data with the current garbage collection information.
     *
     * @param usageData
     *            the usage data object
     */
    private void setGarbageCollectionStats(final JvmResourceUsageData usageData)
    {
        for (final GarbageCollectorMXBean gcMXBean : ManagementFactory.getGarbageCollectorMXBeans())
        {
            final String gcName = gcMXBean.getName();

            final long gcCount = gcMXBean.getCollectionCount();
            final long gcTime = gcMXBean.getCollectionTime();
            double gcPercentage = 0.0;
            long gcTimeDiff;
            long gcCountDiff;

            if (ArrayUtils.contains(KNOWN_MINOR_GC_NAMES, gcName))
            {
                gcTimeDiff = (gcTime - lastMinorGcTime);
                gcCountDiff = (gcCount - lastMinorGcCount);

                gcPercentage = gcTimeDiff * 100.0 / (uptime - lastUptime) / cpuCount;
                gcPercentage = Precision.round(gcPercentage, 2);

                gcPercentage = limitUsageValue(gcPercentage);

                usageData.setMinorGcCount(gcCount);
                usageData.setMinorGcTime(gcTime);
                usageData.setMinorGcCpuUsage(gcPercentage);
                usageData.setMinorGcTimeDiff((int) gcTimeDiff);
                usageData.setMinorGcCountDiff((int) gcCountDiff);

                lastMinorGcTime = gcTime;
                lastMinorGcCount = gcCount;
            }
            else if (ArrayUtils.contains(KNOWN_FULL_GC_NAMES, gcName))
            {
                gcTimeDiff = (gcTime - lastFullGcTime);
                gcCountDiff = (gcCount - lastFullGcCount);

                gcPercentage = gcTimeDiff * 100.0 / (uptime - lastUptime) / cpuCount;
                gcPercentage = Precision.round(gcPercentage, 2);

                gcPercentage = limitUsageValue(gcPercentage);

                usageData.setFullGcCount(gcCount);
                usageData.setFullGcTime(gcTime);
                usageData.setFullGcCpuUsage(gcPercentage);
                usageData.setFullGcTimeDiff((int) gcTimeDiff);
                usageData.setFullGcCountDiff((int) gcCountDiff);

                lastFullGcTime = gcTime;
                lastFullGcCount = gcCount;
            }
            else
            {
                log.debug("Don't know how to handle statistics of GC: " + gcName);
            }
        }
    }

    /**
     * Updates the passed resource usage data with the current heap usage values.
     *
     * @param usageDate
     *            the usage data object
     */
    private void setHeapMemoryStats(final JvmResourceUsageData usageDate)
    {
        final Runtime runtime = Runtime.getRuntime();

        final long totalHeap = runtime.totalMemory();
        final long usedHeap = totalHeap - runtime.freeMemory();
        double heapUsage = usedHeap * 100.0 / totalHeap;
        heapUsage = Precision.round(heapUsage, 2);

        usageDate.setTotalHeapSize(totalHeap);
        usageDate.setUsedHeapSize(usedHeap);
        usageDate.setHeapUsage(heapUsage);
    }

    /**
     * Updates the passed resource usage data with the current number of threads.
     *
     * @param usageData
     *            the usage data object
     */
    private void setNumberOfThreads(final JvmResourceUsageData usageData)
    {
        final ThreadMXBean threadMxBean = ManagementFactory.getThreadMXBean();

        final long[] threadIds = threadMxBean.getAllThreadIds();
        final ThreadInfo[] threadInfos = threadMxBean.getThreadInfo(threadIds);

        int runnable = 0;
        int blocked = 0;
        int waiting = 0;

        for (final ThreadInfo threadInfo : threadInfos)
        {
            // thread may terminate between getAllThreadIds() and
            // getThreadInfo(), so check for that
            if (threadInfo != null)
            {
                final Thread.State threadState = threadInfo.getThreadState();

                if (threadState == Thread.State.RUNNABLE)
                {
                    runnable++;
                }
                else if (threadState == Thread.State.BLOCKED)
                {
                    blocked++;
                }
                else if (threadState == Thread.State.TIMED_WAITING || threadState == Thread.State.WAITING)
                {
                    waiting++;
                }
            }
        }

        usageData.setRunnableThreadCount(runnable);
        usageData.setBlockedThreadCount(blocked);
        usageData.setWaitingThreadCount(waiting);
    }

    /**
     * Updates the passed resource usage data with the current memory usage values.
     *
     * @param usageData
     *            the usage data object
     */
    @SuppressWarnings("restriction")
    private void setPhysicalMemoryStats(final JvmResourceUsageData usageData)
    {
        long totalMem = 0;
        long usedMem = 0;
        double memUsage = 0;

        final OperatingSystemMXBean osMxBean = ManagementFactory.getOperatingSystemMXBean();

        // check whether we run on a Sun JVM
        if (sunApiAvailable)
        {
            // these values may be more accurate
            usedMem = ((com.sun.management.OperatingSystemMXBean) osMxBean).getCommittedVirtualMemorySize();

            totalMem = ((com.sun.management.OperatingSystemMXBean) osMxBean).getTotalPhysicalMemorySize();
            memUsage = usedMem * 100.0 / totalMem;
            memUsage = Precision.round(memUsage, 2);
        }
        else
        {
            // these values may differ a little
            usedMem = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getCommitted() +
                      ManagementFactory.getMemoryMXBean().getNonHeapMemoryUsage().getCommitted();
        }

        usageData.setCommittedMemorySize(usedMem);
        usageData.setMemoryUsage(memUsage);
    }

    /**
     * Updates the passed resource usage data record.
     *
     * @param usageData
     *            the usage data object
     */
    private void updateStats(final JvmResourceUsageData usageData)
    {
        uptime = ProcessUtils.getUptime();

        // update the usage data object
        usageData.setTime(GlobalClock.getInstance().getTime());

        setHeapMemoryStats(usageData);
        setPhysicalMemoryStats(usageData);
        setCpuStats(usageData);
        setNumberOfThreads(usageData);
        setGarbageCollectionStats(usageData);

        lastUptime = uptime;
    }

    /**
     * Limits the given usage value such that 0 &le; value &le; 100. This is necessary especially for CPU usage values
     * as slight timing differences or system time corrections may cause usage values that are either negative or exceed
     * 100.0.
     *
     * @param usage
     *            the usage value
     * @return the limited usage value
     */
    private double limitUsageValue(double usage)
    {
        // make sure the value neither is negative (#1093), nor exceeds 100%
        usage = Math.min(usage, 100.0);
        usage = Math.max(usage, 0.0);

        return usage;
    }
}
