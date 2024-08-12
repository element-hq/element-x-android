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
            lambdaError()
        }
        parametersSequence.add(params.toList())
    }

    fun assertions(): LambdaRecorderAssertions {
        return LambdaRecorderAssertions(parametersSequence = parametersSequence)
    }
}

/**
 * A recorder that can be used to record the parameters of a suspend lambda invocation.
 */
abstract class LambdaSuspendRecorder internal constructor(
    private val assertNoInvocation: Boolean,
) {
    private val parametersSequence: MutableList<List<Any?>> = mutableListOf()

    internal suspend fun onInvoke(vararg params: Any?) {
        if (assertNoInvocation) {
            lambdaError()
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

inline fun <reified T1, reified T2, reified T3, reified T4, reified T5, reified T6, reified R> lambdaRecorder(
    ensureNeverCalled: Boolean = false,
    noinline block: (T1, T2, T3, T4, T5, T6) -> R
): LambdaSixParamsRecorder<T1, T2, T3, T4, T5, T6, R> {
    return LambdaSixParamsRecorder(ensureNeverCalled, block)
}

inline fun <reified T1, reified T2, reified T3, reified T4, reified T5, reified T6, reified T7, reified R> lambdaRecorder(
    ensureNeverCalled: Boolean = false,
    noinline block: (T1, T2, T3, T4, T5, T6, T7) -> R
): LambdaSevenParamsRecorder<T1, T2, T3, T4, T5, T6, T7, R> {
    return LambdaSevenParamsRecorder(ensureNeverCalled, block)
}

inline fun <reified R> lambdaAnyRecorder(
    ensureNeverCalled: Boolean = false,
    noinline block: (List<Any?>) -> R
): LambdaListAnyParamsRecorder<R> {
    return LambdaListAnyParamsRecorder(ensureNeverCalled, block)
}

// Suspend versions

inline fun <reified R> lambdaSuspendRecorder(
    ensureNeverCalled: Boolean = false,
    noinline block: () -> R
): LambdaNoParamRecorder<R> {
    return LambdaNoParamRecorder(ensureNeverCalled, block)
}

inline fun <reified T, reified R> lambdaSuspendRecorder(
    ensureNeverCalled: Boolean = false,
    noinline block: suspend (T) -> R
): LambdaOneParamSuspendRecorder<T, R> {
    return LambdaOneParamSuspendRecorder(ensureNeverCalled, block)
}

inline fun <reified T1, reified T2, reified R> lambdaSuspendRecorder(
    ensureNeverCalled: Boolean = false,
    noinline block: suspend (T1, T2) -> R
): LambdaTwoParamsSuspendRecorder<T1, T2, R> {
    return LambdaTwoParamsSuspendRecorder(ensureNeverCalled, block)
}

inline fun <reified T1, reified T2, reified T3, reified R> lambdaSuspendRecorder(
    ensureNeverCalled: Boolean = false,
    noinline block: suspend (T1, T2, T3) -> R
): LambdaSuspendThreeParamsRecorder<T1, T2, T3, R> {
    return LambdaSuspendThreeParamsRecorder(ensureNeverCalled, block)
}

inline fun <reified T1, reified T2, reified T3, reified T4, reified R> lambdaSuspendRecorder(
    ensureNeverCalled: Boolean = false,
    noinline block: suspend (T1, T2, T3, T4) -> R
): LambdaFourParamsSuspendRecorder<T1, T2, T3, T4, R> {
    return LambdaFourParamsSuspendRecorder(ensureNeverCalled, block)
}

inline fun <reified T1, reified T2, reified T3, reified T4, reified T5, reified R> lambdaSuspendRecorder(
    ensureNeverCalled: Boolean = false,
    noinline block: suspend (T1, T2, T3, T4, T5) -> R
): LambdaFiveParamsSuspendRecorder<T1, T2, T3, T4, T5, R> {
    return LambdaFiveParamsSuspendRecorder(ensureNeverCalled, block)
}

inline fun <reified T1, reified T2, reified T3, reified T4, reified T5, reified T6, reified R> lambdaSuspendRecorder(
    ensureNeverCalled: Boolean = false,
    noinline block: suspend (T1, T2, T3, T4, T5, T6) -> R
): LambdaSixParamsSuspendRecorder<T1, T2, T3, T4, T5, T6, R> {
    return LambdaSixParamsSuspendRecorder(ensureNeverCalled, block)
}

inline fun <reified T1, reified T2, reified T3, reified T4, reified T5, reified T6, reified T7, reified R> lambdaSuspendRecorder(
    ensureNeverCalled: Boolean = false,
    noinline block: suspend (T1, T2, T3, T4, T5, T6, T7) -> R
): LambdaSevenParamsSuspendRecorder<T1, T2, T3, T4, T5, T6, T7, R> {
    return LambdaSevenParamsSuspendRecorder(ensureNeverCalled, block)
}

inline fun <reified R> lambdaSuspendAnyRecorder(
    ensureNeverCalled: Boolean = false,
    noinline block: suspend (List<Any?>) -> R
): LambdaListAnyParamsSuspendRecorder<R> {
    return LambdaListAnyParamsSuspendRecorder(ensureNeverCalled, block)
}

class LambdaNoParamRecorder<out R>(ensureNeverCalled: Boolean, val block: () -> R) : LambdaRecorder(ensureNeverCalled), () -> R {
    override fun invoke(): R {
        onInvoke()
        return block()
    }
}

class LambdaNoParamSuspendRecorder<out R>(ensureNeverCalled: Boolean, val block: suspend () -> R) : LambdaRecorder(ensureNeverCalled), suspend () -> R {
    override suspend fun invoke(): R {
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

class LambdaOneParamSuspendRecorder<in T, out R>(ensureNeverCalled: Boolean, val block: suspend (T) -> R) : LambdaRecorder(
    ensureNeverCalled
), suspend (T) -> R {
    override suspend fun invoke(p: T): R {
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

class LambdaTwoParamsSuspendRecorder<in T1, in T2, out R>(ensureNeverCalled: Boolean, val block: suspend (T1, T2) -> R) : LambdaRecorder(
    ensureNeverCalled
), suspend (T1, T2) -> R {
    override suspend fun invoke(p1: T1, p2: T2): R {
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

class LambdaSuspendThreeParamsRecorder<in T1, in T2, in T3, out R>(ensureNeverCalled: Boolean, val block: suspend (T1, T2, T3) -> R) : LambdaSuspendRecorder(
    ensureNeverCalled
), suspend (T1, T2, T3) -> R {
    override suspend fun invoke(p1: T1, p2: T2, p3: T3): R {
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

class LambdaFourParamsSuspendRecorder<in T1, in T2, in T3, in T4, out R>(
    ensureNeverCalled: Boolean,
    val block: suspend (T1, T2, T3, T4) -> R
) : LambdaRecorder(
    ensureNeverCalled
), suspend (T1, T2, T3, T4) -> R {
    override suspend fun invoke(p1: T1, p2: T2, p3: T3, p4: T4): R {
        onInvoke(p1, p2, p3, p4)
        return block(p1, p2, p3, p4)
    }
}

class LambdaFiveParamsRecorder<in T1, in T2, in T3, in T4, in T5, out R>(
    ensureNeverCalled: Boolean,
    val block: (T1, T2, T3, T4, T5) -> R,
) : LambdaRecorder(
    ensureNeverCalled
), (T1, T2, T3, T4, T5) -> R {
    override fun invoke(p1: T1, p2: T2, p3: T3, p4: T4, p5: T5): R {
        onInvoke(p1, p2, p3, p4, p5)
        return block(p1, p2, p3, p4, p5)
    }
}

class LambdaFiveParamsSuspendRecorder<in T1, in T2, in T3, in T4, in T5, out R>(
    ensureNeverCalled: Boolean,
    val block: suspend (T1, T2, T3, T4, T5) -> R
) : LambdaRecorder(
    ensureNeverCalled
), suspend (T1, T2, T3, T4, T5) -> R {
    override suspend fun invoke(p1: T1, p2: T2, p3: T3, p4: T4, p5: T5): R {
        onInvoke(p1, p2, p3, p4, p5)
        return block(p1, p2, p3, p4, p5)
    }
}

class LambdaSixParamsRecorder<in T1, in T2, in T3, in T4, in T5, in T6, out R>(
    ensureNeverCalled: Boolean,
    val block: (T1, T2, T3, T4, T5, T6) -> R,
) : LambdaRecorder(ensureNeverCalled), (T1, T2, T3, T4, T5, T6) -> R {
    override fun invoke(p1: T1, p2: T2, p3: T3, p4: T4, p5: T5, p6: T6): R {
        onInvoke(p1, p2, p3, p4, p5, p6)
        return block(p1, p2, p3, p4, p5, p6)
    }
}

class LambdaSixParamsSuspendRecorder<in T1, in T2, in T3, in T4, in T5, in T6, out R>(
    ensureNeverCalled: Boolean,
    val block: suspend (T1, T2, T3, T4, T5, T6) -> R,
) : LambdaRecorder(ensureNeverCalled), suspend (T1, T2, T3, T4, T5, T6) -> R {
    override suspend fun invoke(p1: T1, p2: T2, p3: T3, p4: T4, p5: T5, p6: T6): R {
        onInvoke(p1, p2, p3, p4, p5, p6)
        return block(p1, p2, p3, p4, p5, p6)
    }
}

class LambdaSevenParamsRecorder<in T1, in T2, in T3, in T4, in T5, in T6, in T7, out R>(
    ensureNeverCalled: Boolean,
    val block: (T1, T2, T3, T4, T5, T6, T7) -> R,
) : LambdaRecorder(ensureNeverCalled), (T1, T2, T3, T4, T5, T6, T7) -> R {
    override fun invoke(p1: T1, p2: T2, p3: T3, p4: T4, p5: T5, p6: T6, p7: T7): R {
        onInvoke(p1, p2, p3, p4, p5, p6, p7)
        return block(p1, p2, p3, p4, p5, p6, p7)
    }
}

class LambdaSevenParamsSuspendRecorder<in T1, in T2, in T3, in T4, in T5, in T6, in T7, out R>(
    ensureNeverCalled: Boolean,
    val block: suspend (T1, T2, T3, T4, T5, T6, T7) -> R,
) : LambdaRecorder(ensureNeverCalled), suspend (T1, T2, T3, T4, T5, T6, T7) -> R {
    override suspend fun invoke(p1: T1, p2: T2, p3: T3, p4: T4, p5: T5, p6: T6, p7: T7): R {
        onInvoke(p1, p2, p3, p4, p5, p6, p7)
        return block(p1, p2, p3, p4, p5, p6, p7)
    }
}

class LambdaEightParamsRecorder<in T1, in T2, in T3, in T4, in T5, in T6, in T7, in T8, out R>(
    ensureNeverCalled: Boolean,
    val block: (T1, T2, T3, T4, T5, T6, T7, T8) -> R,
) : LambdaRecorder(ensureNeverCalled), (T1, T2, T3, T4, T5, T6, T7, T8) -> R {
    override fun invoke(p1: T1, p2: T2, p3: T3, p4: T4, p5: T5, p6: T6, p7: T7, p8: T8): R {
        onInvoke(p1, p2, p3, p4, p5, p6, p7, p8)
        return block(p1, p2, p3, p4, p5, p6, p7, p8)
    }
}

class LambdaEightParamsSuspendRecorder<in T1, in T2, in T3, in T4, in T5, in T6, in T7, in T8, out R>(
    ensureNeverCalled: Boolean,
    val block: suspend (T1, T2, T3, T4, T5, T6, T7, T8) -> R,
) : LambdaRecorder(ensureNeverCalled), suspend (T1, T2, T3, T4, T5, T6, T7, T8) -> R {
    override suspend fun invoke(p1: T1, p2: T2, p3: T3, p4: T4, p5: T5, p6: T6, p7: T7, p8: T8): R {
        onInvoke(p1, p2, p3, p4, p5, p6, p7, p8)
        return block(p1, p2, p3, p4, p5, p6, p7, p8)
    }
}

class LambdaNineParamsRecorder<in T1, in T2, in T3, in T4, in T5, in T6, in T7, in T8, in T9, out R>(
    ensureNeverCalled: Boolean,
    val block: (T1, T2, T3, T4, T5, T6, T7, T8, T9) -> R,
) : LambdaRecorder(ensureNeverCalled), (T1, T2, T3, T4, T5, T6, T7, T8, T9) -> R {
    override fun invoke(p1: T1, p2: T2, p3: T3, p4: T4, p5: T5, p6: T6, p7: T7, p8: T8, p9: T9): R {
        onInvoke(p1, p2, p3, p4, p5, p6, p7, p8, p9)
        return block(p1, p2, p3, p4, p5, p6, p7, p8, p9)
    }
}

class LambdaNineParamsSuspendRecorder<in T1, in T2, in T3, in T4, in T5, in T6, in T7, in T8, in T9, out R>(
    ensureNeverCalled: Boolean,
    val block: suspend (T1, T2, T3, T4, T5, T6, T7, T8, T9) -> R,
) : LambdaRecorder(ensureNeverCalled), suspend (T1, T2, T3, T4, T5, T6, T7, T8, T9) -> R {
    override suspend fun invoke(p1: T1, p2: T2, p3: T3, p4: T4, p5: T5, p6: T6, p7: T7, p8: T8, p9: T9): R {
        onInvoke(p1, p2, p3, p4, p5, p6, p7, p8, p9)
        return block(p1, p2, p3, p4, p5, p6, p7, p8, p9)
    }
}

class LambdaListAnyParamsRecorder<out R>(
    ensureNeverCalled: Boolean,
    val block: (List<Any?>) -> R,
) : LambdaRecorder(ensureNeverCalled), (List<Any?>) -> R {
    override fun invoke(p: List<Any?>): R {
        onInvoke(*p.toTypedArray())
        return block(p)
    }
}

class LambdaListAnyParamsSuspendRecorder<out R>(
    ensureNeverCalled: Boolean,
    val block: suspend (List<Any?>) -> R,
) : LambdaSuspendRecorder(ensureNeverCalled), suspend (List<Any?>) -> R {
    override suspend fun invoke(p: List<Any?>): R {
        onInvoke(*p.toTypedArray())
        return block(p)
    }
}
