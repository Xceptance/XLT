package com.xceptance.xlt.api.engine;

import org.junit.Assert;
import org.junit.Test;

import com.xceptance.common.lang.ReflectionUtils;
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
        String[] values = new String[]
            {
                "T", "noname", "123", "1", "true", stackTrace
            };
        td.parseValues(values);

        final String directoryName = ReflectionUtils.readField(TransactionData.class, td, "directoryName");

        td.setAgentName("007");
        final String expected = td.getAgentName() + "/" + td.getName() + "/" + td.getTestUserNumber() + "/" + XltConstants.DUMP_OUTPUT_DIR +
                                "/" + directoryName;

        Assert.assertEquals("Wrong dump directory", expected, td.getDumpDirectoryPath());

        values = new String[]
            {
                "T", "noname", "123", "1", "true", ""
            };
        td.parseValues(values);
        Assert.assertEquals("Wrong stack trace", null, td.getFailureStackTrace());
        values = new String[]
            {
                "T", "noname", "123", "1", "true", "neitherMatchingNorEmptySTackTrace"
            };
        td.parseValues(values);
    }
}
