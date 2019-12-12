package iuliiaponomareva.evroscudo.di

import dagger.Module
import dagger.Provides
import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers

@Module
class SchedulersModule {
    @Provides
    fun provideScheduler(): Scheduler = AndroidSchedulers.mainThread()
}