package io.element.android.x

import android.app.Application
import com.airbnb.mvrx.Mavericks
import io.element.android.x.matrix.MatrixInstance
import timber.log.Timber

class ElementXApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())
        MatrixInstance.init(this)
        Mavericks.initialize(this)
    }
}