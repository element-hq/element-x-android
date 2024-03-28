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
 * A matcher that matches any value.
 * Can be used when we don't care about the value of a parameter.
 */
fun any() = object : ParameterMatcher {
    override fun match(param: Any?) = true
    override fun toString(): String = "any()"
}
