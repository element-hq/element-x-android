package io.element.android.x.matrix

import android.annotation.SuppressLint
import android.app.Application
import kotlinx.coroutines.CoroutineScope


object MatrixInstance {
    @SuppressLint("StaticFieldLeak")
    private lateinit var instance: Matrix

    fun init(context: Application, coroutineScope: CoroutineScope) {
        instance = Matrix(coroutineScope, context)
    }

    fun getInstance(): Matrix {
        return instance
    }
}