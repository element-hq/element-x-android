package io.element.android.x.matrix

import android.annotation.SuppressLint
import android.app.Application
import io.element.android.x.matrix.tracing.TracingConfigurations
import io.element.android.x.matrix.tracing.setupTracing
import io.element.android.x.sdk.matrix.BuildConfig
import kotlinx.coroutines.CoroutineScope


object MatrixInstance {
    @SuppressLint("StaticFieldLeak")
    private lateinit var instance: Matrix

    fun init(context: Application, coroutineScope: CoroutineScope) {
        if (BuildConfig.DEBUG) {
            setupTracing(TracingConfigurations.debug)
        } else {
            setupTracing(TracingConfigurations.release)
        }
        instance = Matrix(coroutineScope, context)
    }

    fun getInstance(): Matrix {
        return instance
    }
}