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
    private val defaultLogLevel = LogLevel.INFO

    // Order should matters
    private val targetsToLogLevel: Map<Target, LogLevel> = mapOf(
        Target.HYPER to LogLevel.WARN,
        Target.MATRIX_SDK_CRYPTO to LogLevel.DEBUG,
        Target.MATRIX_SDK_CRYPTO_ACCOUNT to LogLevel.TRACE,
        Target.MATRIX_SDK_HTTP_CLIENT to LogLevel.DEBUG,
        Target.MATRIX_SDK_SLIDING_SYNC to LogLevel.INFO,
        Target.MATRIX_SDK_BASE_SLIDING_SYNC to LogLevel.INFO,
        Target.MATRIX_SDK_UI_TIMELINE to LogLevel.INFO,
        Target.MATRIX_SDK_BASE_CLIENT to LogLevel.TRACE,
        // To debug OIDC logouts
        Target.MATRIX_SDK_OIDC to LogLevel.TRACE,
    )

    fun getLogLevel(target: Target): LogLevel {
        return overrides[target] ?: targetsToLogLevel[target] ?: defaultLogLevel
    }

    val filter: String
        get() {
            val fullMap = Target.entries.associateWith {
                overrides[it] ?: targetsToLogLevel[it] ?: defaultLogLevel
            }
            return fullMap.map {
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
    MATRIX_SDK_CRYPTO_ACCOUNT("matrix_sdk_crypto::olm::account"),
    MATRIX_SDK("matrix_sdk"),
    MATRIX_SDK_HTTP_CLIENT("matrix_sdk::http_client"),
    MATRIX_SDK_CLIENT("matrix_sdk::client"),
    MATRIX_SDK_OIDC("matrix_sdk::oidc"),
    MATRIX_SDK_SLIDING_SYNC("matrix_sdk::sliding_sync"),
    MATRIX_SDK_BASE_SLIDING_SYNC("matrix_sdk_base::sliding_sync"),
    MATRIX_SDK_UI_TIMELINE("matrix_sdk_ui::timeline"),
    MATRIX_SDK_BASE_READ_RECEIPTS("matrix_sdk_base::read_receipts"),
    MATRIX_SDK_BASE_CLIENT("matrix_sdk_base"),
}

enum class LogLevel(open val filter: String) {
    ERROR("error"),
    WARN("warn"),
    INFO("info"),
    DEBUG("debug"),
    TRACE("trace"),
}

object TracingFilterConfigurations {
    val release = TracingFilterConfiguration(
        overrides = mapOf(
            Target.ELEMENT to LogLevel.DEBUG
        ),
    )
    val nightly = TracingFilterConfiguration(
        overrides = mapOf(
            Target.ELEMENT to LogLevel.TRACE,
        ),
    )
    val debug = TracingFilterConfiguration(
        overrides = mapOf(
            Target.ELEMENT to LogLevel.TRACE
        )
    )

    /**
     *  Use this method to create a custom configuration where all targets will have the same log level.
     */
    fun custom(logLevel: LogLevel) = TracingFilterConfiguration(overrides = Target.entries.associateWith { logLevel })

    /**
     * Use this method to override the log level of specific targets.
     */
    fun custom(overrides: Map<Target, LogLevel>) = TracingFilterConfiguration(overrides)
}
