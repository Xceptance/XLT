/*
 * Copyright (c) 2005-2021 Xceptance Software Technologies GmbH
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
package com.xceptance.xlt.engine;

import java.io.File;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.xceptance.xlt.api.engine.Session;
import com.xceptance.xlt.api.util.XltProperties;
import com.xceptance.xlt.common.XltConstants;

/**
 * Tests the implementation of {@link DataManagerImpl}.
 *
 * @author Hartmut Arlt (Xceptance Software Technologies GmbH)
 */
@Ignore
public class DataManagerImplTest
{
    private static final String PROP_RESULT_DIR = "com.xceptance.xlt.result-dir";

    private String resultDir;

    @Before
    public void setup()
    {
        resultDir = XltProperties.getInstance().getProperty(PROP_RESULT_DIR);
    }

    @After
    public void tearDown()
    {
        if (resultDir != null)
        {
            XltProperties.getInstance().setProperty(PROP_RESULT_DIR, resultDir);
        }
    }

    @Test
    public void testGetTimerFile_ResultDirIsEmpty() throws Exception
    {
        XltProperties.getInstance().setProperty(PROP_RESULT_DIR, "");
        Session.getCurrent().clear();

        final File timerFile = new DataManagerImpl((SessionImpl) Session.getCurrent()).getTimerFile();

        Assert.assertNotNull("No timer file", timerFile);
        timerFile.equals(new File(new File(XltConstants.RESULT_ROOT_DIR), XltConstants.TIMER_FILENAME));
    }

    @Test
    public void testGetTimerFile_ResultDirIsNotEmpty() throws Exception
    {
        final String dir = ".";
        XltProperties.getInstance().setProperty(PROP_RESULT_DIR, dir);
        Session.getCurrent().clear();

        final File timerFile = new DataManagerImpl((SessionImpl) Session.getCurrent()).getTimerFile();

        Assert.assertNotNull("No timer file", timerFile);
        timerFile.equals(new File(new File(dir), XltConstants.TIMER_FILENAME));
    }
}
