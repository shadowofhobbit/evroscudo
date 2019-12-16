package iuliiaponomareva.evroscudo.parsers;


import androidx.annotation.NonNull;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import iuliiaponomareva.evroscudo.BankId;
import iuliiaponomareva.evroscudo.Currency;


@SuppressWarnings("SpellCheckingInspection")
public class EnglandParser extends ExchangeRatesXMLParser {
    private Map<String, String> codes = new HashMap<>();
    private Date date = null;

    public EnglandParser() {
        codes.put("XUDLADS", "AUD");
        codes.put("XUDLCDS", "CAD");
        codes.put("XUDLBK89", "CNY");
        codes.put("XUDLBK25", "CZK");
        codes.put("XUDLDKS", "DKK");
        codes.put("XUDLERS", "EUR");
        codes.put("XUDLHDS", "HKD");
        codes.put("XUDLBK33", "HUF");
        codes.put("XUDLBK97", "INR");
        codes.put("XUDLBK78", "ILS");
        codes.put("XUDLJYS", "JPY");
        codes.put("XUDLBK83", "MYR");
        codes.put("XUDLNDS", "NZD");
        codes.put("XUDLNKS", "NOK");
        codes.put("XUDLBK47", "PLN");
        codes.put("XUDLBK85", "RUB");
        codes.put("XUDLSRS", "SAR");
        codes.put("XUDLSGS", "SGD");
        codes.put("XUDLZRS", "ZAR");
        codes.put("XUDLBK93", "KRW");
        codes.put("XUDLSKS", "SEK");
        codes.put("XUDLSFS", "CHF");
        codes.put("XUDLTWS", "TWD");
        codes.put("XUDLBK87", "THB");
        codes.put("XUDLBK95", "TRY");
        codes.put("XUDLUSS", "USD");
    }


    @NonNull
    @Override
    String getURL() {
        long DAY_IN_MS = 1000 * 60 * 60 * 24;
        Date from = new Date(System.currentTimeMillis() - (7 * DAY_IN_MS));
        DateFormat format = new SimpleDateFormat("dd/MMM/YYYY", Locale.UK);
        String dateFrom = format.format(from);
        //19/Apr/2018
        return "https://www.bankofengland.co.uk/boeapps/iadb/fromshowcolumns.asp?CodeVer=new&xml.x=yes" +
                "&Datefrom=" + dateFrom + "&Dateto=now&SeriesCodes=XUDLADS,XUDLCDS,XUDLBK89,XUDLBK25,XUDLDKS," +
                "XUDLERS,XUDLHDS,XUDLBK33,XUDLBK97,XUDLBK78,XUDLJYS,XUDLBK83,XUDLNDS,XUDLNKS,XUDLBK47," +
                "XUDLBK85,XUDLSRS,XUDLSGS,XUDLZRS,XUDLBK93,XUDLSKS,XUDLSFS,XUDLTWS,XUDLBK87,XUDLBK95," +
                "XUDLUSS&VUD=A&VUDdate=latest&Omit=-F1-A-B-D-E";
    }

    @NonNull
    @Override
    List<Currency> parseData(XmlPullParser parser) throws IOException, XmlPullParserException {
        List<Currency> currencies = new ArrayList<>();
        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
        parser.next();
        parser.require(XmlPullParser.START_TAG, null, "Envelope");
        do {
            parser.next();
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            if (name.equals("Cube")) {
                String code = codes.get(parser.getAttributeValue("", "SCODE"));
                if (code != null) {
                    Currency currency = parseDateAndRate(parser, code);
                    currencies.add(currency);
                }
            } else {
                skip(parser);
            }
        }  while (!"Envelope".equals(parser.getName()));
        return currencies;
    }


    private Currency parseDateAndRate(XmlPullParser parser, String code) throws IOException,
            XmlPullParserException {
        Currency currency = new Currency(code);

        while (parser.next() != XmlPullParser.END_TAG) {

            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            if (parser.getName().equals("Cube")) {
                //Example:
                //2016-02-17
                String unparsedDate = parser.getAttributeValue(null, "LAST_OBS");
                if (unparsedDate != null) {
                    try {
                        date = new SimpleDateFormat("yyyy-MM-dd", Locale.UK).parse(unparsedDate);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    parser.next();
                } else {

                    Date time;
                    String unparsedTime = parser.getAttributeValue(null, "TIME");
                    if (unparsedTime != null) {
                        try {
                            time = new SimpleDateFormat("yyyy-MM-dd", Locale.UK).parse(unparsedTime);
                            if ((date == null) || (date.equals(time))) {
                                String rate = parser.getAttributeValue(null, "OBS_VALUE");
                                if (rate != null) {
                                    currency.setBankRate(rate, BankId.UK);
                                }
                            }
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }
                    parser.next();
                    parser.next();
                }
            }


        }
        return currency;
    }

    @Override
    public Date getDate() {
        return date;
    }
}
