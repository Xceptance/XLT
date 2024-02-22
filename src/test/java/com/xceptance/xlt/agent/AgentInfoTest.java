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
package com.xceptance.xlt.agent;

import java.io.File;
import java.lang.reflect.Field;

import org.junit.Assert;
import org.junit.Test;

/**
 * Tests the {@link AgentInfo} class.
 * 
 * @author Sebastian Oerding
 */
public class AgentInfoTest
{
    @Test
    public void testConstructor() throws Exception
    {
        final String agentId = "007";
        final File agentDirectory = new File("XLT");
        final AgentInfo ai = new AgentInfo(agentId, agentDirectory);

        final Field resultsDirPathConstant = AgentInfo.class.getDeclaredField("NAME_RESULTS_DIR");
        resultsDirPathConstant.setAccessible(true);
        final String path = (String) resultsDirPathConstant.get(AgentInfo.class);

        Assert.assertEquals(agentId, ai.getAgentID());
        Assert.assertEquals(agentDirectory, ai.getAgentDirectory());
        final String actualpath = ai.getResultsDirectory().getAbsolutePath();
        final String expectedPath = agentDirectory.getAbsolutePath() + File.separator + path;
        Assert.assertEquals("The expected path \"" + expectedPath + "\" is not equal to the actual path \"" + actualpath + "\"",
                            expectedPath, actualpath);
    }
}
