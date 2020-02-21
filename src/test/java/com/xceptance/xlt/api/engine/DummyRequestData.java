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
public class DummyRequestData extends RequestData
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
     * <li>&quot;requestName&quot; as name</li>
     * <li>&quot;4000&quot; as time</li>
     * <li>failed set to <code>true</code></li>
     * <li>&quot;007&quot; as agent name</li>
     * <li>100 sent bytes</li>
     * <li>200 received bytes</li>
     * <li>&quot;404&quot; as response status</li>
     * </ul>
     * 
     * @return a new instance for each invocation with some hard coded values as described above
     */
    public static RequestData getDefault()
    {
        final DummyRequestData returnValue = new DummyRequestData();
        // String stackTrace = "a (user: 'testUser', output: '1234567890')";
        returnValue.parseValues(new String[]
            {
                "R", "requestName", "4000", "1", "true", "100", "200", "404"
            });
        returnValue.setAgentName("007");
        return returnValue;
    }
}
