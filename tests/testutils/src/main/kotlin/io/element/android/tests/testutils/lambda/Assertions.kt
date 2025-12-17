/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.tests.testutils.lambda

fun assert(lambdaRecorder: LambdaRecorder): LambdaRecorderAssertions {
    return lambdaRecorder.assertions()
}

class LambdaRecorderAssertions internal constructor(
    private val parametersSequence: List<List<Any?>>,
) {
    fun isCalledOnce(): CalledOnceParametersAssertions {
        return CalledOnceParametersAssertions(
            assertions = isCalledExactly(1)
        )
    }

    fun isNeverCalled() {
        isCalledExactly(0)
    }

    fun isCalledExactly(times: Int): ParametersAssertions {
        if (parametersSequence.size != times) {
            throw AssertionError("Expected to be called $times, but was called ${parametersSequence.size} times")
        }
        return ParametersAssertions(parametersSequence)
    }
}

class CalledOnceParametersAssertions internal constructor(private val assertions: ParametersAssertions) {
    fun with(vararg matchers: ParameterMatcher) {
        assertions.withSequence(matchers.toList())
    }

    fun withNoParameter() {
        assertions.withNoParameter()
    }
}

class ParametersAssertions internal constructor(
    private val parametersSequence: List<List<Any?>>
) {
    fun withSequence(vararg matchersSequence: List<ParameterMatcher>) {
        if (parametersSequence.size != matchersSequence.size) {
            throw AssertionError("Lambda was called ${parametersSequence.size} times, but only ${matchersSequence.size} assertions were provided")
        }
        parametersSequence.zip(matchersSequence).forEachIndexed { invocationIndex, (parameters, matchers) ->
            if (parameters.size != matchers.size) {
                throw AssertionError("Expected ${matchers.size} parameters, but got ${parameters.size} parameters during invocation #$invocationIndex")
            }
            parameters.zip(matchers).forEachIndexed { paramIndex, (param, matcher) ->
                if (!matcher.match(param)) {
                    throw AssertionError(
                        "Parameter #$paramIndex does not match the expected value (actual=$param,expected=$matcher) during invocation #$invocationIndex"
                    )
                }
            }
        }
    }

    fun withNoParameter() {
        if (parametersSequence.any { it.isNotEmpty() }) {
            throw AssertionError("Expected no parameters, but got some")
        }
    }
}
