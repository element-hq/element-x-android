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
            throw AssertionError("Expected ${matchersSequence.size} parameters, but got ${parametersSequence.size} parameters")
        }
        parametersSequence.zip(matchersSequence).forEach { (parameters, matchers) ->
            if (parameters.size != matchers.size) {
                throw AssertionError("Expected ${matchers.size} parameters, but got ${parameters.size} parameters")
            }
            parameters.zip(matchers).forEachIndexed { j, (param, matcher) ->
                if (!matcher.match(param)) {
                    throw AssertionError("Parameter $j does not match the expected value")
                }
            }
        }
        for (i in parametersSequence.indices) {
            val params = parametersSequence[i]
            val checker = matchersSequence[i]
            if (params.size != checker.size) {
                throw AssertionError("Expected ${checker.size} parameters, but got ${params.size} parameters")
            }
            for (j in params.indices) {
                val param = params[j]
                val check = checker[j]
                if (!check.match(param)) {
                    throw AssertionError("Parameter $j does not match the expected value")
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
