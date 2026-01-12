package posters.functional.modules;
import static com.xceptance.xlt.api.engine.scripting.StaticScriptCommands.*;

/**
 * <p>Verifies the addresses and payment information in the order overview.</p>
 */
public class VerifyAddressAndPaymentOfOrder
{

    /**
     * <p>Verifies the addresses and payment information in the order overview.</p>
     *
     * @param billFullName
     * @param shipFullName
     * @param ccFullName
     * @param billCompany
     * @param shipCompany
     * @param billAddress
     * @param shipAddress
     * @param billCity
     * @param shipCity
     * @param state
     * @param zip
     * @param country
     * @param creditCard
     * @param expDateMonth
     * @param expDateYear
     */
    public static void execute(String billFullName, String shipFullName, String ccFullName, String billCompany, String shipCompany, String billAddress, String shipAddress, String billCity, String shipCity, String state, String zip, String country, String creditCard, String expDateMonth, String expDateYear)
    {
        // resolve any placeholder in the parameters
        billFullName = resolve(billFullName);
        shipFullName = resolve(shipFullName);
        ccFullName = resolve(ccFullName);
        billCompany = resolve(billCompany);
        shipCompany = resolve(shipCompany);
        billAddress = resolve(billAddress);
        shipAddress = resolve(shipAddress);
        billCity = resolve(billCity);
        shipCity = resolve(shipCity);
        state = resolve(state);
        zip = resolve(zip);
        country = resolve(country);
        creditCard = resolve(creditCard);
        expDateMonth = resolve(expDateMonth);
        expDateYear = resolve(expDateYear);
        // shipping address
        assertText("css=#shippingAddr .name", shipFullName);
        assertText("css=#shippingAddr .company", shipCompany);
        assertText("css=#shippingAddr .addressLine", shipAddress);
        assertText("css=#shippingAddr .company", shipCompany);
        assertText("css=#shippingAddr .city", shipCity);
        assertText("css=#shippingAddr .state", state);
        assertText("css=#shippingAddr .zip", " " + zip);
        assertText("css=#shippingAddr .country", country);
        // billing address
        assertText("css=#billingAddr .name", billFullName);
        assertText("css=#billingAddr .company", billCompany);
        assertText("css=#billingAddr .addressLine", billAddress);
        assertText("css=#billingAddr .company", billCompany);
        assertText("css=#billingAddr .city", billCity);
        assertText("css=#billingAddr .state", state);
        assertText("css=#billingAddr .zip", zip);
        assertText("css=#billingAddr .country", country);
        // payment
        assertText("css=#payment .name .value", ccFullName);
        assertText("css=#payment .cardNumber .value", creditCard);
        assertText("css=#payment .exp .month", expDateMonth);
        assertText("css=#payment .exp .year", expDateYear);

    }
}