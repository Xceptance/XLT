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
package com.xceptance.xlt.engine.junit;

import java.io.File;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import com.xceptance.common.lang.Parameter;
import com.xceptance.common.lang.ReflectionUtils;

import util.lang.ClassFromByteArrayLoader;
import util.xlt.properties.ReversibleChangePipeline;


/**
 * @author Sebastian Oerding
 */
public class AbstractTestCaseRunnerTest
{
    /**
     * Tests the constructor for the ParameterizedFrameworkMethod.
     * 
     * @throws Throwable
     */
    @Test
    public void testParameterizedFrameworkMethod() throws Throwable
    {
        final Map<String, String> dataSet = getDummyDataSet();
        final Method method = new DummyTestClass().getDummyMethod();

        final Class<?> pFMClass = ReflectionUtils.getNestedClass(AbstractTestCaseRunner.class, "ParameterizedFrameworkMethod");
        final Method m = ReflectionUtils.getMethod(pFMClass, "getName");

        final Parameter<Map<String, String>> dataSetP = Parameter.valueOf(dataSet, Map.class);

        /* Case index == -1 */
        final Object o1 = ReflectionUtils.getNewInstance(pFMClass, method, "testMethodName", Parameter.valueOf(-1, int.class), dataSetP);
        final String name1 = ReflectionUtils.invokeMethod(o1, m);
        Assert.assertEquals("Name mismatch", "testMethodName", name1);

        /* Case index != -1 */
        final Object o2 = ReflectionUtils.getNewInstance(pFMClass, method, "testMethodName", Parameter.valueOf(1, int.class), dataSetP);
        final String name2 = ReflectionUtils.invokeMethod(o2, m);
        Assert.assertEquals("Name mismatch", "testMethodName[1] - {key=value}", name2);
    }

    @Test
    public void testStaticInitializationBlock()
    {
        final Class<com.xceptance.xlt.engine.junit.AbstractTestCaseRunner> sourceClass = com.xceptance.xlt.engine.junit.AbstractTestCaseRunner.class;

        final File dataDir = ReflectionUtils.readStaticField(sourceClass, "DATA_SETS_DIR");
        Assert.assertEquals("Wrong data directory!", null, dataDir);
        final ReversibleChangePipeline rcp = new ReversibleChangePipeline();
        rcp.addAndApply("com.xceptance.xlt.data.dataSets.dir", ".");
        final Class<?> theClass = ClassFromByteArrayLoader.getFreshlyLoadedClass(sourceClass);
        final File dataDir2 = ReflectionUtils.readStaticField(theClass, "DATA_SETS_DIR");
        Assert.assertNotNull("Set property \"com.xceptance.xlt.data.dataSets.dir\", expected the data dir not to be null!", dataDir2);
        Assert.assertEquals("Wrong data directory!", new File(".").getAbsolutePath(), dataDir2.getAbsolutePath());
        rcp.addAndApply("com.xceptance.xlt.data.dataSets.dir", "bin/agent.sh");
        final Class<?> theClass2 = ClassFromByteArrayLoader.getFreshlyLoadedClass(sourceClass);
        final File dataDir3 = ReflectionUtils.readStaticField(theClass2, "DATA_SETS_DIR");
        Assert.assertEquals("Wrong data directory!", null, dataDir3);
        rcp.reverseAll();
    }

    /**
     * @return a map containing the single pair "key", "value"
     */
    private Map<String, String> getDummyDataSet()
    {
        final Map<String, String> dataSet = new HashMap<String, String>();
        dataSet.put("key", "value");
        return dataSet;
    }
}
