package iuliiaponomareva.evroscudo.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


public class RatesDBHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "ExchangeRates.db";
    private static final String TEXT_TYPE = " TEXT";
    private static final String INTEGER_TYPE = " INTEGER";
    private static final String COMMA_SEP = ",";
    private static final String SQL_CREATE_RATES =
            "CREATE TABLE " + RatesContract.ExchangeRate.TABLE_NAME + " (" +
                    RatesContract.ExchangeRate._ID + " INTEGER PRIMARY KEY," +
                    RatesContract.ExchangeRate.COLUMN_NAME_BANK + TEXT_TYPE + COMMA_SEP +
                    RatesContract.ExchangeRate.COLUMN_NAME_CURRENCY_CODE + TEXT_TYPE + COMMA_SEP +
                    RatesContract.ExchangeRate.COLUMN_NAME_RATE + TEXT_TYPE +
            " )";
    private static final String SQL_CREATE_NOMINALS =
            "CREATE TABLE " + RatesContract.Nominals.TABLE_NAME + " (" +
                    RatesContract.Nominals._ID + " INTEGER PRIMARY KEY," +
                    RatesContract.Nominals.COLUMN_NAME_BANK + TEXT_TYPE + COMMA_SEP +
                    RatesContract.Nominals.COLUMN_NAME_CURRENCY_CODE + TEXT_TYPE + COMMA_SEP +
                    RatesContract.Nominals.COLUMN_NAME_NOMINAL + INTEGER_TYPE +
                    " )";
    private static final String SQL_CREATE_DATES = "CREATE TABLE " +
            RatesContract.Dates.TABLE_NAME + " (" +
            RatesContract.Dates._ID + " INTEGER PRIMARY KEY," +
            RatesContract.Dates.COLUMN_NAME_BANK + TEXT_TYPE + COMMA_SEP +
            RatesContract.Dates.COLUMN_NAME_DATE + INTEGER_TYPE + " )";

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + RatesContract.ExchangeRate.TABLE_NAME;
    private static final String SQL_DELETE_NOMINALS =
            "DROP TABLE IF EXISTS " + RatesContract.Nominals.TABLE_NAME;
    private static final String SQL_DELETE_DATES =
            "DROP TABLE IF EXISTS " + RatesContract.Dates.TABLE_NAME;

    public RatesDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_RATES);
        db.execSQL(SQL_CREATE_NOMINALS);
        db.execSQL(SQL_CREATE_DATES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_ENTRIES);
        db.execSQL(SQL_DELETE_NOMINALS);
        db.execSQL(SQL_DELETE_DATES);
        onCreate(db);

    }
}
