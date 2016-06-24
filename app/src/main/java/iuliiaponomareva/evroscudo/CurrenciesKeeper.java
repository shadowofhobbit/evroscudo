package iuliiaponomareva.evroscudo;


import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

public class CurrenciesKeeper {
    private Map<String, Currency> currencies = new TreeMap<>();

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

    public void addAll(Collection<Currency> currencies) {
        for (Currency c :currencies) {
            add(c);
        }
    }

    public Collection<Currency> getCurrencies() {
        return currencies.values();
    }
}
