package iuliiaponomareva.evroscudo.di

import android.content.Context
import dagger.BindsInstance
import dagger.Component
import iuliiaponomareva.evroscudo.displayrates.DisplayRatesActivity

@Component(modules = [AppModule::class])
interface AppComponent {

    @Component.Factory
    interface Factory {
        fun create(@BindsInstance context: Context): AppComponent
    }

    fun inject(activity: DisplayRatesActivity)
}