package iuliiaponomareva.evroscudo.di

import dagger.Binds
import dagger.Module
import iuliiaponomareva.evroscudo.displayrates.DisplayRatesContract
import iuliiaponomareva.evroscudo.displayrates.DisplayRatesModel
import iuliiaponomareva.evroscudo.displayrates.DisplayRatesPresenter
import javax.inject.Singleton

@Module
abstract class AppModule {
    @Binds
    abstract fun providePresenter(presenter: DisplayRatesPresenter): DisplayRatesContract.Presenter

    @Binds
    @Singleton
    abstract fun provideModel(model: DisplayRatesModel): DisplayRatesContract.Model
}