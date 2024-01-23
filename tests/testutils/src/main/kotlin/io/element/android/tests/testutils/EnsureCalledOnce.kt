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

fun ensureCalledOnce(block: (callback: EnsureCalledOnce) -> Unit) {
    val callback = EnsureCalledOnce()
    block(callback)
    callback.assertSuccess()
}

class EnsureCalledOnceWithParam<T>(
    private val expectedParam: T
) : (T) -> Unit {
    private var counter = 0
    override fun invoke(p1: T) {
        if (p1 != expectedParam) {
            throw AssertionError("Expected to be called with $expectedParam, but was called with $p1")
        }
        counter++
    }

    fun assertSuccess() {
        if (counter != 1) {
            throw AssertionError("Expected to be called once, but was called $counter times")
        }
    }
}

fun <T> ensureCalledOnceWithParam(param: T, block: (callback: EnsureCalledOnceWithParam<T>) -> Unit) {
    val callback = EnsureCalledOnceWithParam(param)
    block(callback)
    callback.assertSuccess()
}
