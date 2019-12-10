package iuliiaponomareva.evroscudo.di

import android.content.Context
import dagger.Module
import dagger.Provides
import iuliiaponomareva.evroscudo.Bank
import iuliiaponomareva.evroscudo.BankId
import iuliiaponomareva.evroscudo.R
import iuliiaponomareva.evroscudo.parsers.*

@Module
class DataModule {
    @Provides
    fun provideBanks(context: Context): HashMap<BankId, Bank> {
        val banks = HashMap<BankId, Bank>()

        banks[BankId.CBR] = Bank(
            context.getString(R.string.cbr),
            BankId.CBR,
            CBRParser(),
            true
        )
        banks[BankId.ECB] = Bank(
            context.getString(R.string.ecb),
            BankId.ECB,
            ECBParser(),
            false
        )
        banks[BankId.RBA] = Bank(
            context.getString(R.string.rba),
            BankId.RBA,
            RBAParser(),
            false
        )
        banks[BankId.CANADA] = Bank(
            context.getString(R.string.bank_of_canada),
            BankId.CANADA,
            CanadaParser(),
            false
        )
        banks[BankId.UA] = Bank(
            context.getString(R.string.ua),
            BankId.UA,
            UAParser(),
            true
        )
        banks[BankId.KZ] = Bank(
            context.getString(R.string.kz),
            BankId.KZ,
            KZParser(),
            true
        )
        banks[BankId.ISRAEL] = Bank(
            context.getString(R.string.IL),
            BankId.ISRAEL,
            IsraelParser(),
            true
        )
        banks[BankId.BY] = Bank(
            context.getString(R.string.BY),
            BankId.BY,
            BYParser(),
            true
        )
        banks[BankId.DK] = Bank(
            context.getString(R.string.DK),
            BankId.DK,
            DKParser(),
            true
        )
        banks[BankId.CZ] = Bank(
            context.getString(R.string.czech),
            BankId.CZ,
            CZParser(),
            true
        )
        banks[BankId.KG] = Bank(
            context.getString(R.string.KG),
            BankId.KG,
            KGParser(),
            true
        )
        banks[BankId.TJ] = Bank(
            context.getString(R.string.TJ),
            BankId.TJ,
            TJParser(),
            true
        )
        banks[BankId.Norges] = Bank(
            context.getString(R.string.norges_bank),
            BankId.Norges,
            NorgesParser(),
            true
        )
        banks[BankId.Sweden] = Bank(
            context.getString(R.string.sweden),
            BankId.Sweden,
            SwedenParser(),
            true
        )
        banks[BankId.UK] = Bank(
            context.getString(R.string.uk),
            BankId.UK,
            EnglandParser(),
            false
        )
        return banks
    }
}