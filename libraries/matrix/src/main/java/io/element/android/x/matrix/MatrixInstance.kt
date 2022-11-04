package io.element.android.x.matrix

import android.annotation.SuppressLint
import android.content.Context
import kotlinx.coroutines.GlobalScope


object MatrixInstance {
    @SuppressLint("StaticFieldLeak")
    private lateinit var instance: Matrix

    fun init(context: Context) {
        instance = Matrix(GlobalScope, context)
    }

    fun getInstance(): Matrix {
        return instance
    }
}