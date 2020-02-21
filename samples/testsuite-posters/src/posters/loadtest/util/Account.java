package posters.loadtest.util;

import java.util.UUID;

import org.apache.commons.lang3.RandomStringUtils;

import com.xceptance.xlt.api.data.GeneralDataProvider;

/**
 * Provides a customer account.
 */
public class Account
{
    /**
     * the email address
     */
    private String email;

    /**
     * the first name
     */
    private String firstName;

    /**
     * the last name
     */
    private String lastName;

    /**
     * the password
     */
    private String password;

    /**
     * Create generic generated account.
     */
    public Account()
    {
        final GeneralDataProvider provider = GeneralDataProvider.getInstance();

        firstName = provider.getFirstName(false);
        lastName = provider.getLastName(false);
        email = RandomStringUtils.randomAlphanumeric(2) + UUID.randomUUID().toString().replaceAll("-", "").substring(0, 12) + "@varmail.de";
        password = RandomStringUtils.randomAlphanumeric(10);
    }

    public String getEmail()
    {
        return email;
    }

    public void setEmail(final String email)
    {
        this.email = email;
    }

    public String getFirstName()
    {
        return firstName;
    }

    public void setFirstName(final String firstName)
    {
        this.firstName = firstName;
    }

    public String getLastName()
    {
        return lastName;
    }

    public void setLastName(final String lastName)
    {
        this.lastName = lastName;
    }

    public String getPassword()
    {
        return password;
    }

    public void setPassword(final String password)
    {
        this.password = password;
    }
}
