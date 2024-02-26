/*
 * Copyright (c) 2024 New Vector Ltd
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

package io.element.android.tests.testutils

class EnsureCalledOnce : () -> Unit {
    private var counter = 0
    override fun invoke() {
        counter++
    }

    fun assertSuccess() {
        if (counter != 1) {
            throw AssertionError("Expected to be called once, but was called $counter times")
        }
    }
}

fun ensureCalledOnce(block: (callback: () -> Unit) -> Unit) {
    val callback = EnsureCalledOnce()
    block(callback)
    callback.assertSuccess()
}

class EnsureCalledOnceWithParam<T, R>(
    private val expectedParam: T,
    private val result: R,
) : (T) -> R {
    private var counter = 0
    override fun invoke(p1: T): R {
        if (p1 != expectedParam) {
            throw AssertionError("Expected to be called with $expectedParam, but was called with $p1")
        }
        counter++
        return result
    }

    fun assertSuccess() {
        if (counter != 1) {
            throw AssertionError("Expected to be called once, but was called $counter times")
        }
    }
}

class EnsureCalledOnceWithTwoParams<T, U>(
    private val expectedParam1: T,
    private val expectedParam2: U,
) : (T, U) -> Unit {
    private var counter = 0
    override fun invoke(p1: T, p2: U) {
        if (p1 != expectedParam1 || p2 != expectedParam2) {
            throw AssertionError("Expected to be called with $expectedParam1 and $expectedParam2, but was called with $p1 and $p2")
        }
        counter++
    }

    fun assertSuccess() {
        if (counter != 1) {
            throw AssertionError("Expected to be called once, but was called $counter times")
        }
    }
}

/**
 * Shortcut for [<T, R> ensureCalledOnceWithParam] with Unit result.
 */
fun <T> ensureCalledOnceWithParam(param: T, block: (callback: EnsureCalledOnceWithParam<T, Unit>) -> Unit) {
    ensureCalledOnceWithParam(param, block, Unit)
}

fun <T, R> ensureCalledOnceWithParam(param: T, block: (callback: EnsureCalledOnceWithParam<T, R>) -> R, result: R) {
    val callback = EnsureCalledOnceWithParam(param, result)
    block(callback)
    callback.assertSuccess()
}
