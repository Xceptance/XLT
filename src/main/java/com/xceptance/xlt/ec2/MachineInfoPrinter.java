package com.xceptance.xlt.ec2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.apache.commons.lang3.StringUtils;

public class MachineInfoPrinter
{
    public static String prettyPrint(final List<MachineInfo> machineInformation, final String offset)
    {
        final StringBuilder sb = new StringBuilder();

        final String nameHeading = "Name";
        final String hostHeading = "Host";
        final String typeHeading = "Type";
        final String keyNameHeading = "Key-Pair Name";
        final String secGroupHeading = "Security Groups";
        final String imageHeading = "Image";
        final String stateHeading = "State";
        final String startTimeHeading = "Launch Time (UTC)";
        final String uptimeHeading = "Uptime (h:mm)";

        int maxName = hostHeading.length();
        int maxHost = nameHeading.length();
        int maxType = typeHeading.length();
        int maxKey = keyNameHeading.length();
        int maxSecGroup = secGroupHeading.length();
        int maxImage = imageHeading.length();
        int maxState = stateHeading.length();
        int maxStartTime = startTimeHeading.length();
        int maxUptime = uptimeHeading.length();

        final List<MachineInfo> machinesSortedByUptime = new ArrayList<>();

        // First we need to determine the maximum length for each column
        for (MachineInfo info : machineInformation)
        {
            maxHost = Math.max(maxHost, info.getHost().length());
            maxName = Math.max(maxName, info.getNameTag().length());
            maxStartTime = Math.max(maxStartTime, info.getLaunchTime().length());
            maxUptime = Math.max(maxUptime, info.getUpTime().length());
            maxType = Math.max(maxType, info.getType().length());
            maxKey = Math.max(maxKey, info.getKeyName().length());
            maxSecGroup = Math.max(maxSecGroup, info.getSecurityGroups().length());
            maxImage = Math.max(maxImage, info.getImageName().length());
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
        final int maxLineLength = maxHost + maxName + maxStartTime + maxUptime + maxType + maxKey + maxSecGroup + maxImage + maxState +
                                  8 * colSep.length();
        final String dashLine = offset + StringUtils.repeat('-', maxLineLength) + "\n";

        sb.append(dashLine);

        // Build the table header
        sb.append(offset).append(StringUtils.center(nameHeading, maxName)).append(colSep).append(StringUtils.center(hostHeading, maxHost))
          .append(colSep).append(StringUtils.center(typeHeading, maxType)).append(colSep).append(StringUtils.center(keyNameHeading, maxKey))
          .append(colSep).append(StringUtils.center(secGroupHeading, maxSecGroup)).append(colSep)
          .append(StringUtils.center(imageHeading, maxImage)).append(colSep).append(StringUtils.center(stateHeading, maxState))
          .append(colSep).append(StringUtils.center(startTimeHeading, maxStartTime)).append(colSep)
          .append(StringUtils.center(uptimeHeading, maxUptime)).append('\n');

        sb.append(dashLine);

        // Now we generate the actual machine output
        final String lineFormat = String.format("%s%%-%ds" + colSep + "%%-%ds" + colSep + "%%-%ds" + colSep + "%%-%ds" + colSep + "%%-%ds" +
                                                colSep + "%%-%ds" + colSep + "%%-%ds" + colSep + "%%%ds" + colSep + "%%%ds\n", offset,
                                                maxName, maxHost, maxType, maxKey, maxSecGroup, maxImage, maxState, maxStartTime,
                                                maxUptime);
        for (MachineInfo info : machinesSortedByUptime)
        {
            sb.append(String.format(lineFormat, info.getNameTag(), info.getHost(), info.getType(), info.getKeyName(),
                                    info.getSecurityGroups(), info.getImageName(), info.getState(), info.getLaunchTime(), info.getUpTime()));
        }
        sb.append(dashLine);

        return sb.toString();
    }
}
