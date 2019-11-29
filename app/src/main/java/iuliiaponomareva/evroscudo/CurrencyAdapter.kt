package iuliiaponomareva.evroscudo

import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.RecyclerView
import java.util.*


class CurrencyAdapter(private val activity: DisplayRatesActivity) :
    RecyclerView.Adapter<CurrencyAdapter.CurrencyViewHolder>() {
    private val rates = ArrayList<Currency>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CurrencyViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_item, parent, false)
        return CurrencyViewHolder(view)
    }

    override fun onBindViewHolder(holder: CurrencyViewHolder, position: Int) {
        val code = rates[position].code
        holder.codeTextView.text = code
        val bank1 = activity.firstBank
        val rate1 = rates[position].getBankRate(bank1.bankId)
        showRate(position, holder.bankRateTextView, code, bank1, rate1)
        val bank2 = activity.secondBank
        val rate2 = rates[position].getBankRate(bank2.bankId)
        showRate(position, holder.bankRateTextView2, code, bank2, rate2)
    }

    override fun getItemCount(): Int {
        return rates.size
    }

    fun set(currencies: Collection<Currency>) {
        val needed = currencies
            .filter {
                PreferenceManager.getDefaultSharedPreferences(this.activity)
                    .getBoolean(it.code, true)
            }
            .filter {
                (it.getBankRate(activity.firstBank.bankId) != null) || (it.getBankRate(
                    activity.secondBank.bankId
                ) != null)
            }
        rates.clear()
        rates.addAll(needed)
        notifyDataSetChanged()
    }

    private fun showRate(
        position: Int,
        bankRateTextView: TextView,
        code: String,
        bank: Bank,
        rate: String?
    ) {
        if (TextUtils.isEmpty(rate))
            bankRateTextView.text = ""
        else {
            val text = StringBuilder(rate!!)
            text.append(" ")
            if (bank.inMyCurrency) {
                text.append(bank.currencyCode)
                text.append(activity.getString(R.string.`is`))
                text.append(rates[position].getNominal(bank.bankId))
            } else {
                text.append(code)
                text.append(activity.getString(R.string.`is`))
                text.append(rates[position].getNominal(bank.bankId))
                text.append(bank.currencyCode)
            }
            bankRateTextView.text = text
        }
    }

    class CurrencyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var codeTextView: TextView
        var bankRateTextView: TextView
        var bankRateTextView2: TextView

        init {
            codeTextView = itemView.findViewById(R.id.currency_code)
            bankRateTextView = itemView.findViewById(R.id.bank_rate)
            bankRateTextView2 = itemView.findViewById(R.id.bank_2_rate)
        }
    }
}
