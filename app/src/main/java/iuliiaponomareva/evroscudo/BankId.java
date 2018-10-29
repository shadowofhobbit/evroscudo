package iuliiaponomareva.evroscudo;


public enum BankId {
    ECB("EUR"), CBR("RUB"), RBA("AUD"), CANADA("CAD"), UA("UAH"), KZ("KZT"), ISRAEL("ILS"), BY("BYR"),
    DK("DKK"), CZ("CZK"), KG("KGS"), TJ("TJS"), UK("GBP"), Norges("NOK"), Sweden("SEK");

    private String currencyCode;
    BankId(String code) {
        this.currencyCode = code;
    }
    public String getCurrencyCode() {
        return currencyCode;
    }
}
