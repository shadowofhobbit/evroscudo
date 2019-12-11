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

import iuliiaponomareva.evroscudo.BankId;
import iuliiaponomareva.evroscudo.Currency;

public class ECBParser extends ExchangeRatesXMLParser {
    private Date date;


    @Override
    String getURL() {
        return "http://www.ecb.europa.eu/stats/eurofxref/eurofxref-daily.xml";
    }

    @Override
    public Date getDate() {
        return date;
    }

    @Override
    List<Currency> parseData(XmlPullParser parser) throws IOException, XmlPullParserException {
        List<Currency> currencies = new ArrayList<>();
        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, true);
        parser.next();
        parser.require(XmlPullParser.START_TAG, null, "Envelope");
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();

            if (name.equals("Cube")) {
                currencies.addAll(parseDate(parser));
                break;

            } else {
                skip(parser);
            }
        }
        return currencies;
    }

    private List<Currency> parseDate(XmlPullParser parser) throws IOException, XmlPullParserException {
        List<Currency> currencies = new ArrayList<>();
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            if (parser.getName().equals("Cube")) {
                //Example:
                //2016-02-17
                String unparsedDate = parser.getAttributeValue(null, "time");
                try {
                    date = new SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(unparsedDate);
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                currencies.addAll(parseCurrencies(parser));
                break;
            }
        }
        return currencies;
    }

    private List<Currency> parseCurrencies(XmlPullParser parser) throws IOException, XmlPullParserException {
        List<Currency> currencies = new ArrayList<>();
        while ((parser.next() != XmlPullParser.END_TAG)) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            if (parser.getName().equals("Cube")) {
                String code = parser.getAttributeValue(null, "currency");
                String rate = parser.getAttributeValue(null, "rate");
                Currency currency = new Currency(code);
                currency.setBankRate(rate, BankId.ECB);
                currencies.add(currency);
                parser.next();
            }
        }
        return currencies;
    }

}