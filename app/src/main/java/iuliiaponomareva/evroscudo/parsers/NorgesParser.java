package iuliiaponomareva.evroscudo.parsers;


import android.util.Log;

import androidx.annotation.NonNull;

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


public class NorgesParser extends ExchangeRatesXMLParser {
    private Date date;

    @NonNull
    @Override
    String getURL() {
        return "https://data.norges-bank.no/api/data/EXR/B..NOK.SP?lastNObservations=1";
    }

    @NonNull
    @Override
    List<Currency> parseData(XmlPullParser parser) throws IOException, XmlPullParserException {
        List<Currency> currencies = new ArrayList<>();
        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
        parser.next();
        parser.require(XmlPullParser.START_TAG, null, "message:StructureSpecificData");
        do {
            parser.next();
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String tag = parser.getName();
            if (tag.equals("DataSet") || tag.equals("message:DataSet")) {
                do {
                    parser.next();
                    if (parser.getEventType() != XmlPullParser.START_TAG) {
                        continue;
                    }
                    String name = parser.getName();
                    if (name.equals("Series")) {
                        Currency currency = parseCurrency(parser);
                        currencies.add(currency);
                    } else {
                        skip(parser);
                    }
                }  while (!"message:DataSet".equals(parser.getName()));
            } else {
                skip(parser);
            }
        }  while (!"message:StructureSpecificData".equals(parser.getName()));
        return currencies;
    }

    private Currency parseCurrency(XmlPullParser parser) throws IOException, XmlPullParserException {
        String code = parser.getAttributeValue(null, "BASE_CUR");
        String power = parser.getAttributeValue(null, "UNIT_MULT");
        parser.next();
        while (!"Series".equals(parser.getName())) {
            if (parser.getName().equals("Obs")) {
                String bankRate = parser.getAttributeValue(null, "OBS_VALUE");
                String dateString = parser.getAttributeValue(null, "TIME_PERIOD");

                int powerOfTen = 0;
                try {
                    powerOfTen = Integer.parseInt(power);
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                    Log.e("Evroscudo", "Error parsing power " + power);
                }
                if (dateString != null) {
                    try {
                        Date currencyDate = new SimpleDateFormat("yyyy-MM-dd", Locale.UK).parse(dateString);
                        if ((date == null) || date.before(currencyDate)) {
                            date = currencyDate;
                        }
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
                if ((bankRate != null) && (code != null)) {
                    Currency currency = new Currency(code);
                    currency.setBankRate(bankRate, BankId.Norges);

                    currency.setNominal((int) Math.pow(10.0, powerOfTen), BankId.Norges);
                    return currency;
                }
            } else {
                skip(parser);
            }
        }
        return null;
    }

    @Override
    public Date getDate() {
        return date;
    }
}
