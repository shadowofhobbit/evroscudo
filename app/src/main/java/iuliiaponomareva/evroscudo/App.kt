package iuliiaponomareva.evroscudo

import android.app.Application
import iuliiaponomareva.evroscudo.di.AppComponent
import iuliiaponomareva.evroscudo.di.DaggerAppComponent

class App: Application() {
    val appComponent: AppComponent by lazy {
        DaggerAppComponent.factory().create(applicationContext)
    }
}