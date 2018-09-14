package iuliiaponomareva.evroscudo;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;


public class CurrencyAdapter extends ArrayAdapter<Currency> {
    private DisplayRatesActivity activity;

    public CurrencyAdapter(DisplayRatesActivity activity, int resource) {
        super(activity, resource);
        this.activity = activity;
    }

    static class ViewHolder {
        TextView codeTextView;
        TextView bankRateTextView;
        TextView bankRateTextView2;
    }

    @Override
    @NonNull
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.list_item, parent, false);
            holder = new ViewHolder();
            holder.codeTextView = convertView.findViewById(R.id.currency_code);
            holder.bankRateTextView = convertView.findViewById(R.id.bank_rate);
            holder.bankRateTextView2 = convertView.findViewById(R.id.bank_2_rate);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        String code = getItem(position).getCode();
        holder.codeTextView.setText(code);
        Bank bank1 = activity.getFirstBank();
        String rate1 = getItem(position).getBankRate(bank1.getBanks());
        showRate(position, holder.bankRateTextView, code, bank1, rate1);
        Bank bank2 = activity.getSecondBank();
        String rate2 = getItem(position).getBankRate(bank2.getBanks());
        showRate(position, holder.bankRateTextView2, code, bank2, rate2);

        return convertView;
    }

    private void showRate(int position, TextView bankRateTextView, String code, Bank bank, String rate) {
        if (TextUtils.isEmpty(rate))
            bankRateTextView.setText("");
        else {
            StringBuilder text = new StringBuilder(rate);
            text.append(" ");
            if (bank.inMyCurrency) {
                text.append(bank.getCurrencyCode());
                text.append(activity.getString(R.string.is));
                text.append(getItem(position).getNominal(bank.getBanks()));
            } else {
                text.append(code);
                text.append(activity.getString(R.string.is));
                text.append(getItem(position).getNominal(bank.getBanks()));
                text.append(bank.getCurrencyCode());
            }
            bankRateTextView.setText(text);
        }
    }
}
