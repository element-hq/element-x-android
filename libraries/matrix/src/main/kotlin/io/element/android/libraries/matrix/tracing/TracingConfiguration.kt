/*
 * Copyright (c) 2022 New Vector Ltd
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

package io.element.android.libraries.matrix.tracing

data class TracingConfiguration(
    val overrides: Map<Target, LogLevel> = emptyMap()
) {

    private val targets = mapOf(
        Target.Common to LogLevel.Warn,
        Target.Hyper to LogLevel.Warn,
        Target.Sled to LogLevel.Warn,
        Target.MatrixSdk.Sled to LogLevel.Warn,
        Target.MatrixSdk.HttpClient to LogLevel.Trace,
        Target.MatrixSdk.SlidingSync to LogLevel.Trace,
        Target.MatrixSdk.BaseSlidingSync to LogLevel.Trace
    )

    val filter: String
        get() {
            val newTargets = HashMap(targets)
            overrides.forEach { (target, logLevel) ->
                newTargets[target] = logLevel
            }
            return newTargets.map { "${it.key.filter}=${it.value.filter}" }.joinToString(separator = ",")
        }
}

sealed class Target(open val filter: String) {
    object Common : Target("common")
    object Hyper : Target("hyper")
    object Sled : Target("sled")
    sealed class MatrixSdk(override val filter: String) : Target(filter) {
        object Root : MatrixSdk("matrix_sdk")
        object Sled : MatrixSdk("matrix_sdk_sled")
        object FFI : MatrixSdk("matrix_sdk_ffi")
        object HttpClient : MatrixSdk("matrix_sdk::http_client")
        object UniffiAPI : MatrixSdk("matrix_sdk_ffi::uniffi_api")
        object SlidingSync : MatrixSdk("matrix_sdk::sliding_sync")
        object BaseSlidingSync : MatrixSdk("matrix_sdk_base::sliding_sync")
    }
}

sealed class LogLevel(val filter: String) {
    object Warn : LogLevel("warn")
    object Trace : LogLevel("trace")
    object Info : LogLevel("info")
    object Debug : LogLevel("debug")
    object Error : LogLevel("error")
}

fun setupTracing(tracingConfiguration: TracingConfiguration) {
    org.matrix.rustcomponents.sdk.setupTracing(tracingConfiguration.filter)
}

object TracingConfigurations {
    val release = TracingConfiguration(overrides = mapOf(Target.Common to LogLevel.Info))
    val debug = TracingConfiguration(overrides = mapOf(Target.Common to LogLevel.Info))

    fun custom(overrides: Map<Target, LogLevel>) = TracingConfiguration(overrides)
}
