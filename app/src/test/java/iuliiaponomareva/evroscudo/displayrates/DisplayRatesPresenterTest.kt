package iuliiaponomareva.evroscudo.displayrates

import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.schedulers.TestScheduler
import iuliiaponomareva.evroscudo.Bank
import iuliiaponomareva.evroscudo.BankId
import iuliiaponomareva.evroscudo.Currency
import iuliiaponomareva.evroscudo.parsers.CBRParser
import iuliiaponomareva.evroscudo.parsers.ECBParser
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.Mockito.verify
import org.mockito.junit.MockitoJUnitRunner
import java.io.IOException
import java.util.*

@RunWith(MockitoJUnitRunner::class)
class DisplayRatesPresenterTest {
    @Mock
    private lateinit var model: DisplayRatesContract.Model

    @Mock
    private lateinit var view: DisplayRatesContract.View

    private lateinit var presenter: DisplayRatesPresenter

    private lateinit var testScheduler: TestScheduler

    private lateinit var bank: Bank

    @Before
    fun setUp() {
        testScheduler = TestScheduler()
        presenter = DisplayRatesPresenter(model, testScheduler)
        presenter.attachView(view)
        bank = Bank("РФ", BankId.CBR, CBRParser(), true)
    }


    @Test
    fun onBankSelectedDataReceived() {
        `when`(view.isConnectedToNetwork()).thenReturn(true)
        val ratesData = RatesData(
            currencies = mutableListOf(Currency("EUR")),
            date = null
        )
        `when`(model.refreshRates(bank)).thenReturn(Observable.just(ratesData))
        presenter.onBankSelected(bank)
        testScheduler.triggerActions()
        verify(model).refreshRates(bank)
        verify(view).finishRefreshing()
        verify(view).displayData(bank, ratesData)
    }

    @Test
    fun onBankSelectedNoData() {
        `when`(view.isConnectedToNetwork()).thenReturn(true)
        val ratesData = RatesData(
            currencies = mutableListOf(),
            date = null
        )
        `when`(model.refreshRates(bank)).thenReturn(Observable.just(ratesData))
        presenter.onBankSelected(bank)
        testScheduler.triggerActions()
        verify(model).refreshRates(bank)
        verify(view).finishRefreshing()
        verify(view).displayError()
    }

    @Test
    fun onBankSelectedError() {
        `when`(view.isConnectedToNetwork()).thenReturn(true)
        `when`(model.refreshRates(bank)).thenReturn(Observable.error(RuntimeException()))
        presenter.onBankSelected(bank)
        testScheduler.triggerActions()
        verify(model).refreshRates(bank)
        verify(view).displayError()
        verify(view).finishRefreshing()
    }

    @Test
    fun onBankSelectedNoInternet() {
        `when`(view.isConnectedToNetwork()).thenReturn(false)
        presenter.onBankSelected(bank)
        verify(view).displayNoInternet()
        val ratesData = verify(model).getRatesFromCache(bank)
        verify(view).displayData(bank, ratesData)
    }

    @Test
    fun enterView() {
        val currencies = mutableListOf(Currency("EUR"))
        `when`(model.getRatesFromDb()).thenReturn(Single.just(currencies))
        val dates = hashMapOf(Pair(BankId.CBR, Date()))
        `when`(model.getDatesFromDb()).thenReturn(Single.just(dates))
        presenter.enterView()
        testScheduler.triggerActions()
        verify(view).displayCurrencies(currencies)
        verify(view).setDates(dates)
    }

    @Test
    fun enterViewLoadingError() {
        val currencies = mutableListOf(Currency("EUR"))
        `when`(model.getRatesFromDb()).thenReturn(Single.just(currencies))
        `when`(model.getDatesFromDb()).thenReturn(Single.error(RuntimeException()))
        presenter.enterView()
        testScheduler.triggerActions()
        verify(view).displayCurrencies(currencies)
        verify(view).displayError()
    }

    @Test
    fun onRefreshDataReceived() {
        val bankEU = Bank("EU", BankId.ECB, ECBParser(), false)
        `when`(view.isConnectedToNetwork()).thenReturn(true)
        val ratesData = RatesData(
            currencies = mutableListOf(Currency("EUR"), Currency("USD")),
            date = null
        )
        val ratesData2 = RatesData(
            currencies = mutableListOf(Currency("RUB"), Currency("USD")),
            date = Date()
        )
        `when`(model.refreshRates(bank)).thenReturn(Observable.just(ratesData))
        `when`(model.refreshRates(bankEU)).thenReturn(Observable.just(ratesData2))
        presenter.onRefresh(bank, bankEU)
        testScheduler.triggerActions()
        verify(view).displayData(bank, ratesData)
        verify(view).displayData(bankEU, ratesData2)
        verify(view).finishRefreshing()
    }

    @Test
    fun onRefreshNoData() {
        val bankEU = Bank("EU", BankId.ECB, ECBParser(), false)
        `when`(view.isConnectedToNetwork()).thenReturn(true)
        val ratesData = RatesData(
            currencies = mutableListOf(Currency("EUR"), Currency("USD")),
            date = null
        )
        val ratesData2 = RatesData(
            currencies = mutableListOf(),
            date = Date()
        )
        `when`(model.refreshRates(bank)).thenReturn(Observable.just(ratesData))
        `when`(model.refreshRates(bankEU)).thenReturn(Observable.just(ratesData2))
        presenter.onRefresh(bank, bankEU)
        testScheduler.triggerActions()
        verify(view).displayData(bank, ratesData)
        verify(view).displayError()
        verify(view).finishRefreshing()
    }

    @Test
    fun onRefreshError() {
        val bankEU = Bank("EU", BankId.ECB, ECBParser(), false)
        `when`(view.isConnectedToNetwork()).thenReturn(true)
        val ratesData = RatesData(
            currencies = mutableListOf(Currency("EUR"), Currency("USD")),
            date = null
        )
        `when`(model.refreshRates(bank)).thenReturn(Observable.just(ratesData))
        `when`(model.refreshRates(bankEU)).thenReturn(Observable.error(IOException()))
        presenter.onRefresh(bank, bankEU)
        testScheduler.triggerActions()
        verify(view).displayData(bank, ratesData)
        verify(view).displayError()
        verify(view).finishRefreshing()
    }

    @Test
    fun onRefreshNoInternet() {
        val bankEU = Bank("EU", BankId.ECB, ECBParser(), false)
        `when`(view.isConnectedToNetwork()).thenReturn(false)
        presenter.onRefresh(bank, bankEU)
        verify(view).displayNoInternet()
        verify(view).finishRefreshing()
    }

}