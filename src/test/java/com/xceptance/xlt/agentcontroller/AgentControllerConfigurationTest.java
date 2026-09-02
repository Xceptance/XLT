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
package com.xceptance.xlt.agentcontroller;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.xceptance.xlt.api.util.XltException;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;

@RunWith(JUnitParamsRunner.class)
public class AgentControllerConfigurationTest
{
    @Test
    @Parameters(
        {
            "1", "12", "1-2", // numbers
            "a", "aA", "a-A", // letters
            "012345678901234567890123456789012345678901234567890123456789012", // exactly 63 chars
    })
    public void validatePrivateMachineName_validName(String text)
    {
        AgentControllerConfiguration.validatePrivateMachineName(text);
    }

    @Test
    @Parameters(
        {
            "", // empty
            "0123456789012345678901234567890123456789012345678901234567890123", // more than 63 chars
            "-", // hyphen only
            "-abc", // starts with hyphen
            "abc-", // ends with hyphen
            "a.b", // invalid char
    })
    public void validatePrivateMachineName_invalidName(String text)
    {
        Assert.assertThrows(XltException.class, () -> AgentControllerConfiguration.validatePrivateMachineName(text));
    }
}
