package util.xlt;

/**
 * An {@link AssertionError} type whose purpose is to be thrown intentionally within unit tests for a framework in order
 * to test the frameworks's behavior in case of exceptions/errors
 * 
 * @author Deniz Altin
 */
public class IntentionalError extends AssertionError
{
    private static final long serialVersionUID = 1L;

    public static final String DEFAULT_MESSAGE = "Intentional Error";

    public IntentionalError()
    {
        this(DEFAULT_MESSAGE);
    }

    public IntentionalError(final String detailMessage)
    {
        super(detailMessage);
    }
}
