/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.preferences.impl.developer.tracing

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.libraries.matrix.api.tracing.LogLevel
import io.element.android.libraries.matrix.api.tracing.Target
import kotlinx.collections.immutable.persistentMapOf

open class ConfigureTracingStateProvider : PreviewParameterProvider<ConfigureTracingState> {
    override val values: Sequence<ConfigureTracingState>
        get() = sequenceOf(
            aConfigureTracingState(),
        )
}

fun aConfigureTracingState() = ConfigureTracingState(
    targetsToLogLevel = persistentMapOf(
        Target.ELEMENT to LogLevel.INFO,
        Target.MATRIX_SDK_FFI to LogLevel.WARN,
        Target.MATRIX_SDK_BASE_SLIDING_SYNC to LogLevel.ERROR,
    ),
    eventSink = {}
)
