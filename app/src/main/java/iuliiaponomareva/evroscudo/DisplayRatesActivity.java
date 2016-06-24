package iuliiaponomareva.evroscudo;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import iuliiaponomareva.evroscudo.parsers.BYParser;
import iuliiaponomareva.evroscudo.parsers.CBRParser;
import iuliiaponomareva.evroscudo.parsers.CZParser;
import iuliiaponomareva.evroscudo.parsers.CanadaParser;
import iuliiaponomareva.evroscudo.parsers.DKParser;
import iuliiaponomareva.evroscudo.parsers.ECBParser;
import iuliiaponomareva.evroscudo.parsers.IsraelParser;
import iuliiaponomareva.evroscudo.parsers.KGParser;
import iuliiaponomareva.evroscudo.parsers.KZParser;
import iuliiaponomareva.evroscudo.parsers.RBAParser;
import iuliiaponomareva.evroscudo.parsers.TJParser;
import iuliiaponomareva.evroscudo.parsers.UAParser;

public class DisplayRatesActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {
    public static final int BANK_COUNT = 12;
    private ArrayAdapter<Currency> ratesAdapter;
    private Spinner spinner1;
    private Spinner spinner2;
    private ArrayAdapter<Bank> adapter1;
    private ArrayAdapter<Bank> adapter2;
    private Map<Banks, Bank> banks;
    private CurrenciesKeeper currenciesKeeper;
    private TextView dateView1;
    private TextView dateView2;
    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_rates);
        createBanks();
        currenciesKeeper = new CurrenciesKeeper();
        setUpToolbar();
        setUpListOfRates();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    private void setUpListOfRates() {
        ListView listview = (ListView) findViewById(R.id.data_grid);
        ratesAdapter = new CurrencyAdapter(this, R.id.list_item_layout);
        listview.setAdapter(ratesAdapter);
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_to_refresh);
        swipeRefreshLayout.setOnRefreshListener(this);
    }

    private void setUpToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);

        dateView1 = ((TextView)toolbar.findViewById(R.id.date1));
        dateView2 = ((TextView)toolbar.findViewById(R.id.date2));
        spinner1 = (Spinner) toolbar.findViewById(R.id.banks_spinner);
        spinner2 = (Spinner) toolbar.findViewById(R.id.banks_spinner_2);
        List<Bank> banksList = new ArrayList<>(banks.values());
        Collections.sort(banksList);
        adapter1 = new ArrayAdapter<>(this, R.layout.my_spinner_item,
                banksList);
        setupSpinner(spinner1, adapter1);
        adapter2 = new ArrayAdapter<>(this, R.layout.my_spinner_item,
                banksList);
        setupSpinner(spinner2, adapter2);
    }

    private void createBanks() {
        Bank CBR = new Bank(getString(R.string.cbr), Banks.CBR, new CBRParser(), true);
        Bank ECB = new Bank(getString(R.string.ecb), Banks.ECB, new ECBParser(), false);
        Bank RBA = new Bank(getString(R.string.rba), Banks.RBA, new RBAParser(), false);
        Bank CANADA = new Bank(getString(R.string.bank_of_canada), Banks.CANADA, new CanadaParser(), false);
        Bank UKR = new Bank(getString(R.string.ua), Banks.UA, new UAParser(), true);
        Bank KZ = new Bank(getString(R.string.kz), Banks.KZ, new KZParser(), true);
        Bank IL = new Bank(getString(R.string.IL), Banks.ISRAEL, new IsraelParser(), true);
        Bank BY= new Bank(getString(R.string.BY), Banks.BY, new BYParser(), true);
        Bank DK = new Bank(getString(R.string.DK), Banks.DK, new DKParser(), true);
        Bank CZ = new Bank(getString(R.string.czech), Banks.CZ, new CZParser(), true);
        Bank KG = new Bank(getString(R.string.KG), Banks.KG, new KGParser(), true);
        Bank TJ = new Bank(getString(R.string.TJ), Banks.TJ, new TJParser(), true);
        banks = new HashMap<>(BANK_COUNT);
        banks.put(CBR.getBanks(), CBR);
        banks.put(ECB.getBanks(), ECB);
        banks.put(RBA.getBanks(), RBA);
        banks.put(CANADA.getBanks(), CANADA);
        banks.put(UKR.getBanks(), UKR);
        banks.put(KZ.getBanks(), KZ);
        banks.put(IL.getBanks(), IL);
        banks.put(BY.getBanks(), BY);
        banks.put(DK.getBanks(), DK);
        banks.put(CZ.getBanks(), CZ);
        banks.put(KG.getBanks(), KG);
        banks.put(TJ.getBanks(), TJ);
    }

    private void setupSpinner(Spinner spinner, ArrayAdapter<Bank> adapter) {

        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        spinner.setAdapter(adapter);
        final ArrayAdapter<Bank> finalAdapter = adapter;
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                if (isConnectedToNetwork())
                    new RefreshRatesAsyncTask(DisplayRatesActivity.this).execute(finalAdapter.getItem(pos));
                else {
                    getRates();
                    Toast.makeText(DisplayRatesActivity.this,
                            R.string.no_internet_connection, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private String formatDate(Date date) {
        if (date != null)
            return new SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(date);
        else
            return "";
    }

    @Override
    protected void onStart() {
        super.onStart();
        new LoadRatesTask(this).execute();
    }

    @Override
    protected void onStop() {
        super.onStop();
        Intent saveRatesIntent = new Intent(this, SaveRatesIntentService.class);
        saveRatesIntent.setAction(SaveRatesIntentService.ACTION_SAVE);
        Collection<Currency> currencies = currenciesKeeper.getCurrencies();
        saveRatesIntent.putExtra(SaveRatesIntentService.EXTRA_CURRENCIES,
                currencies.toArray(new Currency[currencies.size()]));
        saveRatesIntent.putExtra(SaveRatesIntentService.EXTRA_BANK,
                new Bank[]{getFirstBank(), getSecondBank()});
        startService(saveRatesIntent);

    }

    @Override
    protected void onPause() {
        super.onPause();
        saveSelectedBanks();
    }

    @Override
    protected void onResume() {
        super.onResume();
        getSelectedBanks();
    }


    private void saveSelectedBanks() {
        SharedPreferences preferences = getSharedPreferences("evroscudo", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        if (spinner1.getSelectedItem() != null)
            editor.putString("bank1", ((Bank) spinner1.getSelectedItem()).getBanks().name());
        if (spinner2.getSelectedItem() != null)
            editor.putString("bank2", ((Bank) spinner2.getSelectedItem()).getBanks().name());
        editor.apply();
    }

    private void getSelectedBanks() {
        SharedPreferences preferences = getSharedPreferences("evroscudo", MODE_PRIVATE);
        String bank = preferences.getString("bank1", adapter1.getItem(0).getBanks().name());
        spinner1.setSelection(adapter1.getPosition(banks.get(Banks.valueOf(bank))));
        String bank2 = preferences.getString("bank2", adapter2.getItem(0).getBanks().name());
        spinner2.setSelection(adapter2.getPosition(banks.get(Banks.valueOf(bank2))));

    }

    Bank getFirstBank() {
        return (Bank)spinner1.getSelectedItem();
    }

    Bank getSecondBank() {
        return (Bank)spinner2.getSelectedItem();
    }

    private boolean isConnectedToNetwork() {
        ConnectivityManager manager =
                (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    public void setDates(HashMap<String, Date> data) {
        for (String s : data.keySet()) {
            banks.get(Banks.valueOf(s)).setDate(data.get(s));
        }
        updateDates();
    }

    void addCurrencies(Collection<Currency> currencies) {
        ratesAdapter.clear();
        currenciesKeeper.addAll(currencies);
        ratesAdapter.addAll(currenciesKeeper.getCurrencies());
        ratesAdapter.notifyDataSetChanged();
    }

    public void updateDates() {
        Date date1 = getFirstBank().getDate();
        Date date2 = getSecondBank().getDate();
        dateView1.setText(formatDate(date1));
        dateView2.setText(formatDate(date2));
    }

    public void setKeeper(CurrenciesKeeper keeper) {
        this.currenciesKeeper = keeper;
    }

    @Override
    public void onRefresh() {
        if (isConnectedToNetwork()) {
            new RefreshRatesAsyncTask(DisplayRatesActivity.this).execute(getFirstBank(), getSecondBank());
        }
        else {
            Toast.makeText(DisplayRatesActivity.this,
                    R.string.no_internet_connection, Toast.LENGTH_SHORT).show();
            finishRefreshing();
        }
    }

    void getRates() {
        ratesAdapter.clear();
        ratesAdapter.addAll(currenciesKeeper.getCurrencies());
        ratesAdapter.notifyDataSetChanged();
        updateDates();
    }

    public void finishRefreshing() {
        swipeRefreshLayout.setRefreshing(false);
    }

    public void showInfo(MenuItem item) {
        new InfoDialogFragment().show(getFragmentManager(), "info");
    }
}