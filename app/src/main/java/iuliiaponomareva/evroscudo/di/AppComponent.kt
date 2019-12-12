package iuliiaponomareva.evroscudo.di

import android.content.Context
import dagger.BindsInstance
import dagger.Component
import iuliiaponomareva.evroscudo.displayrates.DisplayRatesActivity
import javax.inject.Singleton

@Singleton
@Component(modules = [AppModule::class, DataModule::class, SchedulersModule::class])
interface AppComponent {

    @Component.Factory
    interface Factory {
        fun create(@BindsInstance context: Context): AppComponent
    }

    fun inject(activity: DisplayRatesActivity)
}