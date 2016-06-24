package iuliiaponomareva.evroscudo;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;


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
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.list_item, parent, false);
            holder = new ViewHolder();
            holder.codeTextView = (TextView) convertView.findViewById(R.id.currency_code);
            holder.bankRateTextView = (TextView) convertView.findViewById(R.id.bank_rate);
            holder.bankRateTextView2 = (TextView) convertView.findViewById(R.id.bank_2_rate);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        String code = getItem(position).getCode();
        holder.codeTextView.setText(code);
        Bank bank1 = activity.getFirstBank();
        String rate1 = getItem(position).getBankRate(bank1.getBanks());

        if (TextUtils.isEmpty(rate1))
            holder.bankRateTextView.setText("");
        else {
            StringBuilder text = new StringBuilder(rate1);
            text.append(" ");
            if (bank1.inMyCurrency) {
                text.append(bank1.getCurrencyCode());
                text.append(activity.getString(R.string.is));
                text.append(getItem(position).getNominal(bank1.getBanks()));
            } else {
                text.append(code);
                text.append(activity.getString(R.string.is));
                text.append(getItem(position).getNominal(bank1.getBanks()));
                text.append(bank1.getCurrencyCode());
            }
            holder.bankRateTextView.setText(text);
        }


        Bank bank2 = activity.getSecondBank();
        String rate2 = getItem(position).getBankRate(bank2.getBanks());
        if (TextUtils.isEmpty(rate2))
            holder.bankRateTextView2.setText("");
        else {
            StringBuilder text = new StringBuilder(rate2);
            text.append(" ");
            if (bank2.inMyCurrency) {
                text.append(bank2.getCurrencyCode());
                text.append(activity.getString(R.string.is));
                text.append(getItem(position).getNominal(bank2.getBanks()));
            } else {
                text.append(code);
                text.append(activity.getString(R.string.is));
                text.append(getItem(position).getNominal(bank2.getBanks()));
                text.append(bank2.getCurrencyCode());
            }
            holder.bankRateTextView2.setText(text);
        }
        return convertView;
    }
}
