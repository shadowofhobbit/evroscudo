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


public class CBRParser extends ExchangeRatesXMLParser {
    private Date date;


    @Override
    String getURL() {
        return "https://www.cbr.ru/scripts/XML_daily.asp";
    }

    @Override
    List<Currency> parseData(XmlPullParser parser) throws IOException, XmlPullParserException {
        List<Currency> currencies = new ArrayList<>();
        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
        parser.next();
        parser.require(XmlPullParser.START_TAG, null, "ValCurs");
        String unparsedDate = parser.getAttributeValue(null, "Date");
        try {
            //Example:
            //18.02.2016
            date = new SimpleDateFormat("dd.MM.yyyy", Locale.US).parse(unparsedDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            if (name.equals("Valute")) {
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
        while (!((parser.next() == XmlPullParser.END_TAG) && (parser.getName().equals("Valute")))) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            switch (name) {
                case "CharCode":
                    code = readTag(parser, name);
                    break;
                case "Value":
                    bankRate = readTag(parser, name);
                    break;
                case "Nominal":
                    nominal = Integer.parseInt(readTag(parser, name));
                    break;
                default:
                    skip(parser);
                    break;
            }
        }
        if ((bankRate != null) && (code != null)) {
            Currency currency = new Currency(code);
            currency.setBankRate(bankRate, BankId.CBR);
            currency.setNominal(nominal, BankId.CBR);
            return currency;
        }
        return null;
    }


    @Override
    public Date getDate() {
        return date;
    }
}
