package iuliiaponomareva.evroscudo;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import iuliiaponomareva.evroscudo.parsers.ExchangeRatesParser;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class DownloadRatesService extends IntentService {
    private static final String ACTION_REFRESH = "iuliiaponomareva.evroscudo.action.FOO";


    // TODO: Rename parameters
    private static final String EXTRA_PARAM1 = "iuliiaponomareva.evroscudo.extra.PARAM1";


    public DownloadRatesService() {
        super("DownloadRatesService");
    }

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void startActionRefresh(Context context, Bank[] param1) {
        Intent intent = new Intent(context, DownloadRatesService.class);
        intent.setAction(ACTION_REFRESH);
        intent.putExtra(EXTRA_PARAM1, param1);
        context.startService(intent);
    }



    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_REFRESH.equals(action)) {
                final Parcelable[] param1 = intent.getParcelableArrayExtra(EXTRA_PARAM1);
                Bank[] banks = Arrays.copyOf(param1, param1.length, Bank[].class);
                handleActionRefresh(banks);
            }
        }
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void handleActionRefresh(Bank[] banks) {
        List<Currency> currencies = new ArrayList<>();
        for (Bank bank : banks) {
            ExchangeRatesParser parser = bank.getParser();
            currencies.addAll(parser.parse());
            bank.setDate(parser.getDate());
        }
        save(currencies, banks);

    }

    private void save(List<Currency> currencies, Bank... banks) {
        SQLiteOpenHelper helper = new RatesDBHelper(this);
        SQLiteDatabase database = null;
        try {
            database = helper.getWritableDatabase();
            database.beginTransaction();
            for (Bank bank : banks) {
                if (bank.getDate() != null) {
                    database.delete(RatesContract.Dates.TABLE_NAME,
                            RatesContract.Dates.COLUMN_NAME_BANK+" == ?",
                            new String[]{bank.getBanks().name()});
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
            database.setTransactionSuccessful();
        } finally {
            if (database != null) {
                database.endTransaction();
                database.close();
            }
        }
    }

    private void notifyActivity() {
        //Intent intent = new Intent(ACTION_BROADCAST_CHANNELS);
        //intent.putExtra(NEW_CHANNELS, newChannels.toArray(new Channel[newChannels.size()]));
        //LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }
}
