package iuliiaponomareva.evroscudo.settings

import android.os.Bundle
import android.preference.PreferenceActivity
import android.view.MenuItem
import androidx.preference.CheckBoxPreference
import androidx.preference.PreferenceFragment
import iuliiaponomareva.evroscudo.R


/**
 * A [PreferenceActivity] that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 *
 * See [Android Design: Settings](http://developer.android.com/design/patterns/settings.html)
 * for design guidelines and the [Settings API Guide](http://developer.android.com/guide/topics/ui/settings.html)
 * for more information on developing a Settings UI.
 */
class SettingsActivity : AppCompatPreferenceActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupActionBar()
        // load settings fragment
        fragmentManager.beginTransaction().replace(android.R.id.content, GeneralPreferenceFragment())
            .commit()
    }

    private fun setupActionBar() {
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onMenuItemSelected(featureId: Int, item: MenuItem): Boolean {
        val id = item.itemId
        if (id == android.R.id.home) {
           onBackPressed()
        }
        return super.onMenuItemSelected(featureId, item)
    }


    class GeneralPreferenceFragment : PreferenceFragment() {
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
