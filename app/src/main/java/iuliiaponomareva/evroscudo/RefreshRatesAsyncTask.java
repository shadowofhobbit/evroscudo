package iuliiaponomareva.evroscudo;

import android.os.AsyncTask;

import java.util.ArrayList;
import java.util.List;

import iuliiaponomareva.evroscudo.parsers.ExchangeRatesParser;


class RefreshRatesAsyncTask extends AsyncTask<Bank, Void, List<Currency>> {
    private DisplayRatesActivity activity;


    public RefreshRatesAsyncTask(DisplayRatesActivity activity) {
        this.activity = activity;
    }

    @Override
    protected List<Currency> doInBackground(Bank... banks) {

        List<Currency> currencies = new ArrayList<>();
        for (Bank bank : banks) {
            ExchangeRatesParser parser = bank.getParser();
            currencies.addAll(parser.parse());
            bank.setDate(parser.getDate());
        }
        return currencies;
    }

    @Override
    protected void onPostExecute(List<Currency> currencies) {
        activity.addCurrencies(currencies);
        activity.updateDates();
        activity.finishRefreshing();

    }


}
