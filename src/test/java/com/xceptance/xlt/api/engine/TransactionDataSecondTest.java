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
package com.xceptance.xlt.api.engine;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.xceptance.common.lang.ReflectionUtils;
import com.xceptance.xlt.api.util.XltCharBuffer;
import com.xceptance.xlt.api.util.XltCharBufferUtil;
import com.xceptance.xlt.common.XltConstants;

/**
 * @author Sebastian Oerding
 */
public class TransactionDataSecondTest
{
    @Test
    public void testGetDumpDirectoryPath()
    {
        final DummyTransactionData td = new DummyTransactionData();
        Assert.assertEquals("Default changed, ", null, td.getDumpDirectoryPath());

        td.setTestUserNumber("12");
        Assert.assertEquals("Default changed, ", null, td.getDumpDirectoryPath());

        final String stackTrace = "a (user: 'testUser', output: '1234567890')";
        final List<XltCharBuffer> values = XltCharBufferUtil.toList(new String[]
            {
                "T", "noname", "123", "1", "true", stackTrace
            });
        td.parseValues(values);

        final String directoryName = ReflectionUtils.readField(TransactionData.class, td, "directoryName");

        td.setAgentName("007");
        final String expected = td.getAgentName() + "/" + td.getName() + "/" + td.getTestUserNumber() + "/" + XltConstants.DUMP_OUTPUT_DIR +
                                "/" + directoryName;

        Assert.assertEquals("Wrong dump directory", expected, td.getDumpDirectoryPath());

        final List<XltCharBuffer> values2 = XltCharBufferUtil.toList(new String[]
            {
                "T", "noname", "123", "1", "true", ""
            });
        td.parseValues(values2);
        
        Assert.assertEquals("Wrong stack trace", null, td.getFailureStackTrace());
        
        final List<XltCharBuffer> values3 = XltCharBufferUtil.toList(new String[]
            {
                "T", "noname", "123", "1", "true", "neitherMatchingNorEmptySTackTrace"
            });
        td.parseValues(values3);
    }
}
