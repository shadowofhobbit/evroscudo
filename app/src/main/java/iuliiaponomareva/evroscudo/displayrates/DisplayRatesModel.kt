package iuliiaponomareva.evroscudo.displayrates

import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import iuliiaponomareva.evroscudo.Bank
import iuliiaponomareva.evroscudo.RatesLocalDataSource
import javax.inject.Inject

class DisplayRatesModel @Inject constructor(private val ratesLocalDataSource: RatesLocalDataSource) :
    DisplayRatesContract.Model {

    override fun refreshRates(bank: Bank): Observable<RatesData> {

        val parser = bank.parser
        return parser.parseRates()
            .doOnNext {
                ratesLocalDataSource.save(it.currencies, bank)
            }
            .subscribeOn(Schedulers.io())

    }


    override fun getRates(vararg banks: Bank) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}