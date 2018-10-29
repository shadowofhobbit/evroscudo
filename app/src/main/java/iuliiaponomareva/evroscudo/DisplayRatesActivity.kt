package iuliiaponomareva.evroscudo

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import iuliiaponomareva.evroscudo.parsers.*
import iuliiaponomareva.evroscudo.settings.SettingsActivity
import kotlinx.android.synthetic.main.activity_display_rates.*
import kotlinx.android.synthetic.main.toolbar.*
import java.text.SimpleDateFormat
import java.util.*

class DisplayRatesActivity : AppCompatActivity(), SwipeRefreshLayout.OnRefreshListener {
    private lateinit var ratesAdapter: CurrencyAdapter

    private lateinit var adapter1: ArrayAdapter<Bank>
    private lateinit var adapter2: ArrayAdapter<Bank>
    private val banks = HashMap<BankId, Bank>(BANK_COUNT)
    private lateinit var currenciesKeeper: CurrenciesKeeper

    val firstBank: Bank
        get() = spinner1.selectedItem as Bank

    val secondBank: Bank
        get() = spinner2.selectedItem as Bank

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_display_rates)
        createBanks()
        currenciesKeeper = CurrenciesKeeper()
        setUpToolbar()
        setUpListOfRates()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    private fun setUpListOfRates() {
        ratesView.layoutManager = LinearLayoutManager(this)
        ratesAdapter = CurrencyAdapter(this)
        ratesView.adapter = ratesAdapter
        ratesView.setHasFixedSize(true)
        swipeRefreshLayout.setOnRefreshListener(this)
    }

    private fun setUpToolbar() {
        setSupportActionBar(toolbar)
        val banksList = ArrayList(banks.values)
        banksList.sort()
        adapter1 = ArrayAdapter(
            this, R.layout.my_spinner_item,
            banksList
        )
        setupSpinner(spinner1, adapter1)
        adapter2 = ArrayAdapter(
            this, R.layout.my_spinner_item,
            banksList
        )
        setupSpinner(spinner2, adapter2)
    }

    private fun createBanks() {
        val CBR = Bank(getString(R.string.cbr), BankId.CBR, CBRParser(), true)
        val ECB = Bank(getString(R.string.ecb), BankId.ECB, ECBParser(), false)
        val RBA = Bank(getString(R.string.rba), BankId.RBA, RBAParser(), false)
        val CANADA = Bank(getString(R.string.bank_of_canada), BankId.CANADA, CanadaParser(), false)
        val UKR = Bank(getString(R.string.ua), BankId.UA, UAParser(), true)
        val KZ = Bank(getString(R.string.kz), BankId.KZ, KZParser(), true)
        val IL = Bank(getString(R.string.IL), BankId.ISRAEL, IsraelParser(), true)
        val BY = Bank(getString(R.string.BY), BankId.BY, BYParser(), true)
        val DK = Bank(getString(R.string.DK), BankId.DK, DKParser(), true)
        val CZ = Bank(getString(R.string.czech), BankId.CZ, CZParser(), true)
        val KG = Bank(getString(R.string.KG), BankId.KG, KGParser(), true)
        val TJ = Bank(getString(R.string.TJ), BankId.TJ, TJParser(), true)
        val NO = Bank(getString(R.string.norges_bank), BankId.Norges, NorgesParser(), true)
        val SE = Bank(getString(R.string.sweden), BankId.Sweden, SwedenParser(), true)
        val UK = Bank(getString(R.string.uk), BankId.UK, EnglandParser(), false)

        banks[CBR.bankId] = CBR
        banks[ECB.bankId] = ECB
        banks[RBA.bankId] = RBA
        banks[CANADA.bankId] = CANADA
        banks[UKR.bankId] = UKR
        banks[KZ.bankId] = KZ
        banks[IL.bankId] = IL
        banks[BY.bankId] = BY
        banks[DK.bankId] = DK
        banks[CZ.bankId] = CZ
        banks[KG.bankId] = KG
        banks[TJ.bankId] = TJ
        banks[NO.bankId] = NO
        banks[SE.bankId] = SE
        banks[UK.bankId] = UK
    }

    private fun setupSpinner(spinner: Spinner, adapter: ArrayAdapter<Bank>) {

        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        spinner.adapter = adapter
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, pos: Int, id: Long) {
                if (isConnectedToNetwork(this@DisplayRatesActivity))
                    RefreshRatesAsyncTask(this@DisplayRatesActivity).execute(adapter.getItem(pos))
                else {
                    getRates()
                    Toast.makeText(
                        this@DisplayRatesActivity,
                        R.string.no_internet_connection, Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {

            }
        }
    }

    private fun formatDate(date: Date?): String {
        return if (date != null)
            SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(date)
        else
            ""
    }

    override fun onStart() {
        super.onStart()
        LoadRatesTask(this).execute()
    }

    override fun onStop() {
        super.onStop()
        val saveRatesIntent = Intent(this, SaveRatesIntentService::class.java)
        saveRatesIntent.action = SaveRatesIntentService.ACTION_SAVE
        val currencies = currenciesKeeper.currencies
        saveRatesIntent.putExtra(
            SaveRatesIntentService.EXTRA_CURRENCIES,
            currencies.toTypedArray()
        )
        saveRatesIntent.putExtra(
            SaveRatesIntentService.EXTRA_BANK,
            arrayOf(firstBank, secondBank)
        )
        startService(saveRatesIntent)

    }

    override fun onPause() {
        super.onPause()
        saveSelectedBanks()
    }

    override fun onResume() {
        super.onResume()
        getSelectedBanks()
    }


    private fun saveSelectedBanks() {
        val preferences = getSharedPreferences("evroscudo", Context.MODE_PRIVATE)
        val editor = preferences.edit()
        if (spinner1.selectedItem != null)
            editor.putString("bank1", (spinner1.selectedItem as Bank).bankId.name)
        if (spinner2.selectedItem != null)
            editor.putString("bank2", (spinner2.selectedItem as Bank).bankId.name)
        editor.apply()
    }

    private fun getSelectedBanks() {
        val preferences = getSharedPreferences("evroscudo", Context.MODE_PRIVATE)
        val bank = preferences.getString("bank1", adapter1.getItem(0)!!.bankId.name)
        spinner1.setSelection(adapter1.getPosition(banks[BankId.valueOf(bank)]))
        val bank2 = preferences.getString("bank2", adapter2.getItem(0)!!.bankId.name)
        spinner2.setSelection(adapter2.getPosition(banks[BankId.valueOf(bank2)]))

    }

    fun setDates(data: HashMap<String, Date>) {
        for (s in data.keys) {
            banks[BankId.valueOf(s)]?.setDate(data[s])
        }
        updateDates()
    }

    fun addCurrencies(currencies: Collection<Currency>) {
        currenciesKeeper.addAll(currencies)
        ratesAdapter.set(currenciesKeeper.currencies)
    }

    fun updateDates() {
        val date1 = firstBank.date
        val date2 = secondBank.date
        dateView1.text = formatDate(date1)
        dateView2.text = formatDate(date2)
    }

    fun setKeeper(keeper: CurrenciesKeeper) {
        this.currenciesKeeper = keeper
    }

    override fun onRefresh() {
        if (isConnectedToNetwork(this)) {
            RefreshRatesAsyncTask(this@DisplayRatesActivity).execute(firstBank, secondBank)
        } else {
            Toast.makeText(
                this@DisplayRatesActivity,
                R.string.no_internet_connection, Toast.LENGTH_SHORT
            ).show()
            finishRefreshing()
        }
    }

    fun getRates() {
        ratesAdapter.set(currenciesKeeper.currencies)
        updateDates()
    }

    fun finishRefreshing() {
        swipeRefreshLayout.isRefreshing = false
    }

    fun showInfo(item: MenuItem) {
        InfoDialogFragment().show(supportFragmentManager, "info")
    }

    fun showSettings(item: MenuItem) {
        val intent = Intent(this, SettingsActivity::class.java)
        startActivity(intent)
    }

    companion object {
        const val BANK_COUNT = 15
    }
}

class RatesViewModel : ViewModel() {
    private lateinit var rates: MutableLiveData<List<Currency>>
    fun getData() {

    }

}