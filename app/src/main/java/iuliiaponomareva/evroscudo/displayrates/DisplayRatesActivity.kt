package iuliiaponomareva.evroscudo.displayrates

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
import androidx.recyclerview.widget.LinearLayoutManager
import iuliiaponomareva.evroscudo.*
import iuliiaponomareva.evroscudo.Currency
import iuliiaponomareva.evroscudo.info.InfoDialogFragment
import iuliiaponomareva.evroscudo.settings.SettingsActivity
import kotlinx.android.synthetic.main.activity_display_rates.*
import kotlinx.android.synthetic.main.toolbar.*
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class DisplayRatesActivity : AppCompatActivity(), DisplayRatesContract.View {
    private lateinit var ratesAdapter: CurrencyAdapter

    private lateinit var adapter1: ArrayAdapter<Bank>
    private lateinit var adapter2: ArrayAdapter<Bank>
    @Inject lateinit var banks: HashMap<BankId, Bank>
    @Inject lateinit var presenter: DisplayRatesContract.Presenter


    val firstBank: Bank
        get() = spinner1.selectedItem as Bank

    val secondBank: Bank
        get() = spinner2.selectedItem as Bank

    override fun onCreate(savedInstanceState: Bundle?) {
        (application as App).appComponent.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_display_rates)
        setUpToolbar(savedInstanceState == null)
        setUpListOfRates()
        presenter.attachView(this)
    }

    override fun startRefreshing() {
        swipeRefreshLayout.isRefreshing = true
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    private fun setUpListOfRates() {
        ratesView.layoutManager = LinearLayoutManager(this)
        ratesAdapter =
            CurrencyAdapter(this)
        ratesView.adapter = ratesAdapter
        ratesView.setHasFixedSize(true)
        swipeRefreshLayout.setOnRefreshListener{presenter.onRefresh(firstBank, secondBank)}
    }

    private fun setUpToolbar(handleSelectionNow: Boolean) {
        setSupportActionBar(toolbar)
        val banksList = ArrayList(banks.values)
        banksList.sort()
        adapter1 = ArrayAdapter(
            this, R.layout.my_spinner_item,
            banksList
        )
        setupSpinner(spinner1, adapter1, handleSelectionNow)
        adapter2 = ArrayAdapter(
            this, R.layout.my_spinner_item,
            banksList
        )
        setupSpinner(spinner2, adapter2, handleSelectionNow)
    }

    private fun setupSpinner(spinner: Spinner, adapter: ArrayAdapter<Bank>, attachListener: Boolean = false) {

        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        spinner.adapter = adapter
        if (attachListener) {
            spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>,
                    view: View?,
                    pos: Int,
                    id: Long
                ) {

                    presenter.onBankSelected(adapter.getItem(pos) as Bank)
                }

                override fun onNothingSelected(parent: AdapterView<*>) {

                }
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
        presenter.enterView()
    }

    override fun onStop() {
        super.onStop()
        presenter.leaveView()
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
        val bank = preferences.getString("bank1", BankId.ECB.name)
        spinner1.setSelection(adapter1.getPosition(banks[BankId.valueOf(bank!!)]), false)

        val bank2 = preferences.getString("bank2", BankId.CBR.name)
        spinner2.setSelection(adapter2.getPosition(banks[BankId.valueOf(bank2!!)]), false)

        spinner1.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {
                presenter.onBankSelected(adapter1.getItem(pos) as Bank)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {

            }
        }
        spinner2.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {
                presenter.onBankSelected(adapter2.getItem(pos) as Bank)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {

            }
        }
    }

    override fun setDates(data: Map<BankId, Date>) {
        for (s in data.keys) {
            banks[s]?.date = data[s]
        }
        updateDates()
    }

    override fun displayCurrencies(currencies: Collection<Currency>) {
        ratesAdapter.set(currencies)
    }

    private fun updateDates() {
        val date1 = firstBank.date
        val date2 = secondBank.date
        dateView1.text = formatDate(date1)
        dateView2.text = formatDate(date2)
    }

    override fun isConnectedToNetwork(): Boolean = isConnectedToNetwork(this)

    override fun displayNoInternet() {
        Toast.makeText(
            this@DisplayRatesActivity,
            R.string.no_internet_connection, Toast.LENGTH_SHORT
        ).show()
    }

    override fun finishRefreshing() {
        swipeRefreshLayout.isRefreshing = false
    }

    override fun displayInfo() {
        InfoDialogFragment()
            .show(supportFragmentManager, "info")
    }

    override fun displaySettings() {
        val intent = Intent(this, SettingsActivity::class.java)
        startActivity(intent)
    }

    override fun displayError() {
        Toast.makeText(
            this@DisplayRatesActivity,
            getString(R.string.error), Toast.LENGTH_SHORT
        ).show()
    }

    fun showInfo(item: MenuItem) {
        InfoDialogFragment()
            .show(supportFragmentManager, "info")
    }

    fun showSettings(item: MenuItem) {
        val intent = Intent(this, SettingsActivity::class.java)
        startActivity(intent)
    }

    companion object {
        const val BANK_COUNT = 15
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.detachView()
    }

    override fun displayData(bank: Bank, data: RatesData) {
        bank.date = data.date
        displayCurrencies(data.currencies)
        updateDates()
    }
}

data class RatesData(val currencies: MutableList<Currency>, val date: Date?)

