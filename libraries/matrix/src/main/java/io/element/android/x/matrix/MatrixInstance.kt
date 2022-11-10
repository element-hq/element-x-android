package io.element.android.x.matrix

import android.annotation.SuppressLint
import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope


object MatrixInstance {
    @SuppressLint("StaticFieldLeak")
    private lateinit var instance: Matrix

    fun init(context: Context, coroutineScope: CoroutineScope) {
        instance = Matrix(coroutineScope, context)
    }

    fun getInstance(): Matrix {
        return instance
    }
}