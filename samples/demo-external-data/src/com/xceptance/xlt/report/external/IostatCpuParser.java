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
package com.xceptance.xlt.report.external;

/**
 * Parse CPU section of iostat output <code>iostat -c 1 -t</code>.
 */
public class IostatCpuParser extends AbstractIostatParser
{
    /**
     * Section identifier of CPU section.
     */
    protected static final String SECTION_DEVICE = "avg-cpu:";

    /**
     * {@inheritDoc}
     */
    @Override
    protected void parseHeader(final String line)
    {
        // you can replace the real head by your own names like ('tps', 'read', 'write' or 'read/s', 'write/s').
        // feel free but take care of the config file.
        final String[] heads = line.substring(SECTION_DEVICE.length(), line.length()).trim().split("\\s+");
        for (int index = 0; index < heads.length; index++)
        {
            final String headline = heads[index].replace("%", "");
            if (getValueNames().contains(headline))
            {
                getHeadlines().put(headline, index);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getSectionIdentifier()
    {
        return SECTION_DEVICE;
    }
}
