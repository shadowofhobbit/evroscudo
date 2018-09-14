package iuliiaponomareva.evroscudo;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Parcelable;

import java.util.Arrays;
import java.util.Map;



public class SaveRatesIntentService extends IntentService {

    public static final String ACTION_SAVE = "iuliiaponomareva.evroscudo.action.SAVE";

    public static final String EXTRA_CURRENCIES = "iuliiaponomareva.evroscudo.extra.CURRENCIES";
    public static final String EXTRA_BANK = "iuliiaponomareva.evroscudo.extra.BANK";

    public SaveRatesIntentService() {
        super("SaveRatesIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_SAVE.equals(action)) {
                Parcelable[] parcelables = intent.getParcelableArrayExtra(EXTRA_CURRENCIES);
                final Currency[] param1 = Arrays.copyOf(parcelables, parcelables.length, Currency[].class);
                Parcelable[] parcelables2 = intent.getParcelableArrayExtra(EXTRA_BANK);
                final Bank[] param2 = Arrays.copyOf(parcelables2, parcelables2.length, Bank[].class);
                handleActionSave(param1, param2);
            }
        }
    }


    private void handleActionSave(Currency[] currencies, Bank... banks) {
        SQLiteOpenHelper helper = new RatesDBHelper(this);
        SQLiteDatabase database = null;
        try {
            database = helper.getWritableDatabase();
            database.delete(RatesContract.Dates.TABLE_NAME, null, null);
            for (Bank bank : banks) {
                if (bank.getDate() != null) {
                    ContentValues values = new ContentValues();
                    values.put(RatesContract.Dates.COLUMN_NAME_BANK, bank.getBanks().name());
                    values.put(RatesContract.Dates.COLUMN_NAME_DATE, bank.getDate().getTime());
                    database.insert(RatesContract.Dates.TABLE_NAME, null, values);
                }

            }
            database.delete(RatesContract.ExchangeRate.TABLE_NAME, null, null);
            database.delete(RatesContract.Nominals.TABLE_NAME, null, null);
            for (Currency currency : currencies) {
                for (Map.Entry<Banks, String> entry : currency.getBankRates().entrySet()) {
                    ContentValues values = new ContentValues();
                    values.put(RatesContract.ExchangeRate.COLUMN_NAME_CURRENCY_CODE, currency.getCode());
                    values.put(RatesContract.ExchangeRate.COLUMN_NAME_BANK, entry.getKey().name());
                    values.put(RatesContract.ExchangeRate.COLUMN_NAME_RATE, entry.getValue());
                    database.insert(RatesContract.ExchangeRate.TABLE_NAME, null, values);
                }

                for (Map.Entry<Banks, Integer> entry : currency.getNominals().entrySet()) {
                    ContentValues values = new ContentValues();
                    values.put(RatesContract.Nominals.COLUMN_NAME_CURRENCY_CODE, currency.getCode());
                    values.put(RatesContract.Nominals.COLUMN_NAME_BANK, entry.getKey().name());
                    values.put(RatesContract.Nominals.COLUMN_NAME_NOMINAL, entry.getValue());
                    database.insert(RatesContract.Nominals.TABLE_NAME, null, values);
                }
            }

        } finally {
            if (database != null) {
                database.close();
            }
        }
    }


}
