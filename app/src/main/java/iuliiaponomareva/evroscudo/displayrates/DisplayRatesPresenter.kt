package iuliiaponomareva.evroscudo.displayrates

import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable
import iuliiaponomareva.evroscudo.Bank
import javax.inject.Inject

class DisplayRatesPresenter @Inject constructor(
    private val model: DisplayRatesContract.Model,
    private val scheduler: Scheduler
) :
    DisplayRatesContract.Presenter {
    private var view: DisplayRatesContract.View? = null
    private var compositeDisposable = CompositeDisposable()

    override fun attachView(view: DisplayRatesContract.View) {
        this.view = view
    }

    override fun onBankSelected(bank: Bank) {
        view?.apply {
            if (isConnectedToNetwork()) {
                view?.startRefreshing()
                compositeDisposable.add(
                    model.refreshRates(bank)
                        .observeOn(scheduler)
                        .subscribe({
                            if (it.currencies.isEmpty()) {
                                view?.displayError()
                            }
                            view?.displayData(bank, data = it)
                            view?.finishRefreshing()
                        }, {
                            view?.displayError()
                            view?.finishRefreshing()
                        })
                )

            } else {
                view?.displayData(bank, model.getRatesFromCache(bank))
                displayNoInternet()
            }
        }

    }

    override fun enterView() {
        compositeDisposable.add(model.getRatesFromDb()
            .observeOn(scheduler)
            .subscribe({ data ->
                view?.displayCurrencies(data)
            }, {
                view?.displayError()
            })
        )
        compositeDisposable.add(model.getDatesFromDb()
            .observeOn(scheduler)
            .subscribe({ dates ->
                view?.setDates(dates)
            }, {
                view?.displayError()
            })
        )
    }

    override fun onRefresh(firstBank: Bank, secondBank: Bank) {
        view?.apply {
            if (isConnectedToNetwork()) {
                compositeDisposable.add(model.refreshRates(firstBank)
                    .map { ratesData -> Pair(firstBank, ratesData) }
                    .mergeWith(model.refreshRates(secondBank)
                        .map { ratesData -> Pair(secondBank, ratesData) })
                    .observeOn(scheduler)
                    .subscribe(
                        {
                            if (it.second.currencies.isEmpty()) {
                                displayError()
                            }
                            displayData(bank = it.first, data = it.second)
                        },
                        {
                            displayError()
                            finishRefreshing()
                        },
                        {
                            finishRefreshing()
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