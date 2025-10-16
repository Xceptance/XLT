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
package com.xceptance.xlt.report.scorecard;

import com.thoughtworks.xstream.annotations.XStreamConverter;
import com.thoughtworks.xstream.converters.enums.EnumToStringConverter;

@XStreamConverter(EnumToStringConverter.class)
enum Status
{
    SKIPPED,
    PASSED,
    FAILED("NOTPASSED"),
    ERROR;

    private final String displayValue;

    private Status(final String value)
    {
        this.displayValue = value;
    }

    private Status()
    {
        this(null);
    }

    public boolean isPassed()
    {
        return Status.PASSED == this;
    }

    public boolean isFailed()
    {
        return Status.FAILED == this;
    }

    public boolean isError()
    {
        return Status.ERROR == this;
    }

    public boolean isSkipped()
    {
        return Status.SKIPPED == this;
    }

    @Override
    public String toString()
    {
        return displayValue == null ? name() : displayValue;
    }

    /**
     * Returns the negated status of this status (PASSED &rarr; FAILED; FAILED &rarr; PASSED; SKIPPED and ERROR stay as
     * is).
     * 
     * @return this status negated
     */
    Status negate()
    {
        if (isPassed())
        {
            return FAILED;
        }
        if (isFailed())
        {
            return PASSED;
        }
        return this;
    }

}
