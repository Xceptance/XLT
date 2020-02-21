package com.xceptance.xlt.api.engine;

/**
 * This class provides a dummy implementation of {@link ActionData} but makes {@link #parseValues(String[])} public to
 * allow modifications for testing purposes.
 * <p>
 * The class provides the convenience method {@link #getDefault()} which gives a new instance of this class for each
 * invocation.
 * </p>
 * 
 * @author Sebastian Oerding
 */
public class DummyActionData extends ActionData
{
    @Override
    public void parseValues(final String[] values)
    {
        super.parseValues(values);
    }

    /**
     * Returns a freshly instantiated DummyTransactionData with the following values:
     * <ul>
     * <li>&quot;A&quot; as type code</li>
     * <li>&quot;actionName&quot; as name</li>
     * <li>&quot;1000&quot; as time</li>
     * <li>failed set to <code>true</code></li>
     * <li>&quot;a (user: 'testUser', output: '1234567890')&quot; as stacktrace</li>
     * <li>&quot;007&quot; as agent name</li>
     * </ul>
     * 
     * @return a new instance for each invocation with some hard coded values as described above
     */
    public static DummyActionData getDefault()
    {
        final DummyActionData returnValue = new DummyActionData();
        final String stackTrace = "a (user: 'testUser', output: '1234567890')";
        returnValue.parseValues(new String[]
            {
                "A", "actionName", "1000", "1", "true", stackTrace
            });
        returnValue.setAgentName("007");
        return returnValue;
    }
}
