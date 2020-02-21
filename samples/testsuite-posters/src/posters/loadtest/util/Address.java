package posters.loadtest.util;

import com.xceptance.xlt.api.data.GeneralDataProvider;
import com.xceptance.xlt.api.util.XltRandom;

/**
 * Provides an address.
 */
public class Address
{
    /**
     * The company field of the address.
     */
    private final String company;

    /**
     * The address line.
     */
    private final String addressLine;

    /**
     * The city field of the address.
     */
    private final String city;

    /**
     * The state field of the address.
     */
    private final String state;

    /**
     * The country field of the address.
     */
    private final String country;

    /**
     * The ZIP code of the address.
     */
    private final String zip;

    /**
     * US state codes
     */
    private static final String[] usStateCodes = new String[]
        {
            "AL", "AK", "AZ", "AR", "CA", "CO", "CT", "DE", "FL", "GA", "HI", "ID", "IL", "IN", "IA", "KS", "KY", "LA", "ME", "MD", "MA",
            "MI", "MN", "MS", "MO", "MT", "NE", "NV", "NH", "NJ", "NM", "NY", "NC", "ND", "OH", "OK", "OR", "PA", "RI", "SC", "SD", "TN",
            "TX", "UT", "VT", "VA", "WA", "WV", "WI", "WY"
        };

    /**
     * Create generic generated address.
     */
    public Address()
    {
        final GeneralDataProvider provider = GeneralDataProvider.getInstance();

        company = provider.getCompany(false);
        addressLine = provider.getStreet(false);
        city = provider.getTown(true);
        state = usStateCodes[XltRandom.nextInt(usStateCodes.length)];
        country = "United States";
        zip = provider.getZip(5);
    }

    public String getCompany()
    {
        return company;
    }

    public String getAddressLine()
    {
        return addressLine;
    }

    public String getCity()
    {
        return city;
    }

    public String getState()
    {
        return state;
    }

    public String getCountry()
    {
        return country;
    }

    public String getZip()
    {
        return zip;
    }
}
