package iuliiaponomareva.evroscudo.displayrates

import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers
import androidx.test.espresso.intent.rule.IntentsTestRule
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import iuliiaponomareva.evroscudo.R
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DisplayRatesActivityTest {
    @get:Rule
    val rule = IntentsTestRule<DisplayRatesActivity>(
        DisplayRatesActivity::class.java)

    @Test
    fun viewsAreShown() {
        onView(withId(R.id.spinner1)).check(matches(isDisplayed()))
        onView(withId(R.id.spinner2)).check(matches(isDisplayed()))
        onView(withId(R.id.dateView1)).check(matches(isDisplayed()))
        onView(withId(R.id.dateView2)).check(matches(isDisplayed()))
        onView(withId(R.id.ratesView)).check(matches(isDisplayed()))
    }

    @Test
    fun checkInfoDialog() {
        openActionBarOverflowOrOptionsMenu(ApplicationProvider.getApplicationContext())
        onView(withText(R.string.info)).check(matches(isDisplayed()))
        onView(withText(R.string.info)).perform(click())
        onView(withText(R.string.info_from)).check(matches(isDisplayed()))
        onView(withText(R.string.ok)).check(matches(isDisplayed()))
    }

    @Test
    fun checkSettings() {
        openActionBarOverflowOrOptionsMenu(ApplicationProvider.getApplicationContext())
        onView(withText(R.string.settings)).check(matches(isDisplayed()))
        onView(withText(R.string.settings)).perform(click())
        intended(IntentMatchers.hasComponent("iuliiaponomareva.evroscudo.settings.SettingsActivity"))
    }
}