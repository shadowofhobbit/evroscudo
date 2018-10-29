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

import androidx.annotation.NonNull;
import iuliiaponomareva.evroscudo.BankId;
import iuliiaponomareva.evroscudo.Currency;


public class UAParser extends ExchangeRatesXMLParser {
    private Date date;

    @NonNull
    @Override
    String getURL() {
        return "http://bank.gov.ua/NBUStatService/v1/statdirectory/exchange";
    }

    @Override
    public Date getDate() {
        if (date != null) {
            return new Date(date.getTime());
        } else {
            return null;
        }
    }

    @NonNull
    @Override
    List<Currency> parseData(XmlPullParser parser) throws IOException, XmlPullParserException {
        List<Currency> currencies = new ArrayList<>();
        parser.next();
        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);

        parser.require(XmlPullParser.START_TAG, null, "exchange");

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            if (name.equals("currency")) {
                Currency currency = parseCurrency(parser);
                if (currency != null) {
                    currencies.add(currency);
                }
            } else {
                skip(parser);
            }
        }
        return currencies;
    }

    private Currency parseCurrency(XmlPullParser parser)
            throws IOException, XmlPullParserException {
        String bankRate = null;
        String code = null;
        int nominal = 1;
        while (parser.next() != XmlPullParser.END_TAG)  {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            switch (name) {
                case "cc":
                    code = readTag(parser, name);
                    break;
                case "rate":
                    bankRate = readTag(parser, name);
                    break;
                case "exchangedate":
                    String unparsedDate = readTag(parser, name);
                    try {
                        //Example:
                        //01.03.2016
                        date = new SimpleDateFormat("dd.MM.yyyy", Locale.US).parse(unparsedDate);
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
            currency.setBankRate(bankRate, BankId.UA);
            currency.setNominal(nominal, BankId.UA);
            return currency;
        }
        return null;
    }
}

