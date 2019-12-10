package iuliiaponomareva.evroscudo;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import iuliiaponomareva.evroscudo.displayrates.DisplayRatesActivity;

public class Currency implements Parcelable {
    private String code;
    private Map<BankId, String> bankRates = new HashMap<>(DisplayRatesActivity.BANK_COUNT);
    private Map<BankId, Integer> nominals = new HashMap<>(DisplayRatesActivity.BANK_COUNT);

    public Currency(String code) {
        this.code = code;
    }

    protected Currency(Parcel in) {
        code = in.readString();
        int size = in.readInt();
        for(int i = 0; i < size; i++){
            BankId key = BankId.valueOf(in.readString());
            String value = in.readString();
            bankRates.put(key,value);
        }
        size = in.readInt();
        for(int i = 0; i < size; i++){
            BankId key = BankId.valueOf(in.readString());
            int value = in.readInt();
            nominals.put(key,value);
        }
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(code);
        dest.writeInt(bankRates.size());
        for (Map.Entry<BankId, String> entry : bankRates.entrySet()) {
            dest.writeString(entry.getKey().name());
            dest.writeString(entry.getValue());
        }
        dest.writeInt(nominals.size());
        for (Map.Entry<BankId, Integer> entry : nominals.entrySet()) {
            dest.writeString(entry.getKey().name());
            dest.writeInt(entry.getValue());
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Currency> CREATOR = new Creator<Currency>() {
        @Override
        public Currency createFromParcel(Parcel in) {
            return new Currency(in);
        }

        @Override
        public Currency[] newArray(int size) {
            return new Currency[size];
        }
    };

    public String getCode() {
        return code;
    }

    public void setBankRate(String rate, BankId bank) {
        bankRates.put(bank, rate);
    }

    public String getBankRate(BankId bank) {
        return bankRates.get(bank);
    }

    public Map<BankId, String> getBankRates() {
        return Collections.unmodifiableMap(bankRates);
    }

    public void updateRates(Map<BankId, String> rates) {
        bankRates.putAll(rates);
    }

    public void updateNominals(Map<BankId, Integer> newNominals) {
        nominals.putAll(newNominals);
    }

    public void setNominal(int nominal, BankId bank) {
        nominals.put(bank, nominal);
    }

    public int getNominal(BankId bank) {
        Integer nominal = nominals.get(bank);
        if (nominal == null)
            return 1;
        else
            return nominal;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Currency currency = (Currency) o;

        return (code != null ? code.equals(currency.code) : currency.code == null);

    }

    @Override
    public int hashCode() {
        return code != null ? code.hashCode() : 0;
    }

    public Map<BankId, Integer> getNominals() {
        return Collections.unmodifiableMap(nominals);
    }
}
