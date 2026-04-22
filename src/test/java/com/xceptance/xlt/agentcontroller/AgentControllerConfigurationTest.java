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
