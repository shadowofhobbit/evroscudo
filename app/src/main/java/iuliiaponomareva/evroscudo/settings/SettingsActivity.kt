package iuliiaponomareva.evroscudo.settings

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.CheckBoxPreference
import androidx.preference.PreferenceFragmentCompat
import iuliiaponomareva.evroscudo.R


class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.settings, SettingsFragment())
            .commit()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }


    class SettingsFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setHasOptionsMenu(true)
            val screen = preferenceManager.createPreferenceScreen(activity)
            preferenceScreen = screen
            val currencies = resources.getStringArray(R.array.all_currencies)
            currencies.forEach {
                val checkBox = CheckBoxPreference(activity)
                checkBox.key = it
                checkBox.setDefaultValue(true)
                checkBox.title = it
                checkBox.summaryOn = resources.getString(R.string.display) + " $it"
                checkBox.summaryOff = resources.getString(R.string.no_display) + " $it"
                screen.addPreference(checkBox)
            }
        }
    }

}
