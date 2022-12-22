package io.element.android.x.initializer

import android.content.Context
import androidx.startup.Initializer
import io.element.android.x.BuildConfig
import io.element.android.x.features.rageshake.logs.VectorFileLogger
import timber.log.Timber

class TimberInitializer : Initializer<Unit> {

    override fun create(context: Context) {
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
        Timber.plant(VectorFileLogger(context))
    }

    override fun dependencies(): List<Class<out Initializer<*>>> = emptyList()
}
