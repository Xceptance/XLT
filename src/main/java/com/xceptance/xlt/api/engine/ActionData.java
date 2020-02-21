package com.xceptance.xlt.api.engine;

/**
 * <p>
 * The {@link ActionData} class holds any data measured for an action. Typically, an action represents one
 * self-contained test step, which itself comprises one or more requests.
 * </p>
 * <p>
 * The values stored include not only the action's start and run time, but also an indicator whether or not the action
 * was executed successfully. Data gathered for the same type of action may be correlated via the name attribute.
 * </p>
 * <p style="color:green">
 * Note that {@link ActionData} objects have an "A" as their type code.
 * </p>
 * 
 * @see RequestData
 * @see TransactionData
 * @see CustomData
 * @see EventData
 * @author Jörg Werner (Xceptance Software Technologies GmbH)
 */
public class ActionData extends TimerData
{
    /**
     * The typecode.
     */
    private static final String TYPE_CODE = "A";

    /**
     * Creates a new ActionData object.
     */
    public ActionData()
    {
        this(null);
    }

    /**
     * Creates a new ActionData object and gives it the specified name. Furthermore, the start time attribute is set to
     * the current time.
     * 
     * @param name
     *            the action name
     */
    public ActionData(final String name)
    {
        super(name, TYPE_CODE);
    }
}
