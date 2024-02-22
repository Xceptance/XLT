/*
 * Copyright (c) 2005-2024 Xceptance Software Technologies GmbH
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
package com.xceptance.xlt.ec2;

import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.TimeZone;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.amazonaws.services.ec2.model.Image;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.Tag;

public class MachineInfo
{
    private static final SimpleDateFormat TIME_FORMATTER;

    static
    {
        TIME_FORMATTER = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        TIME_FORMATTER.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    private final String host;

    private final String launchTime;

    private final String nameTag;

    private final String type;

    private final long runtime;

    private final String upTime;

    private final String keyName;

    private final String securityGroups;

    private final String state;

    private final String imageName;

    public String getHost()
    {
        return host;
    }

    public String getLaunchTime()
    {
        return launchTime;
    }

    public String getNameTag()
    {
        return nameTag;
    }

    public String getType()
    {
        return type;
    }

    public String getUpTime()
    {
        return upTime;
    }

    public long getRuntime()
    {
        return runtime;
    }

    public String getKeyName()
    {
        return keyName;
    }

    public String getImageName()
    {
        return imageName;
    }

    public String getSecurityGroups()
    {
        return securityGroups;
    }

    public String getState()
    {
        return state;
    }

    private MachineInfo(final String nameTag, final String host, final String launchTime, final long runtime, final String type,
                        final String keyName, final String imageName, final String securityGroups, final String state)
    {
        this.nameTag = nameTag;
        this.host = host;
        this.launchTime = launchTime;
        this.runtime = runtime;
        this.type = type;
        this.keyName = keyName;
        this.imageName = imageName;
        this.securityGroups = securityGroups;
        this.state = state;

        this.upTime = getUptimeStr(runtime);
    }

    public static MachineInfo createMachineInfo(final Instance instance, final Image image)
    {
        final String name = getNameFromTags(instance.getTags()).orElse("<not tagged>");

        String address = instance.getPublicIpAddress();
        if (StringUtils.isBlank(address))
        {
            address = StringUtils.defaultIfBlank(instance.getPublicDnsName(), "<unknown>");
        }

        final Date launchStart = instance.getLaunchTime();
        Duration diffToNow = Duration.between(launchStart.toInstant(), Instant.now());

        final long runTime = diffToNow.toMinutes();

        final List<String> secGroups = instance.getSecurityGroups().stream().map(g -> g.getGroupName()).collect(Collectors.toList());

        final String imageName = StringUtils.defaultIfBlank(getImageName(image), instance.getImageId());
        return new MachineInfo(name, address, TIME_FORMATTER.format(launchStart), runTime, instance.getInstanceType(),
                               StringUtils.defaultString(instance.getKeyName(), "<none>"), imageName, secGroups.isEmpty() ? "<none>" : StringUtils.join(secGroups, "; "),
                               instance.getState().getName());
    }

    private static String getImageName(final Image image)
    {
        if (image == null)
        {
            return null;
        }

        return getNameFromTags(image.getTags()).orElse(StringUtils.defaultIfBlank(image.getName(), image.getDescription()));
    }

    private static Optional<String> getNameFromTags(final List<Tag> tags)
    {
        return tags.stream().filter(tag -> tag.getKey().equals("Name")).map(tag -> tag.getValue()).findAny();
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
}
