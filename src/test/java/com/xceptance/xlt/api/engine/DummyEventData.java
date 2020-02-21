package com.xceptance.xlt.api.engine;

/**
 * This class provides a dummy implementation of {@link CustomData} but makes {@link #parseValues(String[])} public to
 * allow modifications for testing purposes.
 * <p>
 * The class provides the convenience method {@link #getDefault()} which gives a new instance of this class for each
 * invocation.
 * </p>
 * 
 * @author Sebastian Oerding
 */
public class DummyEventData extends EventData
{
    @Override
    public void parseValues(final String[] values)
    {
        super.parseValues(values);
    }

    /**
     * Returns a freshly instantiated DummyCustomData with the following values:
     * <ul>
     * <li>&quot;E&quot; as type code</li>
     * <li>&quot;eventName&quot; as name</li>
     * <li>&quot;3000&quot; as time</li>
     * <li>failed set to <code>true</code></li>
     * <li>&quot;a (user: 'testUser', output: '1234567890')&quot; as stacktrace</li>
     * <li>&quot;007&quot; as agent name</li>
     * </ul>
     * 
     * @return a new instance for each invocation with some hard coded values as described above
     */
    public static DummyEventData getDefault()
    {
        final DummyEventData returnValue = new DummyEventData();
        final String stackTrace = "a (user: 'testUser', output: '1234567890')";
        returnValue.parseValues(new String[]
            {
                "E", "eventName", "3000", "1", "true", stackTrace
            });
        returnValue.setAgentName("007");
        return returnValue;
    }
}
