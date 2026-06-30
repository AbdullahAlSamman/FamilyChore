package org.aals.family.chore

import android.app.Application
import org.aals.family.chore.di.initKoin
import org.koin.android.ext.koin.androidContext

class FamilyChoreApp : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin {
            androidContext(this@FamilyChoreApp)
        }
    }
}
