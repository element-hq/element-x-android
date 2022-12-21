package io.element.android.x.initializer

import android.content.Context
import androidx.startup.Initializer
import io.element.android.x.features.rageshake.crash.VectorUncaughtExceptionHandler

class CrashInitializer : Initializer<Unit> {

    override fun create(context: Context) {
        VectorUncaughtExceptionHandler(context).activate()
    }

    override fun dependencies(): List<Class<out Initializer<*>>> = emptyList()
}
