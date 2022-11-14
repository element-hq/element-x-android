package io.element.android.x.core.coroutine

import kotlinx.coroutines.CoroutineDispatcher

data class CoroutineDispatchers(
    val io: CoroutineDispatcher,
    val computation: CoroutineDispatcher,
    val main: CoroutineDispatcher,
    val diffUpdateDispatcher: CoroutineDispatcher,
)
