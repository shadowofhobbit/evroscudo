package iuliiaponomareva.evroscudo.displayrates

import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import iuliiaponomareva.evroscudo.Bank

class DisplayRatesPresenter(private val model: DisplayRatesContract.Model) :
    DisplayRatesContract.Presenter {
    private var view: DisplayRatesContract.View? = null
    private var compositeDisposable = CompositeDisposable()

    override fun attachView(view: DisplayRatesContract.View) {
        this.view = view
    }

    private val scheduler = AndroidSchedulers.mainThread()

    override fun onBankSelected(bank: Bank) {
        view?.apply {
            if (isConnectedToNetwork()) {

                compositeDisposable.add(model.refreshRates(bank)
                    .observeOn(scheduler)
                    .subscribe({
                        if (it.currencies.isEmpty()) {
                            view?.displayError()
                        }
                        view?.displayData(bank, data = it)
                        view?.finishRefreshing()
                    }, {
                        view?.displayError()
                    })
                )

            } else {
                getRatesFromCache()
                displayNoInternet()
            }
        }

    }

    override fun onRefresh(firstBank: Bank, secondBank: Bank) {
        view?.apply {
            if (isConnectedToNetwork()) {
                compositeDisposable.add(model.refreshRates(firstBank)
                    .map { ratesData -> Pair(firstBank, ratesData) }
                    .mergeWith(model.refreshRates(secondBank)
                        .map { ratesData -> Pair(secondBank, ratesData) })
                    .observeOn(scheduler)
                    .subscribe({
                        if (it.second.currencies.isEmpty()) {
                            view?.displayError()
                        }
                        view?.displayData(bank = it.first, data = it.second)
                        view?.finishRefreshing()
                    },
                        {
                            view?.displayError()
                        })
                )
            } else {
                displayNoInternet()
                finishRefreshing()
            }
        }
    }

    override fun leaveView() {
        compositeDisposable.clear()
    }

    override fun detachView() {
        view = null
        compositeDisposable.dispose()
    }
}