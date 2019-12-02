package iuliiaponomareva.evroscudo

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

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
}