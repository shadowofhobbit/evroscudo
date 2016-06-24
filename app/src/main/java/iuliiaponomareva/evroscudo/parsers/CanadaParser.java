package iuliiaponomareva.evroscudo.parsers;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import iuliiaponomareva.evroscudo.Banks;
import iuliiaponomareva.evroscudo.Currency;


public class CanadaParser extends ExchangeRatesXMLParser {
    private Date date;

    @Override
    String getURL() {
        return "http://www.bankofcanada.ca/stats/assets/rates_rss/noon/en_all.xml";
    }

    @Override
    public Date getDate() {
        return date;
    }

    @Override
    List<Currency> parseData(XmlPullParser parser) throws IOException, XmlPullParserException {
        List<Currency> currencies = new ArrayList<>();
        parser.next();
        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
        parser.require(XmlPullParser.START_TAG, null, "RDF");
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            if (name.equals("item")) {
                currencies.add(parseCurrency(parser));
            } else {
                skip(parser);
            }
        }
        return currencies;
    }

    private Currency parseCurrency(XmlPullParser parser) throws IOException, XmlPullParserException {
        Currency curr = null;
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            if (parser.getName().equals("cb:statistics")) {
                while (parser.next() != XmlPullParser.END_TAG) {
                    if (parser.getEventType() != XmlPullParser.START_TAG) {
                        continue;
                    }
                    String name = parser.getName();

                    if (name.equals("cb:exchangeRate")) {
                        curr = parseCur(parser);
                    } else {
                        skip(parser);
                    }
                }
            } else {
                skip(parser);
            }
        }

        return curr;
    }

    private Currency parseCur(XmlPullParser parser) throws IOException, XmlPullParserException {
        String bankRate = null;
        String code = null;
        int nominal = 1;
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }

            String name = parser.getName();
            switch (name) {
                case "cb:targetCurrency":
                    String noonCode = parser.nextText();
                    code = noonCode.substring(0, noonCode.indexOf("_NOON"));
                    break;
                case "cb:value":
                    bankRate = parser.nextText();
                    break;
                case "cb:observationPeriod":
                    String unparsedDate = parser.nextText();
                    try {
                        //Example:
                        //2016-02-29T12:30:00-05:00
                        date = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZZZZZ", Locale.CANADA).parse(unparsedDate);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    break;
                default:
                    skip(parser);
                    break;
            }
        }
        if ((bankRate != null) && (code != null)) {
            Currency currency = new Currency(code);
            currency.setBankRate(bankRate, Banks.CANADA);
            currency.setNominal(nominal, Banks.CANADA);
            return currency;
        }
        return null;
    }
}
