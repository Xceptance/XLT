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
package com.xceptance.xlt.gce;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.google.api.services.compute.model.Instance;

/**
 * Aggregates useful information about an instance.
 */
class MachineInfo
{

    private static final DateTimeFormatter TIME_FORMATTER;

    static
    {
        TIME_FORMATTER = new DateTimeFormatterBuilder().appendPattern("yyyy-MM-dd HH:mm").toFormatter();
    }

    private final String host;

    private final String launchTime;

    private final String name;

    private final String type;

    private final long runtime;

    private final String upTime;

    private final String state;

    private MachineInfo(final String name, final String host, final String launchTime, final long runtime, final String type,
                        final String state)
    {
        this.name = name;
        this.host = host;
        this.launchTime = launchTime;
        this.runtime = runtime;
        this.type = type;
        this.state = state;

        this.upTime = getUptimeStr(runtime);
    }

    /**
     * Returns the public IP of the instance.
     * 
     * @return the public IP
     */
    public String getHost()
    {
        return host;
    }

    /**
     * Returns the launch (creation) time of the instance.
     * 
     * @return the launch time
     */
    public String getLaunchTime()
    {
        return launchTime;
    }

    /**
     * Returns the name of the instance.
     * 
     * @return the name
     */
    public String getName()
    {
        return name;
    }

    /**
     * Returns the machine type.
     * 
     * @return the machine type
     */
    public String getType()
    {
        return type;
    }

    /**
     * Returns the number of minutes the instance is running.
     * 
     * @return the runtime in minutes
     */
    public long getRuntime()
    {
        return runtime;
    }

    /**
     * Returns a human-readable representation of the instance' up-time.
     * 
     * @return the up-time
     */
    public String getUpTime()
    {
        return upTime;
    }

    /**
     * Returns the actual state of the instance.
     * 
     * @return the state
     */
    public String getState()
    {
        return state;
    }

    static MachineInfo createMachineInfo(final Instance instance)
    {
        final String name = instance.getName();
        String address = GceAdminUtils.getIpAddress(instance);

        final OffsetDateTime oTime = OffsetDateTime.from(DateTimeFormatter.ISO_OFFSET_DATE_TIME.parse(instance.getCreationTimestamp()));
        final Duration diffToNow = Duration.between(oTime, OffsetDateTime.now());

        final long runtime = diffToNow.toMinutes();

        return new MachineInfo(name, address, oTime.withOffsetSameInstant(ZoneOffset.of("Z")).format(TIME_FORMATTER), runtime,
                               GceAdminUtils.getMachineType(instance.getMachineType()), instance.getStatus());
    }

    private static String getUptimeStr(final long runtimeInMinutes)
    {
        final StringBuilder sb = new StringBuilder();
        if (runtimeInMinutes > 0)
        {
            final long min = runtimeInMinutes % 60;
            long hrs = runtimeInMinutes / 60;
            final long days = hrs / 24;
            hrs = hrs % 24;

            if (days > 0)
            {
                sb.append(days).append("d ");
            }
            if (hrs < 10)
            {
                sb.append(0);
            }
            sb.append(hrs).append(":");
            if (min < 10)
            {
                sb.append(0);
            }
            sb.append(min);
        }

        return sb.toString();
    }

    static class Printer
    {
        static String prettyPrint(final List<MachineInfo> machineInformation, final String offset)
        {
            final StringBuilder sb = new StringBuilder();

            final String nameHeading = "Name";
            final String hostHeading = "Host";
            final String typeHeading = "Type";
            final String stateHeading = "State";
            final String startTimeHeading = "Launch Time (UTC)";
            final String uptimeHeading = "Uptime (h:mm)";

            int maxName = hostHeading.length();
            int maxHost = nameHeading.length();
            int maxType = typeHeading.length();
            int maxState = stateHeading.length();
            int maxStartTime = startTimeHeading.length();
            int maxUptime = uptimeHeading.length();

            final List<MachineInfo> machinesSortedByUptime = new ArrayList<>();

            // First we need to determine the maximum length for each column
            for (MachineInfo info : machineInformation)
            {
                maxHost = Math.max(maxHost, info.getHost().length());
                maxName = Math.max(maxName, info.getName().length());
                maxStartTime = Math.max(maxStartTime, info.getLaunchTime().length());
                maxUptime = Math.max(maxUptime, info.getUpTime().length());
                maxType = Math.max(maxType, info.getType().length());
                maxState = Math.max(maxState, info.getState().length());

                machinesSortedByUptime.add(info);
            }

            // Sort entries by up-time in descending order
            Collections.sort(machinesSortedByUptime, new Comparator<MachineInfo>()
            {
                @Override
                public int compare(MachineInfo o1, MachineInfo o2)
                {
                    return Long.compare(o2.getRuntime(), o1.getRuntime());
                }
            });

            final String colSep = " | ";
            final int maxLineLength = maxHost + maxName + maxStartTime + maxUptime + maxType + maxState + 5 * colSep.length();
            final String dashLine = offset + StringUtils.repeat('-', maxLineLength) + "\n";

            sb.append(dashLine);

            // Build the table header
            sb.append(offset).append(StringUtils.center(nameHeading, maxName)).append(colSep)
              .append(StringUtils.center(hostHeading, maxHost)).append(colSep).append(StringUtils.center(typeHeading, maxType))
              .append(colSep)

              .append(StringUtils.center(stateHeading, maxState)).append(colSep).append(StringUtils.center(startTimeHeading, maxStartTime))
              .append(colSep).append(StringUtils.center(uptimeHeading, maxUptime)).append('\n');

            sb.append(dashLine);

            // Now we generate the actual machine output
            final String lineFormat = String.format("%s%%-%ds" + colSep + "%%-%ds" + colSep + "%%-%ds" + colSep + "%%-%ds" + colSep +
                                                    "%%%ds" + colSep + "%%%ds\n", offset, maxName, maxHost, maxType, maxState, maxStartTime,
                                                    maxUptime);
            for (MachineInfo info : machinesSortedByUptime)
            {
                sb.append(String.format(lineFormat, info.getName(), info.getHost(), info.getType(), info.getState(), info.getLaunchTime(),
                                        info.getUpTime()));
            }
            sb.append(dashLine);

            return sb.toString();
        }
    }
}
