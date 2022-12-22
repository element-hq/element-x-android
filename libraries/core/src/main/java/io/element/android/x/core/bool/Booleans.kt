package io.element.android.x.core.bool

fun Boolean?.orTrue() = this ?: true

fun Boolean?.orFalse() = this ?: false
