/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.tests.testutils.lambda

/**
 * A matcher that can be used to match parameters in lambda calls.
 * This is useful to assert that a lambda has been called with specific parameters.
 */
interface ParameterMatcher {
    fun match(param: Any?): Boolean
}

/**
 * A matcher that matches a specific value.
 * Can be used to assert that a lambda has been called with a specific value.
 */
fun <T> value(expectedValue: T) = object : ParameterMatcher {
    override fun match(param: Any?) = param == expectedValue
    override fun toString(): String = "value($expectedValue)"
}

/**
 * A matcher that matches a value based on a condition.
 * Can be used to assert that a lambda has been called with a value that satisfies a specific condition.
 */
fun <T> matching(check: (T) -> Boolean) = object : ParameterMatcher {
    override fun match(param: Any?): Boolean {
        @Suppress("UNCHECKED_CAST")
        return (param as? T)?.let { check(it) } ?: false
    }
    override fun toString(): String = "matching(condition)"
}

/**
 * A matcher that matches any value.
 * Can be used when we don't care about the value of a parameter.
 */
fun any() = object : ParameterMatcher {
    override fun match(param: Any?) = true
    override fun toString(): String = "any()"
}

/**
 * A matcher that matches any non null value
 * Can be used when we don't care about the value of a parameter, just about its nullability.
 */
fun nonNull() = object : ParameterMatcher {
    override fun match(param: Any?) = param != null
    override fun toString(): String = "nonNull()"
}
