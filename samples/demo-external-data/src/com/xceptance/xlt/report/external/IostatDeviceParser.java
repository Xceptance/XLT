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
package com.xceptance.xlt.report.external;

/**
 * Parse and create report fragment of command line output e.g. <code>iostat -x 1 -t</code> or
 * <code>iostat -x 1 -t</code>.
 */
public class IostatDeviceParser extends AbstractIostatParser
{
    /**
     * Section identifier of Device section.
     */
    protected static final String SECTION_DEVICE = "Device:";

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getSectionIdentifier()
    {
        return SECTION_DEVICE;
    }
}
