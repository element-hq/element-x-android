/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
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

class EnsureCalledTimes(val times: Int) : () -> Unit {
    private var counter = 0
    override fun invoke() {
        counter++
    }

    fun assertSuccess() {
        if (counter != times) {
            throw AssertionError("Expected to be called $times, but was called $counter times")
        }
    }
}

fun ensureCalledOnce(block: (callback: () -> Unit) -> Unit) {
    val callback = EnsureCalledOnce()
    block(callback)
    callback.assertSuccess()
}

fun ensureCalledTimes(times: Int, block: (callback: () -> Unit) -> Unit) {
    val callback = EnsureCalledTimes(times)
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

class EnsureCalledOnceWithTwoParamsAndResult<T, U, R>(
    private val expectedParam1: T,
    private val expectedParam2: U,
    private val result: R,
) : (T, U) -> R {
    private var counter = 0
    override fun invoke(p1: T, p2: U): R {
        if (p1 != expectedParam1 || p2 != expectedParam2) {
            throw AssertionError("Expected to be called with $expectedParam1 and $expectedParam2, but was called with $p1 and $p2")
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

fun <P1, P2> ensureCalledOnceWithTwoParams(param1: P1, param2: P2, block: (callback: EnsureCalledOnceWithTwoParams<P1, P2>) -> Unit) {
    val callback = EnsureCalledOnceWithTwoParams(param1, param2)
    block(callback)
    callback.assertSuccess()
}
