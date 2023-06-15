/*
 * Copyright (c) 2023 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.element.android.libraries.architecture

import androidx.compose.runtime.MutableState
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import org.junit.Test

class AsyncKtTest {
    @Test
    fun `updates state when block returns success`() = runTest {
        val state = TestableMutableState<Async<Int>>(Async.Uninitialized)

        val result = runUpdatingState(state) {
            delay(1)
            Result.success(1)
        }

        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrNull()).isEqualTo(1)

        assertThat(state.pop()).isEqualTo(Async.Uninitialized)
        assertThat(state.pop()).isEqualTo(Async.Loading(null))
        assertThat(state.pop()).isEqualTo(Async.Success(1))
        state.assertNoMoreValues()
    }

    @Test
    fun `updates state when block returns failure`() = runTest {
        val state = TestableMutableState<Async<Int>>(Async.Uninitialized)

        val result = runUpdatingState(state) {
            delay(1)
            Result.failure(MyThrowable("hello"))
        }

        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isEqualTo(MyThrowable("hello"))

        assertThat(state.pop()).isEqualTo(Async.Uninitialized)
        assertThat(state.pop()).isEqualTo(Async.Loading(null))
        assertThat(state.pop()).isEqualTo(Async.Failure<Int>(MyThrowable("hello")))
        state.assertNoMoreValues()
    }

    @Test
    fun `updates state when block returns failure transforming the error`() = runTest {
        val state = TestableMutableState<Async<Int>>(Async.Uninitialized)

        val result = runUpdatingState(state, { MyThrowable(it.message + " world") }) {
            delay(1)
            Result.failure(MyThrowable("hello"))
        }

        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isEqualTo(MyThrowable("hello world"))

        assertThat(state.pop()).isEqualTo(Async.Uninitialized)
        assertThat(state.pop()).isEqualTo(Async.Loading(null))
        assertThat(state.pop()).isEqualTo(Async.Failure<Int>(MyThrowable("hello world")))
        state.assertNoMoreValues()
    }
}

/**
 * A fake [MutableState] that allows to record all the states that were set.
 */
private class TestableMutableState<T>(
    value: T
) : MutableState<T> {

    private val _deque = ArrayDeque<T>(listOf(value))

    override var value: T
        get() = _deque.last()
        set(value) {
            _deque.addLast(value)
        }

    /**
     * Returns the states that were set in the order they were set.
     */
    fun pop(): T = _deque.removeFirst()

    fun assertNoMoreValues() {
        assertThat(_deque).isEmpty()
    }

    override operator fun component1(): T = value

    override operator fun component2(): (T) -> Unit = { value = it }
}

/**
 * An exception that is also a data class so we can compare it using equals.
 */
private data class MyThrowable(val myMessage: String) : Throwable(myMessage)
