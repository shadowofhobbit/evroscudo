package iuliiaponomareva.evroscudo;


public enum Banks {
    ECB("EUR"), CBR("RUB"), RBA("AUD"), CANADA("CAD"), UA("UAH"), KZ("KZT"), ISRAEL("ILS"), BY("BYR"),
    DK("DKK"), CZ("CZK"), KG("KGS"), TJ("TJS");

    private String currencyCode;
    Banks(String code) {
        this.currencyCode = code;
    }
    public String getCurrencyCode() {
        return currencyCode;
    }
}
