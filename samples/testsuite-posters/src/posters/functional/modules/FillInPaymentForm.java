package posters.functional.modules;
import static com.xceptance.xlt.api.engine.scripting.StaticScriptCommands.*;

/**
 * <p>Fills in the payment information.</p>
 */
public class FillInPaymentForm
{

    /**
     * <p>Fills in the payment information.</p>
     *
     * @param number
     * @param name
     * @param month
     * @param year
     */
    public static void execute(String number, String name, String month, String year)
    {
        // resolve any placeholder in the parameters
        number = resolve(number);
        name = resolve(name);
        month = resolve(month);
        year = resolve(year);
        type("id=creditCardNumber", number);
        type("id=name", name);
        select("id=expirationDateMonth", "label=" + month);
        select("id=expirationDateYear", "label=" + year);

    }
}