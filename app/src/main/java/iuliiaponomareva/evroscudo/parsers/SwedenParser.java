package iuliiaponomareva.evroscudo.parsers;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import iuliiaponomareva.evroscudo.BankId;
import iuliiaponomareva.evroscudo.Currency;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class SwedenParser extends ExchangeRatesXMLParser {

    private static final String BODY_STRING = "<soap:Envelope xmlns:soap=\"http://www.w3.org/2003/05/soap-envelope\"" +
            " xmlns:xsd=\"http://swea.riksbank.se/xsd\">" +
            "<soap:Header/>" +
            "<soap:Body>" +
            "<xsd:getLatestInterestAndExchangeRates>" +
            "<languageid>en</languageid>" +
            "<seriesid>SEKAUDPMI</seriesid>" +
            "<seriesid>SEKBRLPMI</seriesid>" +
            "<seriesid>SEKCADPMI</seriesid>" +
            "<seriesid>SEKCHFPMI</seriesid>" +
            "<seriesid>SEKCNYPMI</seriesid>" +
            "<seriesid>SEKCZKPMI</seriesid>" +
            "<seriesid>SEKDKKPMI</seriesid>" +
            "<seriesid>SEKEURPMI</seriesid>" +
            "<seriesid>SEKGBPPMI</seriesid>" +
            "<seriesid>SEKHKDPMI</seriesid>" +
            "<seriesid>SEKHUFPMI</seriesid>" +
            "<seriesid>SEKIDRPMI</seriesid>" +
            "<seriesid>SEKINRPMI</seriesid>" +
            "<seriesid>SEKISKPMI</seriesid>" +
            "<seriesid>SEKJPYPMI</seriesid>" +
            "<seriesid>SEKKRWPMI</seriesid>" +
            "<seriesid>SEKMADPMI</seriesid>" +
            "<seriesid>SEKMXNPMI</seriesid>" +
            "<seriesid>SEKNOKPMI</seriesid>" +
            "<seriesid>SEKNZDPMI</seriesid>" +
            "<seriesid>SEKPLNPMI</seriesid>" +
            "<seriesid>SEKRUBPMI</seriesid>" +
            "<seriesid>SEKSARPMI</seriesid>" +
            "<seriesid>SEKSGDPMI</seriesid>" +
            "<seriesid>SEKTHBPMI</seriesid>" +
            "<seriesid>SEKTRYPMI</seriesid>" +
            "<seriesid>SEKUSDPMI</seriesid>" +
            "<seriesid>SEKZARPMI</seriesid>" +
            "</xsd:getLatestInterestAndExchangeRates>" +
            "</soap:Body>" +
            "</soap:Envelope>";

    private Date date;

    @NonNull
    String getURL() {
        return "https://swea.riksbank.se:443/sweaWS/services/SweaWebServiceHttpSoap12Endpoint";
    }

    @Override
    public Date getDate() {
        if (date != null) {
            return new Date(date.getTime());
        } else {
            return null;
        }
    }

    @Override
    InputStream downloadUrl(String myURL) throws IOException {
        OkHttpClient client = new OkHttpClient();
        String contentType = "application/soap+xml;charset=UTF-8;action=urn:getLatestInterestAndExchangeRates";
        MediaType mediaType = MediaType.parse(contentType);
        RequestBody body = RequestBody.create(BODY_STRING, mediaType);
        Request request = new Request.Builder()
                .url(getURL())
                .post(body)
                .addHeader("Content-Type", contentType)
                .build();
        Response response = client.newCall(request).execute();
        ResponseBody responseBody = response.body();
        return (responseBody != null) ? responseBody.byteStream() : null;
    }

    @NonNull
    @Override
    public List<Currency> parseData(XmlPullParser parser) throws XmlPullParserException, IOException {
        List<Currency> currencies = new ArrayList<>();
        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
        parser.next();
        parser.require(XmlPullParser.START_TAG, null, "SOAP-ENV:Envelope");
        do {
            parser.next();
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            if ("SOAP-ENV:Body".equals(parser.getName())) {
                do {
                    parser.next();
                    if (parser.getEventType() != XmlPullParser.START_TAG) {
                        continue;
                    }
                    if (parser.getName().equals("ns0:getLatestInterestAndExchangeRatesResponse")) {
                        do {
                            parser.next();
                            if (parser.getEventType() != XmlPullParser.START_TAG) {
                                continue;
                            }
                            String name = parser.getName();
                            if (name.equals("return")) {
                                currencies.addAll(parseCurrencies(parser));
                            } else {
                                skip(parser);
                            }
                        }  while (!"ns0:getLatestInterestAndExchangeRatesResponse".equals(parser.getName()));
                    } else {
                        skip(parser);
                    }
                }  while (!"SOAP-ENV:Body".equals(parser.getName()));
            } else {
                skip(parser);
            }
        }  while (!"SOAP-ENV:Envelope".equals(parser.getName()));
        return currencies;
    }

    private @NonNull
    List<Currency> parseCurrencies(XmlPullParser parser) throws IOException, XmlPullParserException {
        List<Currency> currencies = new ArrayList<>();
        do {
            parser.next();
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            if (parser.getName().equals("groups")) {
                do {

                    parser.next();
                    if (parser.getEventType() != XmlPullParser.START_TAG) {
                        continue;
                    }
                    String name = parser.getName();
                    if (name.equals("series")) {
                        Currency currency = parseCurrency(parser);
                        if (currency != null) {
                            currencies.add(currency);
                        }
                    } else {
                        skip(parser);
                    }
                }  while (!"groups".equals(parser.getName()));
            } else {
                skip(parser);
            }
        }  while (!"return".equals(parser.getName()));
        return currencies;
    }

    private @Nullable
    Currency parseCurrency(XmlPullParser parser) throws IOException, XmlPullParserException {
        String code = null;
        String rate = null;
        double nominal = 1.0;
        do {
            parser.next();
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            switch (name) {
                case "seriesid": {
                    parser.next();
                    String text = parser.getText();
                    if (text != null) {
                        text = text.trim();
                        if (text.length() == 9) {
                            code = text.trim().substring(3, 6);
                        }
                    }
                    break;
                }
                case "unit": {
                    parser.next();
                    String text = parser.getText();
                    if (text != null) {
                        try {
                            nominal = Double.parseDouble(text.trim());
                        } catch (NumberFormatException exception) {
                            exception.printStackTrace();
                        }
                    }
                    break;
                }
                case "resultrows": {
                    do {
                        parser.next();
                        if (parser.getEventType() != XmlPullParser.START_TAG) {
                            continue;
                        }
                        if ("date".equals(parser.getName())) {
                            parser.next();
                            Date time;
                            String unparsedTime = parser.getText();
                            if (unparsedTime != null) {
                                try {
                                    time = new SimpleDateFormat("yyyy-MM-dd", Locale.UK).parse(unparsedTime.trim());
                                    if ((date == null) || (date.before(time))) {
                                        date = time;
                                    }
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                            }
                        } else if ("value".equals(parser.getName())) {
                            parser.next();
                            rate = parser.getText().trim();
                        } else {
                            skip(parser);
                        }
                    }  while (!"resultrows".equals(parser.getName()));

                    break;
                }
                default:
                    skip(parser);
                    break;
            }
        }  while (!"series".equals(parser.getName()));
        Currency currency = null;
        if ((code != null) && (rate != null)) {
            currency = new Currency(code);
            currency.setNominal((int) nominal, BankId.Sweden);
            currency.setBankRate(rate, BankId.Sweden);
        }
        return currency;
    }
}
