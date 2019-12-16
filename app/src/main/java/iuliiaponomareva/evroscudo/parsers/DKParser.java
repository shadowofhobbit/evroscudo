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

public class DKParser extends ExchangeRatesXMLParser {
    private Date date;

    @Override
    String getURL() {
        return "https://www.nationalbanken.dk/_vti_bin/DN/DataService.svc/CurrencyRatesXML?lang=en";
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
        parser.require(XmlPullParser.START_TAG, null, "exchangerates");
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            if (parser.getName().equals("dailyrates")) {
                String unparsedDate = parser.getAttributeValue(null, "id");
                if (unparsedDate != null) {
                    try {
                        // Example:
                        // 2016-03-09
                        date = new SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(unparsedDate);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
                while (parser.next() != XmlPullParser.END_TAG) {
                    if (parser.getEventType() != XmlPullParser.START_TAG) {
                        continue;
                    }
                    String name = parser.getName();
                    if (name.equals("currency")) {
                        currencies.add(parseCurrency(parser));
                        parser.next();
                    } else {
                        skip(parser);
                    }
                }
            }
            else {
                skip(parser);
            }
        }

        return currencies;
    }

    private Currency parseCurrency(XmlPullParser parser) {
        String bankRate= parser.getAttributeValue(null, "rate");
        String code = parser.getAttributeValue(null, "code");
        int nominal = 100;
        if ((bankRate != null) && (code != null) ) {
            Currency currency = new Currency(code);
            currency.setBankRate(bankRate, BankId.DK);
            currency.setNominal(nominal, BankId.DK);
            return currency;
        }
        return null;
    }
}
