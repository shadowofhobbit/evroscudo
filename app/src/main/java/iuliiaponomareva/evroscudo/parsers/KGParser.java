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


public class KGParser extends ExchangeRatesParser {
    private Date dailyDate;

    private String dailyUrl = "http://www.nbkr.kg/XML/daily.xml";
    private String weeklyUrl = "http://www.nbkr.kg/XML/weekly.xml";

    @Override
    public Date getDate() {
        return dailyDate;
    }



    @Override
    public List<Currency> parse() {
        List<Currency> currencies = new ArrayList<>();
        InputStream inputStream = null;
        InputStream inputStream2 = null;
        try {
            inputStream = downloadUrl(dailyUrl);
            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(inputStream, null);
            currencies.addAll(parseData(parser));
            inputStream2 = downloadUrl(weeklyUrl);
            XmlPullParser weeklyParser = Xml.newPullParser();
            weeklyParser.setInput(inputStream2, null);
            currencies.addAll(parseData(weeklyParser));
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

    private Collection<? extends Currency> parseData(XmlPullParser parser) throws IOException, XmlPullParserException {
        List<Currency> currencies = new ArrayList<>();
        parser.next();
        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
        parser.require(XmlPullParser.START_TAG, null, "CurrencyRates");
        if (dailyDate == null) {
            String unparsedDate = parser.getAttributeValue(null, "Date");
            try {
                //Example:
                //11.03.2016
                dailyDate = new SimpleDateFormat("dd.MM.yyyy", Locale.US).parse(unparsedDate);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            if (name.equals("Currency")) {
                currencies.add(parseCurrency(parser));
            } else {
                ExchangeRatesXMLParser.skip(parser);
            }
        }
        return currencies;
    }

    private Currency parseCurrency(XmlPullParser parser) throws IOException, XmlPullParserException {
        String bankRate = null;
        String code;
        int nominal = 1;
        code = parser.getAttributeValue(null, "ISOCode");
        while (parser.next() != XmlPullParser.END_TAG)  {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            switch (name) {
                case "Nominal":
                    nominal = Integer.parseInt(ExchangeRatesXMLParser.readTag(parser, name));
                    break;
                case "Value":
                    bankRate = ExchangeRatesXMLParser.readTag(parser, name);
                    break;

                default:
                    ExchangeRatesXMLParser.skip(parser);
                    break;
            }
        }

        if ((bankRate != null) && (code != null)) {
            Currency currency = new Currency(code);
            currency.setBankRate(bankRate, Banks.KG);
            currency.setNominal(nominal, Banks.KG);
            return currency;
        }
        return null;
    }
}
