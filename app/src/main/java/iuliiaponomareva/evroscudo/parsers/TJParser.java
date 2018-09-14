package iuliiaponomareva.evroscudo.parsers;


import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import iuliiaponomareva.evroscudo.Banks;
import iuliiaponomareva.evroscudo.Currency;

public class TJParser extends ExchangeRatesParser {
    private Date date;



    private void parseDate(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.next();
        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
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
                    switch (name) {

                        case "pubDate":
                            String unparsedDate = ExchangeRatesXMLParser.readTag(parser, name);
                            try {
                                //Example:
                                //15.03.16
                                date = new SimpleDateFormat("dd.MM.yy", Locale.US).parse(unparsedDate);
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                            break;
                        default:
                            ExchangeRatesXMLParser.skip(parser);
                            break;
                    }
                }

            } else {
                ExchangeRatesXMLParser.skip(parser);
            }
        }

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
                   code = ExchangeRatesXMLParser.readTag(parser, name);

                    break;
                case "Value":
                    bankRate = ExchangeRatesXMLParser.readTag(parser, name);
                    break;
                case "Nominal":
                    nominal = Integer.parseInt(ExchangeRatesXMLParser.readTag(parser, name));
                    break;

                default:
                    ExchangeRatesXMLParser.skip(parser);
                    break;
            }
        }

        if ((bankRate != null) && (code != null)) {
            Currency currency = new Currency(code);
            currency.setBankRate(bankRate, Banks.TJ);
            currency.setNominal(nominal, Banks.TJ);
            return currency;
        }
        return null;
    }

    @Override
    public Date getDate() {
        return date;
    }

    @Override
    public List<Currency> parse() {
        List<Currency> currencies = new ArrayList<>();
        InputStream inputStream = null;
        InputStream inputStream2 = null;
        try {
            inputStream = downloadUrl("http://nbt.tj/ru/kurs/rss.php");
            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(inputStream, null);
            parseDate(parser);
            // example
            // http://nbt.tj/ru/kurs/export_xml.php?date=2016-03-15&export=xmlout
            String fdate = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(date);
            inputStream2 = downloadUrl("http://nbt.tj/ru/kurs/export_xml.php?date=" + fdate +
            "&export=xmlout");
            XmlPullParser weeklyParser = Xml.newPullParser();
            weeklyParser.setInput(inputStream2, null);
            currencies.addAll(parseXMLData(weeklyParser));
        } catch (XmlPullParserException | IOException e) {
            e.printStackTrace();
        } finally {

            try {
                if (inputStream != null)
                    inputStream.close();
                if (inputStream2 != null)
                    inputStream2.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        return currencies;
    }

    private Collection<? extends Currency> parseXMLData(XmlPullParser parser) throws IOException, XmlPullParserException {
        List<Currency> currencies = new ArrayList<>();
        parser.next();
        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
        parser.require(XmlPullParser.START_TAG, null, "ValCurs");


        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            if (name.equals("Valute")) {
                currencies.add(parseCurrency(parser));
            } else {
                ExchangeRatesXMLParser.skip(parser);
            }
        }
        return currencies;
    }
}
