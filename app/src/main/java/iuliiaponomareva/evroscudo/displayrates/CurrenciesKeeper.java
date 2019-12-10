package iuliiaponomareva.evroscudo.displayrates;


import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

import javax.inject.Inject;

import iuliiaponomareva.evroscudo.Currency;

public class CurrenciesKeeper {
    private Map<String, Currency> currencies = new TreeMap<>();

    @Inject
    public CurrenciesKeeper() {

    }

    public CurrenciesKeeper(Map<String, Currency> currencies) {
        this.currencies.putAll(currencies);
    }

    private void add(Currency newCurrency) {
        Currency oldValue = currencies.get(newCurrency.getCode());
        if (oldValue != null) {
            oldValue.updateRates(newCurrency.getBankRates());
            oldValue.updateNominals(newCurrency.getNominals());
        } else {
            currencies.put(newCurrency.getCode(), newCurrency);

        }
    }

    public synchronized void addAll(Collection<Currency> currencies) {
        for (Currency c :currencies) {
            add(c);
        }
    }

    public Collection<Currency> getCurrencies() {
        return currencies.values();
    }
}
