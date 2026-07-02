package org.aals.family.chore

import android.app.Application
import org.aals.family.chore.core.domain.util.LoggingInitializer
import org.aals.family.chore.di.initKoin
import org.koin.android.ext.koin.androidContext

class FamilyChoreApp : Application() {
    override fun onCreate() {
        super.onCreate()
        LoggingInitializer.init()
        initKoin {
            androidContext(this@FamilyChoreApp)
        }
    }
}
