package iuliiaponomareva.evroscudo;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import java.text.Collator;
import java.util.Date;
import java.util.Locale;

import iuliiaponomareva.evroscudo.parsers.ExchangeRatesParser;

public class Bank implements Comparable<Bank>, Parcelable {
    public boolean inMyCurrency;
    private ExchangeRatesParser parser;
    private String country;
    private String currencyCode;
    private Banks banks;
    private Date date;

    public Bank(String country, Banks code, ExchangeRatesParser parser, boolean inMyCurrency) {
        this.country = country;
        banks = code;
        this.currencyCode = code.getCurrencyCode();
        this.parser = parser;
        this.inMyCurrency = inMyCurrency;
    }

    protected Bank(Parcel in) {
        inMyCurrency = in.readByte() != 0;
        country = in.readString();
        currencyCode = in.readString();
        banks = Banks.valueOf(in.readString());
        date = new Date(in.readLong());
    }

    public static final Creator<Bank> CREATOR = new Creator<Bank>() {
        @Override
        public Bank createFromParcel(Parcel in) {
            return new Bank(in);
        }

        @Override
        public Bank[] newArray(int size) {
            return new Bank[size];
        }
    };

    public String getCurrencyCode() {
        return currencyCode;
    }

    @Override
    public String toString() {
        return country;
    }

    public ExchangeRatesParser getParser() {
        return parser;
    }

    public Banks getBanks() {
        return banks;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }


    @Override
    public int compareTo(@NonNull Bank another) {
        return Collator.getInstance(Locale.getDefault()).compare(country, another.country);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeByte((byte) (inMyCurrency ? 1 : 0));
        parcel.writeString(country);
        parcel.writeString(currencyCode);
        parcel.writeString(banks.name());
        parcel.writeLong(date.getTime());
    }
}
