/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.architecture

import androidx.compose.runtime.MutableState
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import org.junit.Test

class AsyncDataKtTest {
    @Test
    fun `updates state when block returns success`() = runTest {
        val state = TestableMutableState<AsyncData<Int>>(AsyncData.Uninitialized)

        val result = runUpdatingState(state) {
            delay(1)
            Result.success(1)
        }

        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrNull()).isEqualTo(1)

        assertThat(state.popFirst()).isEqualTo(AsyncData.Uninitialized)
        assertThat(state.popFirst()).isEqualTo(AsyncData.Loading(null))
        assertThat(state.popFirst()).isEqualTo(AsyncData.Success(1))
        state.assertNoMoreValues()
    }

    @Test
    fun `updates state when block returns failure`() = runTest {
        val state = TestableMutableState<AsyncData<Int>>(AsyncData.Uninitialized)

        val result = runUpdatingState(state) {
            delay(1)
            Result.failure(MyException("hello"))
        }

        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isEqualTo(MyException("hello"))

        assertThat(state.popFirst()).isEqualTo(AsyncData.Uninitialized)
        assertThat(state.popFirst()).isEqualTo(AsyncData.Loading(null))
        assertThat(state.popFirst()).isEqualTo(AsyncData.Failure<Int>(MyException("hello")))
        state.assertNoMoreValues()
    }

    @Test
    fun `updates state when block returns failure transforming the error`() = runTest {
        val state = TestableMutableState<AsyncData<Int>>(AsyncData.Uninitialized)

        val result = runUpdatingState(state, { MyException(it.message + " world") }) {
            delay(1)
            Result.failure(MyException("hello"))
        }

        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isEqualTo(MyException("hello world"))

        assertThat(state.popFirst()).isEqualTo(AsyncData.Uninitialized)
        assertThat(state.popFirst()).isEqualTo(AsyncData.Loading(null))
        assertThat(state.popFirst()).isEqualTo(AsyncData.Failure<Int>(MyException("hello world")))
        state.assertNoMoreValues()
    }
}

/**
 * A fake [MutableState] that allows to record all the states that were set.
 */
private class TestableMutableState<T>(
    value: T
) : MutableState<T> {
    private val deque = ArrayDeque(listOf(value))

    override var value: T
        get() = deque.last()
        set(value) {
            deque.addLast(value)
        }

    /**
     * Returns the states that were set in the order they were set.
     */
    fun popFirst(): T = deque.removeFirst()

    fun assertNoMoreValues() {
        assertThat(deque).isEmpty()
    }

    override operator fun component1(): T = value

    override operator fun component2(): (T) -> Unit = { value = it }
}

/**
 * An exception that is also a data class so we can compare it using equals.
 */
private data class MyException(val myMessage: String) : Exception(myMessage)
