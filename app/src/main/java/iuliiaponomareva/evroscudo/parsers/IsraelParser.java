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


public class IsraelParser extends ExchangeRatesXMLParser {
    private Date date;

    @Override
    String getURL() {
        return "http://www.boi.org.il/currency.xml";
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
        parser.require(XmlPullParser.START_TAG, null, "CURRENCIES");

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            switch (name) {
                case "CURRENCY":
                    Currency currency = parseCurrency(parser);
                    if (currency != null)
                        currencies.add(currency);
                    break;
                case "LAST_UPDATE":
                    String unparsedDate = readTag(parser, name);
                    try {
                        //Example:
                        //2016-03-02
                        date = new SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(unparsedDate);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    break;
                default:
                    skip(parser);
                    break;
            }
        }
        return currencies;
    }

    private Currency parseCurrency(XmlPullParser parser) throws IOException, XmlPullParserException {
        String bankRate = null;
        String code = null;
        int nominal = 1;
        while (parser.next() != XmlPullParser.END_TAG)  {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            switch (name) {
                case "CURRENCYCODE":
                    code = readTag(parser, name);
                    break;
                case "RATE":
                    bankRate = readTag(parser, name);
                    break;
                case "UNIT":
                    nominal = Integer.parseInt(readTag(parser, name));
                    break;
                default:
                    skip(parser);
                    break;
            }
        }

        if ((bankRate != null) && (code != null)) {
            Currency currency = new Currency(code);
            currency.setBankRate(bankRate, Banks.ISRAEL);
            currency.setNominal(nominal, Banks.ISRAEL);
            return currency;
        }
        return null;
    }
}
