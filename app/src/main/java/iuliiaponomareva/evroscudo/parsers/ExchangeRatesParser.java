package iuliiaponomareva.evroscudo.parsers;


import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;
import java.util.List;

import iuliiaponomareva.evroscudo.Currency;

public abstract class ExchangeRatesParser {
    public abstract Date getDate();



    public abstract List<Currency> parse();

    InputStream downloadUrl(String myURL) throws IOException {
        URL url = new URL(myURL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setReadTimeout(10000 /* milliseconds */);
        conn.setConnectTimeout(15000 /* milliseconds */);
        conn.setRequestMethod("GET");
        conn.setDoInput(true);
        conn.connect();
        return conn.getInputStream();
    }
}
