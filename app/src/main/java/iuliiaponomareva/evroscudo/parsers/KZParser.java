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


public class KZParser extends ExchangeRatesXMLParser {
    private Date date;

    @Override
    String getURL() {
        return "https://www.nationalbank.kz/rss/rates_all.xml";
    }

    @Override
    public Date getDate() {
        return date;
    }

    @Override
    List<Currency> parseData(XmlPullParser parser) throws IOException, XmlPullParserException {
        List<Currency> currencies = new ArrayList<>();
        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
        parser.next();
        parser.require(XmlPullParser.START_TAG, null, "rss");
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String tag = parser.getName();
            if (tag.equals("channel")) {
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

            } else {
                skip(parser);
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
                case "title":
                    code = readTag(parser, name);
                    break;
                case "description":
                    bankRate = readTag(parser, name);
                    break;
                case "quant":
                    nominal = Integer.parseInt(readTag(parser, name));
                    break;
                case "pubDate":
                    String unparsedDate = readTag(parser, name);
                    try {
                        //Example:
                        //03.03.16
                        date = new SimpleDateFormat("dd.MM.yy", Locale.US).parse(unparsedDate);
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
            currency.setBankRate(bankRate, BankId.KZ);
            currency.setNominal(nominal, BankId.KZ);
            return currency;
        }
        return null;
    }
}
