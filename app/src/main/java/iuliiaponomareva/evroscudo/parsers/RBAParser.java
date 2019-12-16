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

public class RBAParser extends ExchangeRatesXMLParser {
    private Date date;

    @Override
    String getURL() {
        return "https://www.rba.gov.au/rss/rss-cb-exchange-rates.xml";
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
        parser.require(XmlPullParser.START_TAG, null, "RDF");
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();

            if (name.equals("item")) {
                currencies.add(parseCurrency(parser));
                //break;

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
            if (parser.getName().equals("statistics")) {
                while (parser.next() != XmlPullParser.END_TAG) {
                    if (parser.getEventType() != XmlPullParser.START_TAG) {
                        continue;
                    }
                    String name = parser.getName();

                    if (name.equals("exchangeRate")) {
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
                case "targetCurrency":
                    code = parser.nextText();
                    break;
                case "observation":
                    bankRate = parseObservation(parser);
                    break;
                case "observationPeriod":
                    date = parsePeriod(parser);
                    break;
                default:
                    skip(parser);
                    break;
            }
        }
        if ((bankRate != null) && (code != null)) {
            Currency currency = new Currency(code);
            currency.setBankRate(bankRate, BankId.RBA);
            currency.setNominal(nominal, BankId.RBA);
            return currency;
        }
        return null;
    }

    private Date parsePeriod(XmlPullParser parser) throws XmlPullParserException, IOException {
        Date resDate = null;
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();

            if (name.equals("period")) {
                String date = parser.nextText();
                try {
                    //Example:
                    //2016-02-29
                    resDate = new SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(date);
                } catch (ParseException e) {
                    e.printStackTrace();
                }

            } else {
                skip(parser);
            }
        }
        return resDate;
    }

    private String parseObservation(XmlPullParser parser) throws IOException, XmlPullParserException {
        String rate = "";
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();

            if (name.equals("value")) {
                rate = parser.nextText();

            } else {
                skip(parser);
            }
        }
        return rate;
    }

}
