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
 * A recorder that can be used to record the parameters of lambda invocation.
 */
abstract class LambdaRecorder internal constructor(
    private val assertNoInvocation: Boolean,
) {
    private val parametersSequence: MutableList<List<Any?>> = mutableListOf()

    internal fun onInvoke(vararg params: Any?) {
        if (assertNoInvocation) {
            throw AssertionError("This lambda should never be called.")
        }
        parametersSequence.add(params.toList())
    }

    fun assertions(): LambdaRecorderAssertions {
        return LambdaRecorderAssertions(parametersSequence = parametersSequence)
    }
}

inline fun <reified R> lambdaRecorder(
    ensureNeverCalled: Boolean = false,
    noinline block: () -> R
): LambdaNoParamRecorder<R> {
    return LambdaNoParamRecorder(ensureNeverCalled, block)
}

inline fun <reified T, reified R> lambdaRecorder(
    ensureNeverCalled: Boolean = false,
    noinline block: (T) -> R
): LambdaOneParamRecorder<T, R> {
    return LambdaOneParamRecorder(ensureNeverCalled, block)
}

inline fun <reified T1, reified T2, reified R> lambdaRecorder(
    ensureNeverCalled: Boolean = false,
    noinline block: (T1, T2) -> R
): LambdaTwoParamsRecorder<T1, T2, R> {
    return LambdaTwoParamsRecorder(ensureNeverCalled, block)
}

inline fun <reified T1, reified T2, reified T3, reified R> lambdaRecorder(
    ensureNeverCalled: Boolean = false,
    noinline block: (T1, T2, T3) -> R
): LambdaThreeParamsRecorder<T1, T2, T3, R> {
    return LambdaThreeParamsRecorder(ensureNeverCalled, block)
}

inline fun <reified T1, reified T2, reified T3, reified T4, reified R> lambdaRecorder(
    ensureNeverCalled: Boolean = false,
    noinline block: (T1, T2, T3, T4) -> R
): LambdaFourParamsRecorder<T1, T2, T3, T4, R> {
    return LambdaFourParamsRecorder(ensureNeverCalled, block)
}

inline fun <reified T1, reified T2, reified T3, reified T4, reified T5, reified R> lambdaRecorder(
    ensureNeverCalled: Boolean = false,
    noinline block: (T1, T2, T3, T4, T5) -> R
): LambdaFiveParamsRecorder<T1, T2, T3, T4, T5, R> {
    return LambdaFiveParamsRecorder(ensureNeverCalled, block)
}

class LambdaNoParamRecorder<out R>(ensureNeverCalled: Boolean, val block: () -> R) : LambdaRecorder(ensureNeverCalled), () -> R {
    override fun invoke(): R {
        onInvoke()
        return block()
    }
}

class LambdaOneParamRecorder<in T, out R>(ensureNeverCalled: Boolean, val block: (T) -> R) : LambdaRecorder(ensureNeverCalled), (T) -> R {
    override fun invoke(p: T): R {
        onInvoke(p)
        return block(p)
    }
}

class LambdaTwoParamsRecorder<in T1, in T2, out R>(ensureNeverCalled: Boolean, val block: (T1, T2) -> R) : LambdaRecorder(ensureNeverCalled), (T1, T2) -> R {
    override fun invoke(p1: T1, p2: T2): R {
        onInvoke(p1, p2)
        return block(p1, p2)
    }
}

class LambdaThreeParamsRecorder<in T1, in T2, in T3, out R>(ensureNeverCalled: Boolean, val block: (T1, T2, T3) -> R) : LambdaRecorder(
    ensureNeverCalled
), (T1, T2, T3) -> R {
    override fun invoke(p1: T1, p2: T2, p3: T3): R {
        onInvoke(p1, p2, p3)
        return block(p1, p2, p3)
    }
}

class LambdaFourParamsRecorder<in T1, in T2, in T3, in T4, out R>(ensureNeverCalled: Boolean, val block: (T1, T2, T3, T4) -> R) : LambdaRecorder(
    ensureNeverCalled
), (T1, T2, T3, T4) -> R {
    override fun invoke(p1: T1, p2: T2, p3: T3, p4: T4): R {
        onInvoke(p1, p2, p3, p4)
        return block(p1, p2, p3, p4)
    }
}

class LambdaFiveParamsRecorder<in T1, in T2, in T3, in T4, in T5, out R>(ensureNeverCalled: Boolean, val block: (T1, T2, T3, T4, T5) -> R) : LambdaRecorder(
    ensureNeverCalled
), (T1, T2, T3, T4, T5) -> R {
    override fun invoke(p1: T1, p2: T2, p3: T3, p4: T4, p5: T5): R {
        onInvoke(p1, p2, p3, p4, p5)
        return block(p1, p2, p3, p4, p5)
    }
}
