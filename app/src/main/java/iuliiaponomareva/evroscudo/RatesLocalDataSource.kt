package iuliiaponomareva.evroscudo

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.util.*
import kotlin.collections.HashMap

class RatesLocalDataSource(private val context: Context) {

    fun save(
        currencies: List<Currency>,
        bank: Bank
    ) {
        val helper: SQLiteOpenHelper = RatesDBHelper(context)
        var database: SQLiteDatabase? = null
        try {
            database = helper.writableDatabase
            database.beginTransaction()

            if (bank.date != null) {
                database.delete(
                    RatesContract.Dates.TABLE_NAME,
                    RatesContract.Dates.COLUMN_NAME_BANK + " == ?",
                    arrayOf(bank.bankId.name)
                )
                val values = ContentValues()
                values.put(RatesContract.Dates.COLUMN_NAME_BANK, bank.bankId.name)
                values.put(RatesContract.Dates.COLUMN_NAME_DATE, bank.date.time)
                database.insert(RatesContract.Dates.TABLE_NAME, null, values)
            }

            database.delete(RatesContract.ExchangeRate.TABLE_NAME, RatesContract.ExchangeRate.COLUMN_NAME_BANK + " == ?",
                arrayOf(bank.bankId.name))
            database.delete(RatesContract.Nominals.TABLE_NAME,
                RatesContract.Nominals.COLUMN_NAME_BANK  + " == ?", arrayOf(bank.bankId.name))
            for (currency in currencies) {
                for ((key, value) in currency.bankRates) {
                    val values = ContentValues()
                    values.put(
                        RatesContract.ExchangeRate.COLUMN_NAME_CURRENCY_CODE,
                        currency.code
                    )
                    values.put(RatesContract.ExchangeRate.COLUMN_NAME_BANK, key.name)
                    values.put(RatesContract.ExchangeRate.COLUMN_NAME_RATE, value)
                    database.insert(RatesContract.ExchangeRate.TABLE_NAME, null, values)
                }
                for ((key, value) in currency.nominals) {
                    val values = ContentValues()
                    values.put(RatesContract.Nominals.COLUMN_NAME_CURRENCY_CODE, currency.code)
                    values.put(RatesContract.Nominals.COLUMN_NAME_BANK, key.name)
                    values.put(RatesContract.Nominals.COLUMN_NAME_NOMINAL, value)
                    database.insert(RatesContract.Nominals.TABLE_NAME, null, values)
                }
            }
            database.setTransactionSuccessful()
        } finally {
            if (database != null) {
                database.endTransaction()
                database.close()
            }
        }
    }

    fun load() {
        var keeper: CurrenciesKeeper? = null
        val currencies = HashMap<String, Currency>()
        val helper: SQLiteOpenHelper = RatesDBHelper(context)
        var database: SQLiteDatabase? = null
        try {
            database = helper.readableDatabase
            val data = HashMap<String, Date>()
            readDates(database, data)
            readRates(database, currencies)
            readNominals(database, currencies)
            keeper = CurrenciesKeeper(currencies)
        } finally {
            database?.close()
        }

    }

    private fun readNominals(database: SQLiteDatabase, currencies: MutableMap<String, Currency>) {
        val cursor: Cursor
        val projection = arrayOf(
            RatesContract.Nominals.COLUMN_NAME_CURRENCY_CODE,
            RatesContract.Nominals.COLUMN_NAME_BANK,
            RatesContract.Nominals.COLUMN_NAME_NOMINAL
        )
        cursor = database.query(
            RatesContract.Nominals.TABLE_NAME,
            projection, null, null, null, null, null
        )
        cursor.moveToFirst()
        while (!cursor.isAfterLast) {
            val code =
                cursor.getString(cursor.getColumnIndexOrThrow(RatesContract.Nominals.COLUMN_NAME_CURRENCY_CODE))
            val bank =
                cursor.getString(cursor.getColumnIndexOrThrow(RatesContract.Nominals.COLUMN_NAME_BANK))
            val nominal =
                cursor.getInt(cursor.getColumnIndexOrThrow(RatesContract.Nominals.COLUMN_NAME_NOMINAL))
            val currency: Currency = currencies[code] ?: Currency(code)
            currency.setNominal(nominal, BankId.valueOf(bank))
            currencies[code] = currency
            cursor.moveToNext()
        }
        cursor.close()
    }

    private fun readDates(database: SQLiteDatabase, data: MutableMap<String, Date>) {
        val cursor: Cursor
        val projection = arrayOf(
            RatesContract.Dates.COLUMN_NAME_BANK,
            RatesContract.Dates.COLUMN_NAME_DATE
        )
        cursor = database.query(
            RatesContract.Dates.TABLE_NAME,
            projection, null, null, null, null, null
        )
        cursor.moveToFirst()
        while (!cursor.isAfterLast) {
            val date =
                Date(cursor.getLong(cursor.getColumnIndexOrThrow(RatesContract.Dates.COLUMN_NAME_DATE)))
            val bank =
                cursor.getString(cursor.getColumnIndexOrThrow(RatesContract.Dates.COLUMN_NAME_BANK))
            data[bank] = date
            cursor.moveToNext()
        }
        cursor.close()
    }

    private fun readRates(database: SQLiteDatabase, currencies: MutableMap<String, Currency>) {
        val cursor: Cursor
        val projection = arrayOf(
            RatesContract.ExchangeRate.COLUMN_NAME_CURRENCY_CODE,
            RatesContract.ExchangeRate.COLUMN_NAME_BANK,
            RatesContract.ExchangeRate.COLUMN_NAME_RATE
        )
        cursor = database.query(
            RatesContract.ExchangeRate.TABLE_NAME,
            projection, null, null, null, null, null
        )
        cursor.moveToFirst()
        while (!cursor.isAfterLast) {
            val code =
                cursor.getString(cursor.getColumnIndexOrThrow(RatesContract.ExchangeRate.COLUMN_NAME_CURRENCY_CODE))
            val bank =
                cursor.getString(cursor.getColumnIndexOrThrow(RatesContract.ExchangeRate.COLUMN_NAME_BANK))
            val rate =
                cursor.getString(cursor.getColumnIndexOrThrow(RatesContract.ExchangeRate.COLUMN_NAME_RATE))
            val currency: Currency = currencies[code] ?: Currency(code)
            currency.setBankRate(rate, BankId.valueOf(bank))
            currencies[code] = currency
            cursor.moveToNext()
        }
        cursor.close()
    }
}