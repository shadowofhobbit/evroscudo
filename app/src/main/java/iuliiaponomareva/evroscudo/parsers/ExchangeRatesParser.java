package iuliiaponomareva.evroscudo.parsers;


import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.List;

import iuliiaponomareva.evroscudo.Currency;
import iuliiaponomareva.evroscudo.HttpUtilsKt;

public abstract class ExchangeRatesParser {
    public abstract Date getDate();



    public abstract List<Currency> parse();

    InputStream downloadUrl(String myURL) throws IOException {
        return HttpUtilsKt.getInputStream(myURL);
    }
}
