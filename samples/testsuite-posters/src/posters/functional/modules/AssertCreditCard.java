package posters.functional.modules;
import static com.xceptance.xlt.api.engine.scripting.StaticScriptCommands.*;

/**
 * TODO: Add class description
 */
public class AssertCreditCard
{

    /**
     * TODO: Add description
     *
     * @param name
     * @param number
     * @param month
     * @param year
     */
    public static void execute(String name, String number, String month, String year)
    {
        // resolve any placeholder in the parameters
        name = resolve(name);
        number = resolve(number);
        month = resolve(month);
        year = resolve(year);
        assertElementPresent("id=successMessage");
        assertText("css=.paymentName > strong:nth-child(1)", name);
        assertText("css=.paymentCardNumber", number);
        assertText("css=.expMonth", month);
        assertText("css=.expYear", year);

    }
}