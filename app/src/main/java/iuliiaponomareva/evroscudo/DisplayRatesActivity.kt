package iuliiaponomareva.evroscudo

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import iuliiaponomareva.evroscudo.parsers.*
import kotlinx.android.synthetic.main.activity_display_rates.*
import kotlinx.android.synthetic.main.toolbar.*
import java.text.SimpleDateFormat
import java.util.*

class DisplayRatesActivity : AppCompatActivity(), SwipeRefreshLayout.OnRefreshListener {
    private lateinit var ratesAdapter: ArrayAdapter<Currency>

    private lateinit var adapter1: ArrayAdapter<Bank>
    private lateinit var adapter2: ArrayAdapter<Bank>
    private val banks = HashMap<Banks, Bank>(BANK_COUNT)
    private lateinit var currenciesKeeper: CurrenciesKeeper

    val firstBank: Bank
        get() = spinner1.selectedItem as Bank

    val secondBank: Bank
        get() = spinner2.selectedItem as Bank

    private val isConnectedToNetwork: Boolean
        get() {
            val manager =
                applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val networkInfo = manager.activeNetworkInfo
            return networkInfo != null && networkInfo.isConnected
        }

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
        val listview = findViewById<View>(R.id.data_grid) as ListView
        ratesAdapter = CurrencyAdapter(this, R.id.list_item_layout)
        listview.adapter = ratesAdapter

        swipeRefreshLayout.setOnRefreshListener(this)
    }

    private fun setUpToolbar() {
        val toolbar = findViewById<View>(R.id.my_toolbar) as Toolbar
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
        val CBR = Bank(getString(R.string.cbr), Banks.CBR, CBRParser(), true)
        val ECB = Bank(getString(R.string.ecb), Banks.ECB, ECBParser(), false)
        val RBA = Bank(getString(R.string.rba), Banks.RBA, RBAParser(), false)
        val CANADA = Bank(getString(R.string.bank_of_canada), Banks.CANADA, CanadaParser(), false)
        val UKR = Bank(getString(R.string.ua), Banks.UA, UAParser(), true)
        val KZ = Bank(getString(R.string.kz), Banks.KZ, KZParser(), true)
        val IL = Bank(getString(R.string.IL), Banks.ISRAEL, IsraelParser(), true)
        val BY = Bank(getString(R.string.BY), Banks.BY, BYParser(), true)
        val DK = Bank(getString(R.string.DK), Banks.DK, DKParser(), true)
        val CZ = Bank(getString(R.string.czech), Banks.CZ, CZParser(), true)
        val KG = Bank(getString(R.string.KG), Banks.KG, KGParser(), true)
        val TJ = Bank(getString(R.string.TJ), Banks.TJ, TJParser(), true)

        banks[CBR.banks] = CBR
        banks[ECB.banks] = ECB
        banks[RBA.banks] = RBA
        banks[CANADA.banks] = CANADA
        banks[UKR.banks] = UKR
        banks[KZ.banks] = KZ
        banks[IL.banks] = IL
        banks[BY.banks] = BY
        banks[DK.banks] = DK
        banks[CZ.banks] = CZ
        banks[KG.banks] = KG
        banks[TJ.banks] = TJ
    }

    private fun setupSpinner(spinner: Spinner, adapter: ArrayAdapter<Bank>) {

        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        spinner.adapter = adapter
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, pos: Int, id: Long) {
                if (isConnectedToNetwork)
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
            editor.putString("bank1", (spinner1.selectedItem as Bank).banks.name)
        if (spinner2.selectedItem != null)
            editor.putString("bank2", (spinner2.selectedItem as Bank).banks.name)
        editor.apply()
    }

    private fun getSelectedBanks() {
        val preferences = getSharedPreferences("evroscudo", Context.MODE_PRIVATE)
        val bank = preferences.getString("bank1", adapter1.getItem(0)!!.banks.name)
        spinner1.setSelection(adapter1.getPosition(banks[Banks.valueOf(bank)]))
        val bank2 = preferences.getString("bank2", adapter2.getItem(0)!!.banks.name)
        spinner2.setSelection(adapter2.getPosition(banks[Banks.valueOf(bank2)]))

    }

    fun setDates(data: HashMap<String, Date>) {
        for (s in data.keys) {
            banks[Banks.valueOf(s)]?.setDate(data[s])
        }
        updateDates()
    }

    fun addCurrencies(currencies: Collection<Currency>) {
        ratesAdapter.clear()
        currenciesKeeper.addAll(currencies)
        ratesAdapter.addAll(currenciesKeeper.currencies)
        ratesAdapter.notifyDataSetChanged()
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
        if (isConnectedToNetwork) {
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
        ratesAdapter.clear()
        ratesAdapter.addAll(currenciesKeeper.currencies)
        ratesAdapter.notifyDataSetChanged()
        updateDates()
    }

    fun finishRefreshing() {
        swipeRefreshLayout.isRefreshing = false
    }

    fun showInfo(item: MenuItem) {
        InfoDialogFragment().show(supportFragmentManager, "info")
    }

    companion object {
        const val BANK_COUNT = 12
    }
}