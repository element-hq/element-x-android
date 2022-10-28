package io.element.android.x.core.data

import kotlinx.coroutines.CoroutineDispatcher

data class CoroutineDispatchers(
    val io: CoroutineDispatcher,
    val computation: CoroutineDispatcher,
    val main: CoroutineDispatcher,
)
