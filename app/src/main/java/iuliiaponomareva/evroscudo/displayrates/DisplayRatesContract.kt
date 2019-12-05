package iuliiaponomareva.evroscudo.displayrates

import io.reactivex.Observable
import iuliiaponomareva.evroscudo.Bank
import iuliiaponomareva.evroscudo.mvp.BasePresenter
import iuliiaponomareva.evroscudo.mvp.BaseView

interface DisplayRatesContract {
    interface Presenter : BasePresenter<View> {
        fun onBankSelected(bank: Bank)
        fun onRefresh(firstBank : Bank, secondBank: Bank)

        fun leaveView()
    }

    interface View : BaseView {
        fun displayData(bank: Bank, data: RatesData)
        fun isConnectedToNetwork(): Boolean
        fun displayNoInternet()
        fun finishRefreshing()
        fun displayInfo()
        fun displaySettings()
        fun getRatesFromCache()
    }

    interface Model {

        fun getRates(vararg banks: Bank)
        fun refreshRates(bank: Bank): Observable<RatesData>
    }
}