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

package io.element.android.libraries.matrix.api.tracing

data class TracingFilterConfiguration(
    val overrides: Map<Target, LogLevel> = emptyMap(),
) {

    // Order should matters
    private val targetsToLogLevel: MutableMap<Target, LogLevel> = mutableMapOf(
        Target.COMMON to LogLevel.Info,
        Target.HYPER to LogLevel.Warn,
        Target.MATRIX_SDK_CRYPTO to LogLevel.Debug,
        Target.MATRIX_SDK_HTTP_CLIENT to LogLevel.Debug,
        Target.MATRIX_SDK_SLIDING_SYNC to LogLevel.Trace,
        Target.MATRIX_SDK_BASE_SLIDING_SYNC to LogLevel.Trace,
        Target.MATRIX_SDK_UI_TIMELINE to LogLevel.Info,
    )

    val filter: String
        get() {
            overrides.forEach { (target, logLevel) ->
                targetsToLogLevel[target] = logLevel
            }
            return targetsToLogLevel.map {
                if (it.key.filter.isEmpty()) {
                    it.value.filter
                } else {
                    "${it.key.filter}=${it.value.filter}"
                }
            }.joinToString(separator = ",")
        }
}

enum class Target(open val filter: String) {
    COMMON(""),
    ELEMENT("elementx"),
    HYPER("hyper"),
    MATRIX_SDK_FFI("matrix_sdk_ffi"),
    MATRIX_SDK_UNIFFI_API("matrix_sdk_ffi::uniffi_api"),
    MATRIX_SDK_CRYPTO("matrix_sdk_crypto"),
    MATRIX_SDK_HTTP_CLIENT("matrix_sdk::http_client"),
    MATRIX_SDK_SLIDING_SYNC("matrix_sdk::sliding_sync"),
    MATRIX_SDK_BASE_SLIDING_SYNC("matrix_sdk_base::sliding_sync"),
    MATRIX_SDK_UI_TIMELINE("matrix_sdk_ui::timeline"),
}

sealed class LogLevel(val filter: String) {
    object Warn : LogLevel("warn")
    object Trace : LogLevel("trace")
    object Info : LogLevel("info")
    object Debug : LogLevel("debug")
    object Error : LogLevel("error")
}

object TracingFilterConfigurations {
    val release = TracingFilterConfiguration(
        overrides = mapOf(
            Target.COMMON to LogLevel.Info,
            Target.ELEMENT to LogLevel.Debug
        ),
    )
    val debug = TracingFilterConfiguration(
        overrides = mapOf(
            Target.COMMON to LogLevel.Info,
            Target.ELEMENT to LogLevel.Trace
        )
    )

    /**
     *  Use this method to create a custom configuration where all targets will have the same log level.
     */
    fun custom(logLevel: LogLevel) = TracingFilterConfiguration(overrides = Target.values().associateWith { logLevel })

    /**
     * Use this method to override the log level of specific targets.
     */
    fun custom(overrides: Map<Target, LogLevel>) = TracingFilterConfiguration(overrides)
}
