package iuliiaponomareva.evroscudo.displayrates

import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import iuliiaponomareva.evroscudo.Bank
import iuliiaponomareva.evroscudo.BankId
import iuliiaponomareva.evroscudo.Currency
import iuliiaponomareva.evroscudo.db.RatesLocalDataSource
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject

class DisplayRatesModel @Inject constructor(
    private val ratesLocalDataSource: RatesLocalDataSource
) :
    DisplayRatesContract.Model {
    private var currenciesKeeper =
        CurrenciesKeeper()
    private val dates = ConcurrentHashMap<BankId, Date>()

    override fun refreshRates(bank: Bank): Observable<RatesData> {

        val parser = bank.parser
        return parser.parseRates()
            .doOnNext {
                if (it.currencies.isNotEmpty()) {
                    ratesLocalDataSource.save(it.currencies, bank)
                    currenciesKeeper.addAll(it.currencies)
                    dates[bank.bankId] = bank.date
                }
            }
            .map { RatesData(currenciesKeeper.currencies.toMutableList(), it.date) }
            .subscribeOn(Schedulers.io())

    }


    override fun getRatesFromDb(): Single<Collection<Currency>> {
        return Single.fromCallable { ratesLocalDataSource.loadRates() }
            .doOnSuccess {
                currenciesKeeper =
                    CurrenciesKeeper(it)
            }
            .map { currenciesKeeper.currencies }
            .subscribeOn(Schedulers.io())
    }

    override fun getDatesFromDb(): Single<HashMap<BankId, Date>> {
        return Single.fromCallable { ratesLocalDataSource.loadDates() }
            .doOnSuccess {
                dates.putAll(it)
            }
            .subscribeOn(Schedulers.io())
    }

    override fun getRatesFromCache(bank: Bank): RatesData {
        return RatesData(currenciesKeeper.currencies.toMutableList(), dates[bank.bankId])
    }

}