package io.element.android.x.matrix

import android.annotation.SuppressLint
import android.content.Context
import kotlinx.coroutines.CoroutineScope


object MatrixInstance {
    @SuppressLint("StaticFieldLeak")
    private lateinit var instance: Matrix

    fun init(context: Context, coroutineScope: CoroutineScope) {
        //setupTracing("warn,matrix_sdk::sliding_sync=info")
        instance = Matrix(coroutineScope, context)
    }

    fun getInstance(): Matrix {
        return instance
    }
}