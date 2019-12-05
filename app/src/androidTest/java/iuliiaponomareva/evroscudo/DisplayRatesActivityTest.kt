package iuliiaponomareva.evroscudo

import androidx.test.InstrumentationRegistry
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.rule.ActivityTestRule
import androidx.test.runner.AndroidJUnit4
import iuliiaponomareva.evroscudo.displayrates.DisplayRatesActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DisplayRatesActivityTest {
    @get:Rule
    val rule = ActivityTestRule<DisplayRatesActivity>(
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
        openActionBarOverflowOrOptionsMenu(InstrumentationRegistry.getContext())
        onView(withText(R.string.info)).check(matches(isDisplayed()))
        onView(withText(R.string.info)).perform(click())
        onView(withText(R.string.info_from)).check(matches(isDisplayed()))
        onView(withText(R.string.ok)).check(matches(isDisplayed()))
    }
}