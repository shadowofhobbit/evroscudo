package iuliiaponomareva.evroscudo;

import androidx.annotation.NonNull;

import java.text.Collator;
import java.util.Date;
import java.util.Locale;

import iuliiaponomareva.evroscudo.parsers.ExchangeRatesParser;

public class Bank implements Comparable<Bank> {
    private boolean inMyCurrency;
    private ExchangeRatesParser parser;
    private String country;
    private String currencyCode;
    private BankId bankId;
    private Date date;

    public Bank(String country, BankId code, ExchangeRatesParser parser, boolean inMyCurrency) {
        this.country = country;
        bankId = code;
        this.currencyCode = code.getCurrencyCode();
        this.parser = parser;
        this.inMyCurrency = inMyCurrency;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public boolean isInMyCurrency() {
        return inMyCurrency;
    }

    @Override
    public String toString() {
        return country;
    }

    public ExchangeRatesParser getParser() {
        return parser;
    }

    public BankId getBankId() {
        return bankId;
    }

    public Date getDate() {
        if (date == null) {
            date = parser.getDate();
        }
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }


    @Override
    public int compareTo(@NonNull Bank another) {
        return Collator.getInstance(Locale.getDefault()).compare(country, another.country);
    }

}
