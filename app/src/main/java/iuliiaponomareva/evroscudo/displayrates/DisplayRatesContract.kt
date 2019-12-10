package iuliiaponomareva.evroscudo.displayrates

import io.reactivex.Observable
import io.reactivex.Single
import iuliiaponomareva.evroscudo.Bank
import iuliiaponomareva.evroscudo.BankId
import iuliiaponomareva.evroscudo.Currency
import iuliiaponomareva.evroscudo.mvp.BasePresenter
import iuliiaponomareva.evroscudo.mvp.BaseView
import java.util.*

interface DisplayRatesContract {
    interface Presenter : BasePresenter<View> {
        fun onBankSelected(bank: Bank)
        fun onRefresh(firstBank : Bank, secondBank: Bank)

        fun leaveView()
        fun enterView()
    }

    interface View : BaseView {
        fun displayData(bank: Bank, data: RatesData)
        fun isConnectedToNetwork(): Boolean
        fun displayNoInternet()
        fun finishRefreshing()
        fun displayInfo()
        fun displaySettings()
        fun displayCurrencies(currencies: Collection<Currency>)
        fun setDates(data: Map<BankId, Date>)
    }

    interface Model {

        fun refreshRates(bank: Bank): Observable<RatesData>
        fun getRatesFromCache(bank: Bank): RatesData
        fun getRatesFromDb(): Single<Collection<Currency>>
        fun getDatesFromDb(): Single<HashMap<BankId, Date>>
    }
}