package iuliiaponomareva.evroscudo.parsers;


import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;

import io.reactivex.Observable;
import iuliiaponomareva.evroscudo.Currency;
import iuliiaponomareva.evroscudo.HttpUtilsKt;
import iuliiaponomareva.evroscudo.displayrates.RatesData;

public abstract class ExchangeRatesParser {
    public abstract Date getDate();



    public abstract List<Currency> parse();

    InputStream downloadUrl(String myURL) throws IOException {
        return HttpUtilsKt.getInputStream(myURL);
    }

    public Observable<RatesData> parseRates() {
        return Observable.fromCallable(new Callable<RatesData>() {
            @Override
            public RatesData call() {
                return new RatesData(parse(), getDate());
            }
        });
    }
}
