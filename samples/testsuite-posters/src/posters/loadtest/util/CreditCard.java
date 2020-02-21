package posters.loadtest.util;

/**
 * Provides a credit card.
 */
public class CreditCard
{
    /**
     * Credit card owner.
     */
    private String owner;

    /**
     * Credit card number.
     */
    private String number;

    /**
     * Month of credit card expiration.
     */
    private String expirationMonth;

    /**
     * Year of credit card expiration.
     */
    private String expirationYear;

    public CreditCard()
    {
        number = "4111111111111111";
        owner = "John Doe";
    }

    public CreditCard(final Account account)
    {
        number = "4111111111111111";
        owner = account.getFirstName() + " " + account.getLastName();
    }

    public String getOwner()
    {
        return owner;
    }

    public void setOwner(final String owner)
    {
        this.owner = owner;
    }

    public String getNumber()
    {
        return number;
    }

    public void setNumber(final String number)
    {
        this.number = number;
    }

    public String getExpirationMonth()
    {
        return expirationMonth;
    }

    public void setExpirationMonth(final String expirationMonth)
    {
        this.expirationMonth = expirationMonth;
    }

    public String getExpirationYear()
    {
        return expirationYear;
    }

    public void setExpirationYear(final String expirationYear)
    {
        this.expirationYear = expirationYear;
    }
}
