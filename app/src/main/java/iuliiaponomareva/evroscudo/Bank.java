package iuliiaponomareva.evroscudo;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.text.Collator;
import java.util.Date;
import java.util.Locale;

import iuliiaponomareva.evroscudo.parsers.ExchangeRatesParser;

public class Bank implements Comparable<Bank>, Parcelable {
    boolean inMyCurrency;
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

    private Bank(Parcel in) {
        inMyCurrency = in.readByte() != 0;
        country = in.readString();
        currencyCode = in.readString();
        bankId = BankId.valueOf(in.readString());
        long ms = in.readLong();
        if (ms != -1L) {
            date = new Date(ms);
        } else {
            date = null;
        }
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

    String getCurrencyCode() {
        return currencyCode;
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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeByte((byte) (inMyCurrency ? 1 : 0));
        parcel.writeString(country);
        parcel.writeString(currencyCode);
        parcel.writeString(bankId.name());
        if (date != null) {
            parcel.writeLong(date.getTime());
        } else {
            parcel.writeLong(-1L);
        }
    }
}
