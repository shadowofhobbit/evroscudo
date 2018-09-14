package iuliiaponomareva.evroscudo;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import androidx.loader.content.CursorLoader;


public class RatesLoader extends CursorLoader {
    private HashMap<String, Date> data = new HashMap<>();
    private Map<String, Currency> currencies = new HashMap<>();
    public RatesLoader(Context context) {
        super(context);
    }

    @Override
    public Cursor loadInBackground() {
        SQLiteOpenHelper helper = new RatesDBHelper(getContext());
        SQLiteDatabase database = null;
        try {
            database = helper.getReadableDatabase();
            readDates(database);
            readRates(database);
            readNominals(database);
            String query = "select * from "+ RatesContract.ExchangeRate.TABLE_NAME +" inner join "+
                    RatesContract.Nominals.TABLE_NAME;
            database.rawQuery(query, null);
           // keeper = new CurrenciesKeeper(currencies);

        } finally {

            if (database != null)
                database.close();

        }

        return super.loadInBackground();
    }


    private void readNominals(SQLiteDatabase database) {
        Cursor cursor;
        String[] projection = {
                RatesContract.Nominals.COLUMN_NAME_CURRENCY_CODE,
                RatesContract.Nominals.COLUMN_NAME_BANK,
                RatesContract.Nominals.COLUMN_NAME_NOMINAL
        };
        cursor = database.query(RatesContract.Nominals.TABLE_NAME,
                projection, null, null, null, null, null);
        cursor.moveToFirst();

        while (!cursor.isAfterLast()) {
            String code = cursor.getString(cursor.getColumnIndexOrThrow(RatesContract.Nominals.COLUMN_NAME_CURRENCY_CODE));
            String bank = cursor.getString(cursor.getColumnIndexOrThrow(RatesContract.Nominals.COLUMN_NAME_BANK));
            int nominal = cursor.getInt(cursor.getColumnIndexOrThrow(RatesContract.Nominals.COLUMN_NAME_NOMINAL));
            Currency currency = (currencies.get(code)!= null) ? currencies.get(code) : new Currency(code);
            currency.setNominal(nominal, Banks.valueOf(bank));
            currencies.put(code, currency);
            cursor.moveToNext();
        }

        cursor.close();

    }

    private void readDates(SQLiteDatabase database) {
        Cursor cursor;
        String[] projection = {
                RatesContract.Dates.COLUMN_NAME_BANK,
                RatesContract.Dates.COLUMN_NAME_DATE
        };
        cursor = database.query(RatesContract.Dates.TABLE_NAME,
                projection, null, null, null, null, null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            Date date = new Date(cursor.getLong(cursor.getColumnIndexOrThrow(RatesContract.Dates.COLUMN_NAME_DATE)));
            String bank = cursor.getString(cursor.getColumnIndexOrThrow(RatesContract.Dates.COLUMN_NAME_BANK));
            data.put(bank, date);
            cursor.moveToNext();
        }
        cursor.close();
    }

    private void readRates(SQLiteDatabase database) {
        Cursor cursor;
        String[] projection = {
                RatesContract.ExchangeRate.COLUMN_NAME_CURRENCY_CODE,
                RatesContract.ExchangeRate.COLUMN_NAME_BANK,
                RatesContract.ExchangeRate.COLUMN_NAME_RATE
        };
        cursor = database.query(RatesContract.ExchangeRate.TABLE_NAME,
                projection, null, null, null, null, null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            String code = cursor.getString(cursor.getColumnIndexOrThrow(RatesContract.ExchangeRate.COLUMN_NAME_CURRENCY_CODE));
            String bank = cursor.getString(cursor.getColumnIndexOrThrow(RatesContract.ExchangeRate.COLUMN_NAME_BANK));
            String rate = cursor.getString(cursor.getColumnIndexOrThrow(RatesContract.ExchangeRate.COLUMN_NAME_RATE));
            Currency currency = (currencies.get(code)!= null) ? currencies.get(code) : new Currency(code);
            currency.setBankRate(rate, Banks.valueOf(bank));
            currencies.put(code, currency);
            cursor.moveToNext();
        }
        cursor.close();
    }

}
