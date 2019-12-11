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

public class BYParser extends ExchangeRatesXMLParser {
    private Date date;
    @Override
    String getURL() {
        return "http://www.nbrb.by/Services/XmlExRates.aspx";
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
        parser.require(XmlPullParser.START_TAG, null, "DailyExRates");
        String unparsedDate = parser.getAttributeValue(null, "Date");
        try {
            // Example:
            // 03/05/2016
            date = new SimpleDateFormat("MM/dd/yyyy", Locale.US).parse(unparsedDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            if (name.equals("Currency")) {
                currencies.add(parseCurrency(parser));

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
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            switch (name) {
                case "CharCode":
                    code = readTag(parser, name);
                    break;
                case "Rate":
                    bankRate = readTag(parser, name);
                    break;
                case "Scale":
                    nominal = Integer.parseInt(readTag(parser, name));
                    break;
                default:
                    skip(parser);
                    break;
            }
        }
        if ((bankRate != null) && (code != null)) {
            Currency currency = new Currency(code);
            currency.setBankRate(bankRate, BankId.BY);
            currency.setNominal(nominal, BankId.BY);
            return currency;
        }
        return null;
    }
}
