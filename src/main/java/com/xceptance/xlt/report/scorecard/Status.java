/*
 * Copyright (c) 2005-2026 Xceptance Software Technologies GmbH
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
import com.thoughtworks.xstream.converters.SingleValueConverter;

@XStreamConverter(Status.StatusConverter.class)
public enum Status
{
    SKIPPED,
    PASSED,
    FAILED,
    ERROR;

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

    /**
     * Returns the negated status of this status (PASSED &rarr; FAILED; FAILED &rarr; PASSED; SKIPPED and ERROR stay as
     * is).
     *
     * @return this status negated
     */
    public Status negate()
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

    public static class StatusConverter implements SingleValueConverter
    {
        @Override
        public boolean canConvert(@SuppressWarnings("rawtypes") final Class type)
        {
            return type == Status.class;
        }

        @Override
        public String toString(final Object obj)
        {
            return ((Status) obj).name();
        }

        @Override
        public Object fromString(final String str)
        {
            if ("NOTPASSED".equalsIgnoreCase(str))
            {
                return Status.FAILED;
            }
            return Status.valueOf(str.toUpperCase());
        }
    }
}
