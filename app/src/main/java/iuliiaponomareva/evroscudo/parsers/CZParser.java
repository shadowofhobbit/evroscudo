package iuliiaponomareva.evroscudo.parsers;


import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;

import iuliiaponomareva.evroscudo.BankId;
import iuliiaponomareva.evroscudo.Currency;

public class CZParser extends ExchangeRatesParser {
    private Date date;

    @Override
    public Date getDate() {
        return date;
    }


    private String getURL() {
        return "https://www.cnb.cz/en/financial_markets/foreign_exchange_market/exchange_rate_fixing/daily.txt";
    }

    @Override
    public List<Currency> parse() {
        List<Currency> currencies = new ArrayList<>();
        InputStream inputStream = null;
        Scanner in = null;
        try {
            inputStream = downloadUrl(getURL());
            in = new Scanner(inputStream);
            String dateString = in.nextLine();
            try {
                // Example:
                // 09 Mar 2016 #48
                date = new SimpleDateFormat("dd MMM yyyy", Locale.US).parse(dateString);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            in.nextLine();
            while (in.hasNextLine()) {
                String[] data = in.nextLine().split("\\|");
                if (data.length >= 5) {
                    String bankRate = data[4];
                    String code = data[3];
                    int amount = Integer.parseInt(data[2]);
                    Currency currency = new Currency(code);
                    currency.setBankRate(bankRate, BankId.CZ);
                    currency.setNominal(amount, BankId.CZ);
                    currencies.add(currency);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (inputStream != null)
                    inputStream.close();
                if (in != null)
                    in.close();

            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        return currencies;
    }


}
