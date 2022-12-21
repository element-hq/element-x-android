package io.element.android.x.initializer

import android.content.Context
import androidx.startup.Initializer
import timber.log.Timber

class TimberInitializer : Initializer<Unit> {

    override fun create(context: Context) {
        Timber.plant(Timber.DebugTree())
    }

    override fun dependencies(): List<Class<out Initializer<*>>> =
        listOf(TimberInitializer::class.java)
}
