package io.element.android.x.initializer

import android.content.Context
import androidx.startup.Initializer
import io.element.android.x.matrix.tracing.TracingConfigurations
import io.element.android.x.matrix.tracing.setupTracing
import io.element.android.x.sdk.matrix.BuildConfig

class MatrixInitializer : Initializer<Unit> {

    override fun create(context: Context) {
        if (BuildConfig.DEBUG) {
            setupTracing(TracingConfigurations.debug)
        } else {
            setupTracing(TracingConfigurations.release)
        }
    }

    override fun dependencies(): List<Class<out Initializer<*>>> = emptyList()
}
